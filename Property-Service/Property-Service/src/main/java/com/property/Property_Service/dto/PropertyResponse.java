package com.property.Property_Service.dto;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyResponse {
    private Long id;
    private String type;
    private String address;
    private Double rentPrice;
    private Boolean available;
}
