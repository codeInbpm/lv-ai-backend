package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.UserSearchHistory;
import com.lvai.mapper.UserSearchHistoryMapper;
import com.lvai.service.UserSearchHistoryService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserSearchHistoryServiceImpl extends ServiceImpl<UserSearchHistoryMapper, UserSearchHistory> implements UserSearchHistoryService {

    @Override
    public List<String> getRecentSearchHistory(Long userId, int limit) {
        LambdaQueryWrapper<UserSearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserSearchHistory::getUserId, userId)
                .orderByDesc(UserSearchHistory::getUpdateTime)
                .last("LIMIT " + limit);
        return this.list(queryWrapper).stream()
                .map(UserSearchHistory::getKeyword)
                .collect(Collectors.toList());
    }

    @Override
    public void addSearchHistory(Long userId, String keyword) {
        LambdaQueryWrapper<UserSearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserSearchHistory::getUserId, userId)
                .eq(UserSearchHistory::getKeyword, keyword);
        UserSearchHistory exist = this.getOne(queryWrapper);
        if (exist != null) {
            // Update time
            this.updateById(exist);
        } else {
            UserSearchHistory history = new UserSearchHistory();
            history.setUserId(userId);
            history.setKeyword(keyword);
            this.save(history);
        }
    }

    @Override
    public void clearSearchHistory(Long userId) {
        LambdaQueryWrapper<UserSearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserSearchHistory::getUserId, userId);
        this.remove(queryWrapper);
    }
}
