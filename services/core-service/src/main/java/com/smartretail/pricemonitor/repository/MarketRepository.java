package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {
    List<Market> findByDistrictId(Long districtId);
    List<Market> findByCityIgnoreCase(String city);
    
    @Query("SELECT m FROM Market m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.city) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Market> searchMarkets(@Param("query") String query, Pageable pageable);
}
