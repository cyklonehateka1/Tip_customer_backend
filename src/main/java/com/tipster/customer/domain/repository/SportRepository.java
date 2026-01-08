package com.tipster.customer.domain.repository;

import com.tipster.customer.domain.entities.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SportRepository extends JpaRepository<Sport, UUID> {
    Optional<Sport> findBySportKey(String sportKey);
    boolean existsBySportKey(String sportKey);
    List<Sport> findBySportGroup(String sportGroup);
    List<Sport> findByIsActive(Boolean isActive);
}
