package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.BookmarkResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;

public interface BookmarkService {
    BookmarkResponse addBookmark(String username, Long commodityId, Long marketId);
    void removeBookmark(String username, Long bookmarkId);
    PagedResponse<BookmarkResponse> getUserBookmarks(String username, int page, int size);
}
