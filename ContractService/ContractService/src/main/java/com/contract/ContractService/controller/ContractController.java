package com.contract.ContractService.controller;

import com.contract.ContractService.dto.ContractRequest;
import com.contract.ContractService.dto.ContractResponse;
import com.contract.ContractService.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    // POST /contracts
    @PostMapping
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.createContract(request));
    }

    // GET /contracts
    @GetMapping
    public ResponseEntity<List<ContractResponse>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    // PUT /contracts/{id}/terminate
    @PutMapping("/{id}/terminate")
    public ResponseEntity<ContractResponse> terminateContract(@PathVariable String id) {
        return ResponseEntity.ok(contractService.terminateContract(id));
    }
}