package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.UserSearchHistory;
import java.util.List;

public interface UserSearchHistoryService extends IService<UserSearchHistory> {
    List<String> getRecentSearchHistory(Long userId, int limit);
    void addSearchHistory(Long userId, String keyword);
    void clearSearchHistory(Long userId);
}
