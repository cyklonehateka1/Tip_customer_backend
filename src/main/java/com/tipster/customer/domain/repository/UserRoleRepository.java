package com.tipster.customer.domain.repository;

import com.tipster.customer.domain.entities.User;
import com.tipster.customer.domain.entities.UserRole;
import com.tipster.customer.domain.enums.UserRoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUser(User user);
    List<UserRole> findByUserId(UUID userId);
    Optional<UserRole> findByUserAndRole(User user, UserRoleType role);
    boolean existsByUserIdAndRole(UUID userId, UserRoleType role);
}

