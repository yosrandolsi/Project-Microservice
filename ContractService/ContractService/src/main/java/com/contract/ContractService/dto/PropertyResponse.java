package com.contract.ContractService.dto;

import lombok.Data;

@Data
public class PropertyResponse {

    private Long id;
    private String type;
    private String address;
    private Double rentPrice;
    private boolean available;
}