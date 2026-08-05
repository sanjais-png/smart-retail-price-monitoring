package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.Commodity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommodityRepository extends JpaRepository<Commodity, Long> {
    Optional<Commodity> findByCode(String code);
    
    Page<Commodity> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT c FROM Commodity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Commodity> searchCommodities(@Param("query") String query, Pageable pageable);

    List<Commodity> findByIsActiveTrue();
}
