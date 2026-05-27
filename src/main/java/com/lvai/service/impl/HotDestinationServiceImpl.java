package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.entity.Destination;
import com.lvai.entity.UserFootprint;
import com.lvai.entity.UserNote;
import com.lvai.mapper.DestinationMapper;
import com.lvai.mapper.UserFootprintMapper;
import com.lvai.mapper.UserNoteMapper;
import com.lvai.service.IHotDestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class HotDestinationServiceImpl implements IHotDestinationService {

    private final DestinationMapper destinationMapper;
    private final UserFootprintMapper footprintMapper;
    private final UserNoteMapper noteMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public static final String HOT_DESTINATION_CACHE_KEY = "lvai:hot_destinations";

    @Override
    public void calculateHotScore(Long destinationId) {
        Destination destination = destinationMapper.selectById(destinationId);
        if (destination == null) return;
        
        // 利用名称进行模糊匹配，因为Footprint和Note目前没有直接关联destinationId
        String matchName = destination.getName();

        // 1. 打卡数量
        Long checkinCount = footprintMapper.selectCount(
            new LambdaQueryWrapper<UserFootprint>()
                .like(UserFootprint::getCity, matchName).or()
                .like(UserFootprint::getLocationName, matchName)
        );

        // 2. 笔记/攻略数量
        Long noteCount = noteMapper.selectCount(
            new LambdaQueryWrapper<UserNote>()
                .like(UserNote::getLocationName, matchName)
        );

        // 3. 近期活跃度（最近7天打卡数）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Long recentCheckinCount = footprintMapper.selectCount(
            new LambdaQueryWrapper<UserFootprint>()
                .ge(UserFootprint::getCreateTime, sevenDaysAgo)
                .and(w -> w.like(UserFootprint::getCity, matchName).or().like(UserFootprint::getLocationName, matchName))
        );

        // 热度计算公式
        long score = (checkinCount != null ? checkinCount : 0) * 100 +
                     (noteCount != null ? noteCount : 0) * 80 +
                     (destination.getViewCount() != null ? destination.getViewCount() : 0) * 30 +
                     (destination.getLikeCount() != null ? destination.getLikeCount() : 0) * 50 +
                     (recentCheckinCount != null ? recentCheckinCount : 0) * 200;

        destination.setHotScore(score);
        destination.setCheckinCount(checkinCount != null ? checkinCount.intValue() : 0);
        destination.setNoteCount(noteCount != null ? noteCount.intValue() : 0);
        
        destinationMapper.updateById(destination);
    }

    @Override
    public List<Destination> getHotDestinations(int limit) {
        // 先从缓存获取
        String cacheStr = stringRedisTemplate.opsForValue().get(HOT_DESTINATION_CACHE_KEY);
        if (cn.hutool.core.util.StrUtil.isNotBlank(cacheStr)) {
            List<Destination> cacheList = cn.hutool.json.JSONUtil.toList(cacheStr, Destination.class);
            return cacheList.size() > limit ? cacheList.subList(0, limit) : cacheList;
        }

        // 缓存失效，查询数据库
        List<Destination> dbList = destinationMapper.selectList(
            new LambdaQueryWrapper<Destination>()
                .orderByDesc(Destination::getHotScore)
                .orderByDesc(Destination::getSortOrder)
                .last("limit " + limit)
        );

        // 写入缓存，设置过期时间
        if (dbList != null && !dbList.isEmpty()) {
            stringRedisTemplate.opsForValue().set(HOT_DESTINATION_CACHE_KEY, cn.hutool.json.JSONUtil.toJsonStr(dbList), 24, TimeUnit.HOURS);
        }

        return dbList;
    }
}
