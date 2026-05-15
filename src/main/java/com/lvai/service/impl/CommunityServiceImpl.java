package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.dto.CommunityStatsDTO;
import com.lvai.entity.StrategyPost;
import com.lvai.entity.UserCollection;
import com.lvai.service.ICommunityService;
import com.lvai.service.IStrategyPostService;
import com.lvai.service.IUserCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements ICommunityService {

    private final IUserCollectionService collectionService;
    private final IStrategyPostService strategyPostService;

    @Override
    public CommunityStatsDTO getUserCommunityStats(Long userId) {
        CommunityStatsDTO stats = new CommunityStatsDTO();
        // 模拟数据，后续可通过关联 user_follow, user_like 等表查询真实数据
        stats.setFollowingCount(12);
        stats.setFollowersCount(128);
        stats.setLikesAndFavsCount(3500); 
        return stats;
    }

    @Override
    public List<StrategyPost> getCollectedStrategies(Long userId) {
        List<UserCollection> collections = collectionService.list(
                new LambdaQueryWrapper<UserCollection>()
                        .eq(UserCollection::getUserId, userId)
                        .eq(UserCollection::getTargetType, 3) // 3 represents strategy
                        .orderByDesc(UserCollection::getCreateTime)
        );
        List<Long> targetIds = collections.stream().map(UserCollection::getTargetId).collect(Collectors.toList());
        if (targetIds.isEmpty()) return Collections.emptyList();
        
        return strategyPostService.list(
                new LambdaQueryWrapper<StrategyPost>().in(StrategyPost::getId, targetIds)
        );
    }
}
