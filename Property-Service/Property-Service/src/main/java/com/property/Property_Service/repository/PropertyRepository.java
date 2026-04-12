package com.property.Property_Service.repository;



import com.property.Property_Service.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // ─── Finder Methods (Spring Data génère le SQL automatiquement) ───────────

    // Toutes les propriétés disponibles
    List<Property> findByAvailableTrue();

    // Toutes les propriétés NON disponibles
    List<Property> findByAvailableFalse();

    // Recherche par type (Appartement, Villa, Studio...)
    List<Property> findByTypeIgnoreCase(String type);

    // Recherche par adresse (contient le mot clé)
    List<Property> findByAddressContainingIgnoreCase(String keyword);

    // Propriétés disponibles d'un certain type
    List<Property> findByTypeIgnoreCaseAndAvailableTrue(String type);

    // Propriétés dont le loyer est <= un montant donné
    List<Property> findByRentPriceLessThanEqual(Double maxPrice);

    // Propriétés disponibles dont le loyer est <= un montant donné
    List<Property> findByAvailableTrueAndRentPriceLessThanEqual(Double maxPrice);

    // Vérifier si une adresse existe déjà (éviter les doublons)
    boolean existsByAddressIgnoreCase(String address);

    // ─── JPQL Queries ─────────────────────────────────────────────────────────

    // Recherche combinée type + ville (dans l'adresse)
    @Query("SELECT p FROM Property p WHERE " +
            "(:type IS NULL OR LOWER(p.type) = LOWER(:type)) AND " +
            "(:city IS NULL OR LOWER(p.address) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:available IS NULL OR p.available = :available)")
    List<Property> searchProperties(
            @Param("type") String type,
            @Param("city") String city,
            @Param("available") Boolean available
    );

    // ─── Native Query ──────────────────────────────────────────────────────────

    // Mise à jour directe de la disponibilité sans charger l'entité
    @Modifying
    @Query("UPDATE Property p SET p.available = :available WHERE p.id = :id")
    int updateAvailability(@Param("id") Long id, @Param("available") Boolean available);
}