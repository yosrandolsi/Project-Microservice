package com.contract.ContractService.service;

import com.contract.ContractService.client.PropertyClient;
import com.contract.ContractService.dto.*;
import com.contract.ContractService.entity.Contract;
import com.contract.ContractService.exception.*;
import com.contract.ContractService.repository.ContractRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final PropertyClient propertyClient;

    // ───────── CREATE CONTRACT ─────────
    public ContractResponse createContract(ContractRequest request) {

        Long propertyId = request.getPropertyId();

        PropertyResponse property = getPropertySafely(propertyId);

        if (!property.isAvailable()) {
            throw new PropertyNotAvailableException(propertyId.toString());
        }

        if (contractRepository.existsByPropertyIdAndActiveTrue(propertyId)) {
            throw new ActiveContractException(propertyId);
        }

        Contract contract = new Contract();
        contract.setTenantId(request.getTenantId());
        contract.setPropertyId(propertyId);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setMonthlyRent(property.getRentPrice());
        contract.setActive(true);

        Contract saved = contractRepository.save(contract);

        try {
            propertyClient.updateAvailability(propertyId, Map.of("available", false));
        } catch (FeignException e) {
            throw new RuntimeException("Failed to update property availability");
        }

        return mapToResponse(saved);
    }

    // ───────── GET ALL ─────────
    @Transactional(readOnly = true)
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ───────── GET BY ID ─────────
    @Transactional(readOnly = true)
    public ContractResponse getContractById(String id) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contract not found: " + id));

        return mapToResponse(contract);
    }

    // ───────── TERMINATE ─────────
    public ContractResponse terminateContract(String id) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contract not found: " + id));

        if (!contract.isActive()) {
            throw new ActiveContractException(contract.getPropertyId());
        }

        contract.setActive(false);
        Contract updated = contractRepository.save(contract);

        try {
            propertyClient.updateAvailability(contract.getPropertyId(), Map.of("available", true));
        } catch (FeignException e) {
            throw new RuntimeException("Failed to release property");
        }

        return mapToResponse(updated);
    }

    // ───────── 🔥 AVAILABILITY SOON (FIXED) ─────────
    @Transactional(readOnly = true)
    public List<AvailableSoonResponse> getUpcomingAvailableProperties(int days) {

        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(days);

        List<Contract> contracts =
                contractRepository.findByActiveTrueAndEndDateBetween(today, targetDate);

        return contracts.stream().map(contract -> {

            long daysRemaining = ChronoUnit.DAYS.between(today, contract.getEndDate());

            PropertyResponse property;
            try {
                property = propertyClient.getPropertyById(contract.getPropertyId());
            } catch (Exception e) {
                property = null;
            }

            return new AvailableSoonResponse(
                    daysRemaining,
                    contract.getEndDate(),
                    property
            );

        }).toList();
    }

    // ───────── PROPERTY CALL SAFE ─────────
    private PropertyResponse getPropertySafely(Long propertyId) {
        try {
            return propertyClient.getPropertyById(propertyId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Property not found: " + propertyId);
        } catch (FeignException e) {
            throw new RuntimeException("Property-Service error");
        }
    }

    // ───────── MAPPER ─────────
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

    @Transactional(readOnly = true)
    public List<ContractResponse> getInactiveContracts() {
        return contractRepository.findByActiveFalse()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<ContractResponse> getActiveContracts() {
        return contractRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}