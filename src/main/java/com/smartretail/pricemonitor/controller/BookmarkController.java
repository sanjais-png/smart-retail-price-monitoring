package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.BookmarkResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmarks", description = "User commodity/market bookmarks management")
@SecurityRequirement(name = "bearerAuth")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    @Operation(summary = "Add a commodity/market bookmark")
    public ResponseEntity<ApiResponse<BookmarkResponse>> addBookmark(
            Authentication authentication,
            @RequestParam Long commodityId,
            @RequestParam(required = false) Long marketId) {
        BookmarkResponse bookmark = bookmarkService.addBookmark(authentication.getName(), commodityId, marketId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bookmark added successfully", bookmark));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a bookmark")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            Authentication authentication,
            @PathVariable Long id) {
        bookmarkService.removeBookmark(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Bookmark removed successfully"));
    }

    @GetMapping
    @Operation(summary = "Get user's bookmarks")
    public ResponseEntity<ApiResponse<PagedResponse<BookmarkResponse>>> getMyBookmarks(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Bookmarks retrieved",
                bookmarkService.getUserBookmarks(authentication.getName(), page, size)));
    }
}
