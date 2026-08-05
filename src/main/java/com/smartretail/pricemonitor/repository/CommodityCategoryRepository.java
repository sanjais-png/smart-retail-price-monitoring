package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.CommodityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommodityCategoryRepository extends JpaRepository<CommodityCategory, Long> {
    Optional<CommodityCategory> findByNameIgnoreCase(String name);
    Boolean existsByNameIgnoreCase(String name);
}
