package com.property.Property_Service.service;



import com.property.Property_Service.dto.*;
import com.property.Property_Service.entity.Property;
import com.property.Property_Service.exception.*;
import com.property.Property_Service.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;

    // ─── CREATE ───────────────────────────────────────────────────────────────
    public PropertyResponse create(PropertyRequest request) {
        Property property = Property.builder()
                .type(request.getType())
                .address(request.getAddress())
                .rentPrice(request.getRentPrice())
                .available(true)
                .build();
        return toResponse(propertyRepository.save(property));
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAll() {
        return propertyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── READ AVAILABLE ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAvailable() {
        return propertyRepository.findByAvailableTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── READ BY ID ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PropertyResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────
    public PropertyResponse update(Long id, PropertyRequest request) {
        Property property = findOrThrow(id);
        property.setType(request.getType());
        property.setAddress(request.getAddress());
        property.setRentPrice(request.getRentPrice());
        return toResponse(propertyRepository.save(property));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    public void delete(Long id) {
        findOrThrow(id);
        propertyRepository.deleteById(id);
    }

    // ─── PATCH AVAILABILITY ───────────────────────────────────────────────────
    public PropertyResponse updateAvailability(Long id, AvailabilityRequest request) {
        Property property = findOrThrow(id);
        property.setAvailable(request.getAvailable());
        return toResponse(propertyRepository.save(property));
    }

    // ─── INTERNAL: vérifié par Contract-Service via Feign ────────────────────
    @Transactional(readOnly = true)
    public void checkAvailability(Long id) {
        Property property = findOrThrow(id);
        if (!property.getAvailable()) {
            throw new PropertyNotAvailableException(id);
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────
    private Property findOrThrow(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException(id));
    }

    private PropertyResponse toResponse(Property p) {
        return PropertyResponse.builder()
                .id(p.getId())
                .type(p.getType())
                .address(p.getAddress())
                .rentPrice(p.getRentPrice())
                .available(p.getAvailable())
                .build();
    }
}