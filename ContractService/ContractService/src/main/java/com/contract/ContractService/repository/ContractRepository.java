package com.contract.ContractService.repository;

import com.contract.ContractService.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, String> {

    boolean existsByPropertyIdAndActiveTrue(Long propertyId);

    Optional<Contract> findByPropertyIdAndActiveTrue(String propertyId);
}