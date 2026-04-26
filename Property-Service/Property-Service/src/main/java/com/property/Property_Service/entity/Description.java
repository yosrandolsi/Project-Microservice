package com.property.Property_Service.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Description {

    private Integer rooms;    // ✅ @NotNull retiré de l'entité
    private Double surface;   // ✅ @NotNull retiré de l'entité
    private String criteria;
}