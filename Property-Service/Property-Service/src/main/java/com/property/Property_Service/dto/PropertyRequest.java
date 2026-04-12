package com.property.Property_Service.dto;



import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyRequest {

    @NotBlank(message = "Le type est obligatoire")
    @Size(min = 2, max = 50, message = "Le type doit contenir entre 2 et 50 caractères")
    private String type;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(min = 5, max = 200, message = "L'adresse doit contenir entre 5 et 200 caractères")
    private String address;

    @NotNull(message = "Le prix de location est obligatoire")
    @Positive(message = "Le prix doit être un nombre positif")
    @DecimalMax(value = "99999.99", message = "Le prix ne peut pas dépasser 99 999")
    private Double rentPrice;
}
