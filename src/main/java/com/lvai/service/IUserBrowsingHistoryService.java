package com.lvai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.UserBrowsingHistory;

public interface IUserBrowsingHistoryService extends IService<UserBrowsingHistory> {

    void addBrowseHistory(Long userId, Integer targetType, Long targetId, String title, String coverUrl);

    Page<UserBrowsingHistory> getBrowseHistory(Long userId, int page, int size);

    void clearBrowseHistory(Long userId);
    
    void clearBrowseHistoryByTarget(Long userId, Integer targetType, Long targetId);
    
    void saveHistoryAsync(UserBrowsingHistory history);
}
