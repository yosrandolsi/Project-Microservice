package com.payment.payment_service.service;



import com.payment.payment_service.dto.*;
import com.payment.payment_service.entity.*;
import com.payment.payment_service.exception.*;
import com.payment.payment_service.feign.*;
import com.payment.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ContractClient contractClient;
    private final PropertyClient propertyClient;

    // ─── CREATE ───────────────────────────────────────────────────────────────
    public PaymentResponse create(PaymentRequest request) {

        // 1. Vérifier que le contrat existe via Feign → Contract-Service
        ContractResponse contract;
        try {
            contract = contractClient.getContractById(request.getContractId());
        } catch (Exception e) {
            throw new ContractNotFoundException(request.getContractId());
        }

        // 2. Vérifier que le contrat est encore actif
        if (!contract.isActive()) {
            throw new InvalidPaymentException(
                    "Impossible de créer un paiement : le contrat "
                            + request.getContractId() + " est résilié.");
        }

        // 3. Vérifier que la propriété existe via Feign → Property-Service
        try {
            propertyClient.getPropertyById(request.getPropertyId());
        } catch (Exception e) {
            throw new InvalidPaymentException(
                    "Propriété introuvable avec l'ID : " + request.getPropertyId());
        }

        // 4. Vérifier doublon : même contrat + même date + déjà COMPLETED
        if (paymentRepository.existsByContractIdAndPaymentDateAndStatus(
                request.getContractId(),
                request.getPaymentDate(),
                PaymentStatus.COMPLETED)) {
            throw new PaymentAlreadyExistsException(request.getContractId());
        }

        // 5. Créer le paiement avec statut PENDING par défaut
        Payment payment = Payment.builder()
                .contractId(request.getContractId())
                .propertyId(request.getPropertyId())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .method(request.getMethod())
                .paymentDate(request.getPaymentDate())
                .description(request.getDescription())
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    // ─── GET ALL ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY ID ────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ─── GET BY CONTRACT ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByContract(String contractId) {
        return paymentRepository.findByContractId(contractId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY PROPERTY ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByProperty(Long propertyId) {
        return paymentRepository.findByPropertyId(propertyId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY STATUS ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── UPDATE STATUS ────────────────────────────────────────────────────────
    public PaymentResponse updateStatus(Long id, PaymentStatusRequest request) {
        Payment payment = findOrThrow(id);

        // Un paiement remboursé ne peut plus être modifié
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new InvalidPaymentException(
                    "Un paiement remboursé ne peut plus être modifié.");
        }

        // Un paiement COMPLETED ne peut pas repasser à PENDING
        if (payment.getStatus() == PaymentStatus.COMPLETED
                && request.getStatus() == PaymentStatus.PENDING) {
            throw new InvalidPaymentException(
                    "Un paiement complété ne peut pas repasser à PENDING.");
        }

        payment.setStatus(request.getStatus());
        return toResponse(paymentRepository.save(payment));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    public void delete(Long id) {
        Payment payment = findOrThrow(id);

        // Impossible de supprimer un paiement COMPLETED
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidPaymentException(
                    "Un paiement complété ne peut pas être supprimé.");
        }

        paymentRepository.deleteById(id);
    }

    // ─── TOTAL PAYÉ PAR CONTRAT ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Double getTotalByContract(String contractId) {
        Double total = paymentRepository.getTotalPaidByContract(contractId);
        return total != null ? total : 0.0;
    }

    // ─── TOTAL PAYÉ PAR PROPRIÉTÉ ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Double getTotalByProperty(Long propertyId) {
        Double total = paymentRepository.getTotalPaidByProperty(propertyId);
        return total != null ? total : 0.0;
    }

    // ─── RECHERCHE COMBINÉE ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PaymentResponse> search(String contractId, Long propertyId, PaymentStatus status) {
        return paymentRepository.searchPayments(contractId, propertyId, status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────
    private Payment findOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .contractId(p.getContractId())
                .propertyId(p.getPropertyId())
                .amount(p.getAmount())
                .status(p.getStatus())
                .method(p.getMethod())
                .paymentDate(p.getPaymentDate())
                .description(p.getDescription())
                .build();
    }


    @Transactional(readOnly = true)
    public List<PaymentResponse> getActivePropertiesPaymentsByStatus(PaymentStatus status) {

        // 1. récupérer contrats actifs
        List<ContractResponse> activeContracts = contractClient.getActiveContracts();

        // 2. récupérer les IDs
        List<String> contractIds = activeContracts.stream()
                .map(ContractResponse::getId)
                .toList();

        // 3. récupérer paiements filtrés
        return paymentRepository.findByContractIdInAndStatus(contractIds, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}