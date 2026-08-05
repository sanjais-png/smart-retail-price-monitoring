package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByCodeIgnoreCase(String code);
    Optional<State> findByNameIgnoreCase(String name);
    Optional<State> findByName(String name);
}
