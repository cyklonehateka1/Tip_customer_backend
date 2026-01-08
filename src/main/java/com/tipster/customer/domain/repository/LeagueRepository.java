package com.tipster.customer.domain.repository;

import com.tipster.customer.domain.entities.League;
import com.tipster.customer.domain.entities.Provider;
import com.tipster.customer.domain.entities.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeagueRepository extends JpaRepository<League, UUID> {
    Optional<League> findByProviderAndExternalId(Provider provider, String externalId);
    boolean existsByProviderAndExternalId(Provider provider, String externalId);
    List<League> findByProvider(Provider provider);
    List<League> findBySport(Sport sport);
    List<League> findByIsActive(Boolean isActive);
}
