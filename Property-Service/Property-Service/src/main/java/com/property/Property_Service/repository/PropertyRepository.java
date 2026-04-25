package com.property.Property_Service.repository;

import com.property.Property_Service.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    long countByAvailableTrue();

    long countByAvailableFalse();

    // ─── Finder Methods (Spring Data génère automatiquement) ───────────────

    // Toutes les propriétés disponibles
    List<Property> findByAvailableTrue();

    // Toutes les propriétés NON disponibles
    List<Property> findByAvailableFalse();

    // Recherche par type (Appartement, Villa, Studio...)
    List<Property> findByTypeIgnoreCase(String type);

    // Recherche par adresse (mot clé)
    List<Property> findByAddressContainingIgnoreCase(String keyword);

    // Propriétés disponibles d'un certain type
    List<Property> findByTypeIgnoreCaseAndAvailableTrue(String type);

    // Propriétés avec prix <= max
    List<Property> findByRentPriceLessThanEqual(Double maxPrice);

    // Propriétés disponibles avec prix <= max
    List<Property> findByAvailableTrueAndRentPriceLessThanEqual(Double maxPrice);

    // Vérifier si adresse existe déjà
    boolean existsByAddressIgnoreCase(String address);

    @Query("""
        SELECT p FROM Property p WHERE
        (:keyword IS NULL OR 
         LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(p.description.criteria) LIKE LOWER(CONCAT('%', :keyword, '%')))

        AND (:rooms IS NULL OR p.description.rooms = :rooms)

        AND (:minSurface IS NULL OR p.description.surface >= :minSurface)
        AND (:maxSurface IS NULL OR p.description.surface <= :maxSurface)

        AND (:minPrice IS NULL OR p.rentPrice >= :minPrice)
        AND (:maxPrice IS NULL OR p.rentPrice <= :maxPrice)

        AND (:available IS NULL OR p.available = :available)
    """)
    List<Property> advancedSearch(
            @Param("keyword") String keyword,
            @Param("rooms") Integer rooms,
            @Param("minSurface") Double minSurface,
            @Param("maxSurface") Double maxSurface,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("available") Boolean available
    );
}