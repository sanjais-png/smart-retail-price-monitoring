package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Optional<Authority> findByUserId(Long userId);
    Optional<Authority> findByBadgeNumber(String badgeNumber);
}
