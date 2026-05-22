package com.lvai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.UserBrowsingHistory;
import com.lvai.mapper.UserBrowsingHistoryMapper;
import com.lvai.service.IUserBrowsingHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserBrowsingHistoryServiceImpl extends ServiceImpl<UserBrowsingHistoryMapper, UserBrowsingHistory> implements IUserBrowsingHistoryService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    @Lazy
    private IUserBrowsingHistoryService self;

    private static final String HISTORY_KEY_PREFIX = "browse:history:";
    private static final long HISTORY_TTL_DAYS = 7;

    @Override
    public void addBrowseHistory(Long userId, Integer targetType, Long targetId, String title, String coverUrl) {
        String key = HISTORY_KEY_PREFIX + userId;

        UserBrowsingHistory history = new UserBrowsingHistory();
        history.setUserId(userId);
        history.setTargetType(targetType);
        history.setTargetId(targetId);
        history.setTitle(title);
        history.setCoverUrl(coverUrl);
        history.setViewTime(LocalDateTime.now());
        
        // Remove old history of same target to avoid duplicates in Redis List
        List<String> jsonList = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (CollUtil.isNotEmpty(jsonList)) {
            List<UserBrowsingHistory> listObj = jsonList.stream()
                    .map(json -> JSONUtil.toBean(json, UserBrowsingHistory.class))
                    .collect(Collectors.toList());
            
            // Remove the same target to avoid duplication
            listObj.removeIf(item -> item.getTargetType().equals(targetType) && item.getTargetId().equals(targetId));
            
            // Add new history to the front
            listObj.add(0, history);
            
            // Trim to max 500
            if (listObj.size() > 500) {
                listObj = listObj.subList(0, 500);
            }
            
            // Rewrite the list
            stringRedisTemplate.delete(key);
            List<String> toCache = listObj.stream().map(JSONUtil::toJsonStr).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(toCache)) {
                stringRedisTemplate.opsForList().rightPushAll(key, toCache);
            }
            stringRedisTemplate.expire(key, HISTORY_TTL_DAYS, TimeUnit.DAYS);
        } else {
            // Cache is empty, just push
            stringRedisTemplate.opsForList().leftPush(key, JSONUtil.toJsonStr(history));
            stringRedisTemplate.expire(key, HISTORY_TTL_DAYS, TimeUnit.DAYS);
        }

        // Async write to MySQL
        self.saveHistoryAsync(history);
    }

    @Override
    @Async("asyncExecutor")
    public void saveHistoryAsync(UserBrowsingHistory history) {
        UserBrowsingHistory exist = this.getOne(new LambdaQueryWrapper<UserBrowsingHistory>()
                .eq(UserBrowsingHistory::getUserId, history.getUserId())
                .eq(UserBrowsingHistory::getTargetId, history.getTargetId())
                .eq(UserBrowsingHistory::getTargetType, history.getTargetType()));
        if (exist != null) {
            history.setId(exist.getId());
            this.updateById(history);
        } else {
            this.save(history);
        }
    }

    @Override
    public Page<UserBrowsingHistory> getBrowseHistory(Long userId, int page, int size) {
        String key = HISTORY_KEY_PREFIX + userId;
        Page<UserBrowsingHistory> resultPage = new Page<>(page, size);
        
        // 1. Try to get from Redis
        long start = (long) (page - 1) * size;
        long end = start + size - 1;
        List<String> jsonList = stringRedisTemplate.opsForList().range(key, start, end);
        
        if (CollUtil.isNotEmpty(jsonList)) {
            Long total = stringRedisTemplate.opsForList().size(key);
            resultPage.setTotal(total != null ? total : 0);
            
            List<UserBrowsingHistory> historyList = jsonList.stream()
                    .map(json -> JSONUtil.toBean(json, UserBrowsingHistory.class))
                    .collect(Collectors.toList());
            resultPage.setRecords(historyList);
            return resultPage;
        }

        // 2. Cache miss, read from MySQL
        LambdaQueryWrapper<UserBrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBrowsingHistory::getUserId, userId)
                .orderByDesc(UserBrowsingHistory::getViewTime);
        
        this.page(resultPage, queryWrapper);
        
        // 3. Write back to Redis asynchronously to avoid blocking the read
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            // we write the current page back, but ideally we should load the first few pages into Redis.
            // For simplicity, we just write the records to Redis if we queried page 1.
            if (page == 1) {
                // clear old key just in case
                stringRedisTemplate.delete(key);
                List<String> toCache = resultPage.getRecords().stream()
                        .map(JSONUtil::toJsonStr)
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(toCache)) {
                    stringRedisTemplate.opsForList().rightPushAll(key, toCache);
                    stringRedisTemplate.expire(key, HISTORY_TTL_DAYS, TimeUnit.DAYS);
                }
            }
        }
        
        return resultPage;
    }

    @Override
    public void clearBrowseHistory(Long userId) {
        String key = HISTORY_KEY_PREFIX + userId;
        // clear redis
        stringRedisTemplate.delete(key);
        // physical delete in mysql
        LambdaQueryWrapper<UserBrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBrowsingHistory::getUserId, userId);
        this.remove(queryWrapper);
    }

    @Override
    public void clearBrowseHistoryByTarget(Long userId, Integer targetType, Long targetId) {
        String key = HISTORY_KEY_PREFIX + userId;
        // Since it's hard to delete specific item in redis list by content, we just delete the whole redis cache 
        // to force a reload from MySQL next time, or we can iterate and remove.
        // For simplicity, delete cache.
        stringRedisTemplate.delete(key);
        
        // physical delete in mysql
        LambdaQueryWrapper<UserBrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBrowsingHistory::getUserId, userId)
                .eq(UserBrowsingHistory::getTargetType, targetType)
                .eq(UserBrowsingHistory::getTargetId, targetId);
        this.remove(queryWrapper);
    }
}
