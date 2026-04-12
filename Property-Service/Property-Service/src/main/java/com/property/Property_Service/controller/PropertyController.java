package com.property.Property_Service.controller;


import com.property.Property_Service.dto.*;
import com.property.Property_Service.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
@Tag(name = "Property", description = "Gestion des propriétés immobilières")
public class PropertyController {

    private final PropertyService propertyService;

    // POST /properties
    @PostMapping
    @Operation(summary = "Créer une propriété")
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyService.create(request));
    }

    // GET /properties
    @GetMapping
    @Operation(summary = "Lister toutes les propriétés")
    public ResponseEntity<List<PropertyResponse>> getAll() {
        return ResponseEntity.ok(propertyService.getAll());
    }

    // GET /properties/available
    @GetMapping("/available")
    @Operation(summary = "Lister les propriétés disponibles")
    public ResponseEntity<List<PropertyResponse>> getAvailable() {
        return ResponseEntity.ok(propertyService.getAvailable());
    }

    // GET /properties/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une propriété par ID")
    public ResponseEntity<PropertyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getById(id));
    }

    // PUT /properties/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une propriété")
    public ResponseEntity<PropertyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.update(id, request));
    }

    // DELETE /properties/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une propriété")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        propertyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<PropertyResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(propertyService.updateAvailability(id, request));
    }

    // GET /properties/{id}/check-availability  (appelé par Contract-Service via Feign)
    @GetMapping("/{id}/check-availability")
    @Operation(summary = "Vérifier si une propriété est disponible (usage interne Feign)")
    public ResponseEntity<PropertyResponse> checkAvailability(@PathVariable Long id) {
        propertyService.checkAvailability(id);
        return ResponseEntity.ok(propertyService.getById(id));
    }
}
