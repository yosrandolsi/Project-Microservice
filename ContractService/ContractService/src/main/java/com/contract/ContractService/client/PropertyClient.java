package com.contract.ContractService.client;

import com.contract.ContractService.dto.PropertyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "Property-Service")

public interface PropertyClient {

    @GetMapping("/properties/{id}")
    PropertyResponse getPropertyById(@PathVariable("id") Long id);

    @PutMapping("/properties/{id}/availability")
    PropertyResponse updateAvailability(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Boolean> body
    );

    @GetMapping("/properties/{id}/check-availability")
    PropertyResponse checkAvailability(@PathVariable("id") Long id);
}