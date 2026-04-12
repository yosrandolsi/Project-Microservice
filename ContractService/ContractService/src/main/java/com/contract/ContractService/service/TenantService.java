package com.contract.ContractService.service;

import com.contract.ContractService.dto.TenantRequest;
import com.contract.ContractService.dto.TenantResponse;
import com.contract.ContractService.entity.Tenant;
import com.contract.ContractService.exception.ResourceNotFoundException;
import com.contract.ContractService.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    // POST /tenants
    public TenantResponse createTenant(TenantRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setCin(request.getCin());
        tenant.setPhone(request.getPhone());
        return toResponse(tenantRepository.save(tenant));
    }

    // GET /tenants
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET /tenants/{id}
    public TenantResponse getTenantById(String id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant introuvable : " + id));
        return toResponse(tenant);
    }

    // PUT /tenants/{id}
    public TenantResponse updateTenant(String id, TenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant introuvable : " + id));
        tenant.setName(request.getName());
        tenant.setCin(request.getCin());
        tenant.setPhone(request.getPhone());
        return toResponse(tenantRepository.save(tenant));
    }

    // DELETE /tenants/{id}
    public void deleteTenant(String id) {
        if (!tenantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tenant introuvable : " + id);
        }
        tenantRepository.deleteById(id);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getCin(),
                tenant.getPhone()
        );
    }
}