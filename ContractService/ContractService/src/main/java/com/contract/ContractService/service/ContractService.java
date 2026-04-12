package com.contract.ContractService.service;

import com.contract.ContractService.client.PropertyClient;
import com.contract.ContractService.dto.ContractRequest;
import com.contract.ContractService.dto.ContractResponse;
import com.contract.ContractService.dto.PropertyResponse;
import com.contract.ContractService.entity.Contract;
import com.contract.ContractService.exception.ActiveContractException;
import com.contract.ContractService.exception.PropertyNotAvailableException;
import com.contract.ContractService.exception.ResourceNotFoundException;
import com.contract.ContractService.repository.ContractRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final PropertyClient propertyClient;

    // ───────────────── CREATE CONTRACT ─────────────────
    public ContractResponse createContract(ContractRequest request) {

        Long propertyId = request.getPropertyId();

        // 1. Appel Property-Service (avec gestion d'erreur Feign)
        PropertyResponse property = getPropertySafely(propertyId);

        // 2. Vérifier disponibilité
        if (!property.isAvailable()) {
            throw new PropertyNotAvailableException(propertyId.toString());
        }

        // 3. Vérifier règle métier : un seul contrat actif
        if (contractRepository.existsByPropertyIdAndActiveTrue(propertyId)) {
            throw new ActiveContractException(propertyId);
        }

        // 4. Créer contrat
        Contract contract = new Contract();
        contract.setTenantId(request.getTenantId());
        contract.setPropertyId(propertyId);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setMonthlyRent(property.getRentPrice());
        contract.setActive(true);

        Contract saved = contractRepository.save(contract);

        // 5. Marquer propriété comme non disponible
        try {
            propertyClient.updateAvailability(propertyId, Map.of("available", false));
        } catch (FeignException e) {
            throw new RuntimeException("Failed to update property availability");
        }

        return mapToResponse(saved);
    }

    // ───────────────── GET ALL CONTRACTS ─────────────────
    @Transactional(readOnly = true)
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ───────────────── TERMINATE CONTRACT ─────────────────
    public ContractResponse terminateContract(String id) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contract not found with id: " + id));

        if (!contract.isActive()) {
            throw new ActiveContractException(contract.getPropertyId());
        }

        contract.setActive(false);
        Contract updated = contractRepository.save(contract);

        // libérer propriété
        try {
            propertyClient.updateAvailability(
                    contract.getPropertyId(),
                    Map.of("available", true)
            );
        } catch (FeignException e) {
            throw new RuntimeException("Failed to release property");
        }

        return mapToResponse(updated);
    }

    // ───────────────── SAFE PROPERTY CALL ─────────────────
    private PropertyResponse getPropertySafely(Long propertyId) {
        try {
            return propertyClient.getPropertyById(propertyId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Property not found with id: " + propertyId);
        } catch (FeignException e) {
            throw new RuntimeException("Error calling Property-Service");
        }
    }

    // ───────────────── MAPPER ─────────────────
    private ContractResponse mapToResponse(Contract contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getTenantId(),
                contract.getPropertyId(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getMonthlyRent(),
                contract.isActive()
        );
    }
}