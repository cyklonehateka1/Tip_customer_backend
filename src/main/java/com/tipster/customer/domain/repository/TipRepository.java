package com.tipster.customer.domain.repository;

import com.tipster.customer.domain.entities.Tip;
import com.tipster.customer.domain.enums.TipStatusType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface TipRepository extends JpaRepository<Tip, UUID> {

    @EntityGraph(attributePaths = {
        "tipster",
        "tipster.user"
    })
    @Query("""
        SELECT t
        FROM Tip t
        JOIN t.tipster tipster
        WHERE t.isPublished = true
          AND (:keyword IS NULL OR :keyword = '' OR
               LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:tipsterId IS NULL OR tipster.id = :tipsterId)
          AND (:minPrice IS NULL OR t.price >= :minPrice)
          AND (:maxPrice IS NULL OR t.price <= :maxPrice)
          AND (:status IS NULL OR t.status = :status)
        ORDER BY tipster.rating DESC,
                 tipster.successRate DESC,
                 t.publishedAt DESC NULLS LAST,
                 t.createdAt DESC
    """)
    Page<Tip> findPublishedTipsWithFilters(
            @Param("keyword") String keyword,
            @Param("tipsterId") UUID tipsterId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") TipStatusType status,
            Pageable pageable
    );

    @Query("SELECT COUNT(t) FROM Tip t WHERE t.isPublished = true AND t.price = 0")
    long countFreeTips();

    @Query("""
        SELECT COUNT(DISTINCT t) FROM Tip t
        WHERE t.isPublished = true
          AND NOT EXISTS (
              SELECT ts FROM TipSelection ts
              JOIN ts.match m
              WHERE ts.tip.id = t.id
                AND (m.status != :scheduledStatus OR m.matchDate <= :now)
          )
    """)
    long countAvailableTips(
            @Param("now") OffsetDateTime now,
            @Param("scheduledStatus") com.tipster.customer.domain.enums.MatchStatusType scheduledStatus
    );
}
