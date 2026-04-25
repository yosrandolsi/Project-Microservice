package com.property.Property_Service.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Description {

    @NotNull(message = "Nombre de pièces obligatoire")
    private Integer rooms;

    @NotNull(message = "Surface obligatoire")
    private Double surface;

    private String criteria;
}