package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Page<Bookmark> findByUserId(Long userId, Pageable pageable);
    Optional<Bookmark> findByUserIdAndCommodityIdAndMarketId(Long userId, Long commodityId, Long marketId);
    Boolean existsByUserIdAndCommodityIdAndMarketId(Long userId, Long commodityId, Long marketId);
}
