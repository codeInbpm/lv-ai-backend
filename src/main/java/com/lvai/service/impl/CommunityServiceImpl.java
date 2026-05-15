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

    private final com.lvai.service.IUserNoteService userNoteService;

    @Override
    public List<com.lvai.vo.UserCollectionVO> getCollections(Long userId, Integer type) {
        List<UserCollection> collections = collectionService.list(
                new LambdaQueryWrapper<UserCollection>()
                        .eq(UserCollection::getUserId, userId)
                        .eq(UserCollection::getTargetType, type)
                        .orderByDesc(UserCollection::getCreateTime)
        );
        if (collections.isEmpty()) return Collections.emptyList();

        List<Long> targetIds = collections.stream().map(UserCollection::getTargetId).collect(Collectors.toList());
        
        if (type == 3) { // 攻略
            List<StrategyPost> posts = strategyPostService.list(new LambdaQueryWrapper<StrategyPost>().in(StrategyPost::getId, targetIds));
            java.util.Map<Long, StrategyPost> postMap = posts.stream().collect(Collectors.toMap(StrategyPost::getId, p -> p));
            return collections.stream().map(c -> {
                com.lvai.vo.UserCollectionVO vo = new com.lvai.vo.UserCollectionVO();
                vo.setId(c.getId());
                vo.setTargetId(c.getTargetId());
                vo.setTargetType(c.getTargetType());
                vo.setData(postMap.get(c.getTargetId()));
                vo.setIsDeleted(vo.getData() == null);
                return vo;
            }).collect(Collectors.toList());
        } else if (type == 1) { // 笔记
            List<com.lvai.entity.UserNote> notes = userNoteService.list(new LambdaQueryWrapper<com.lvai.entity.UserNote>().in(com.lvai.entity.UserNote::getId, targetIds));
            java.util.Map<Long, com.lvai.entity.UserNote> noteMap = notes.stream().collect(Collectors.toMap(com.lvai.entity.UserNote::getId, n -> n));
            return collections.stream().map(c -> {
                com.lvai.vo.UserCollectionVO vo = new com.lvai.vo.UserCollectionVO();
                vo.setId(c.getId());
                vo.setTargetId(c.getTargetId());
                vo.setTargetType(c.getTargetType());
                vo.setData(noteMap.get(c.getTargetId()));
                vo.setIsDeleted(vo.getData() == null);
                return vo;
            }).collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
}
