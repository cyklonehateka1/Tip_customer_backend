package com.tipster.customer.domain.repository;

import com.tipster.customer.domain.entities.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, UUID> {
    Optional<Provider> findByCode(String code);
    boolean existsByCode(String code);
}
