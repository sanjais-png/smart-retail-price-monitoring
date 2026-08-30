package com.smartretail.pricemonitor.service.impl;

import com.smartretail.pricemonitor.dto.BookmarkResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.entity.Bookmark;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.entity.User;
import com.smartretail.pricemonitor.exception.BadRequestException;
import com.smartretail.pricemonitor.exception.ResourceNotFoundException;
import com.smartretail.pricemonitor.exception.UnauthorizedException;
import com.smartretail.pricemonitor.repository.BookmarkRepository;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.UserRepository;
import com.smartretail.pricemonitor.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;

    @Override
    @Transactional
    public BookmarkResponse addBookmark(String username, Long commodityId, Long marketId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity", "id", commodityId));

        Market market = marketId != null ? marketRepository.findById(marketId)
                .orElseThrow(() -> new ResourceNotFoundException("Market", "id", marketId)) : null;

        Long mId = market != null ? market.getId() : null;
        if (bookmarkRepository.existsByUserIdAndCommodityIdAndMarketId(user.getId(), commodity.getId(), mId)) {
            throw new BadRequestException("Bookmark already exists for this commodity and market");
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .commodity(commodity)
                .market(market)
                .build();

        return mapToResponse(bookmarkRepository.save(bookmark));
    }

    @Override
    @Transactional
    public void removeBookmark(String username, Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", "id", bookmarkId));

        if (!bookmark.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("Not authorized to remove this bookmark");
        }

        bookmarkRepository.delete(bookmark);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookmarkResponse> getUserBookmarks(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Bookmark> bookmarksPage = bookmarkRepository.findByUserId(user.getId(), pageable);

        List<BookmarkResponse> content = bookmarksPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<BookmarkResponse>builder()
                .content(content)
                .page(bookmarksPage.getNumber())
                .size(bookmarksPage.getSize())
                .totalElements(bookmarksPage.getTotalElements())
                .totalPages(bookmarksPage.getTotalPages())
                .last(bookmarksPage.isLast())
                .build();
    }

    private BookmarkResponse mapToResponse(Bookmark b) {
        return BookmarkResponse.builder()
                .id(b.getId())
                .commodityId(b.getCommodity().getId())
                .commodityName(b.getCommodity().getName())
                .marketId(b.getMarket() != null ? b.getMarket().getId() : null)
                .marketName(b.getMarket() != null ? b.getMarket().getName() : null)
                .createdAt(b.getCreatedAt())
                .build();
    }
}
