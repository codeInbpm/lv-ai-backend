package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.dto.CommunityStatsDTO;
import com.lvai.entity.StrategyPost;
import com.lvai.entity.UserCollection;
import com.lvai.entity.Destination;
import com.lvai.service.ICommunityService;
import com.lvai.service.IStrategyPostService;
import com.lvai.service.IUserCollectionService;
import com.lvai.service.IDestinationService;
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
    private final IDestinationService destinationService;

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
        } else if (type == 4) { // 目的地/景区
            List<Destination> dests = destinationService.list(new LambdaQueryWrapper<Destination>().in(Destination::getId, targetIds));
            java.util.Map<Long, Destination> destMap = dests.stream().collect(Collectors.toMap(Destination::getId, d -> d));
            return collections.stream().map(c -> {
                com.lvai.vo.UserCollectionVO vo = new com.lvai.vo.UserCollectionVO();
                vo.setId(c.getId());
                vo.setTargetId(c.getTargetId());
                vo.setTargetType(c.getTargetType());
                vo.setData(destMap.get(c.getTargetId()));
                vo.setIsDeleted(vo.getData() == null);
                return vo;
            }).collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }

    @Override
    public List<com.lvai.vo.UserCollectionVO> getAllCollections(Long userId) {
        List<UserCollection> collections = collectionService.list(
                new LambdaQueryWrapper<UserCollection>()
                        .eq(UserCollection::getUserId, userId)
                        .orderByDesc(UserCollection::getCreateTime)
        );
        if (collections.isEmpty()) return Collections.emptyList();

        // 内存分组，针对存在的类型进行批量 IN 查询，保证极高的响应性能与低的数据库压力
        java.util.Map<Integer, List<UserCollection>> groups = collections.stream()
                .collect(Collectors.groupingBy(UserCollection::getTargetType));

        // 1. 批量加载笔记
        List<UserCollection> noteColls = groups.getOrDefault(1, Collections.emptyList());
        java.util.Map<Long, com.lvai.entity.UserNote> noteMap = Collections.emptyMap();
        if (!noteColls.isEmpty()) {
            List<Long> noteIds = noteColls.stream().map(UserCollection::getTargetId).collect(Collectors.toList());
            List<com.lvai.entity.UserNote> notes = userNoteService.list(
                    new LambdaQueryWrapper<com.lvai.entity.UserNote>().in(com.lvai.entity.UserNote::getId, noteIds)
            );
            noteMap = notes.stream().collect(Collectors.toMap(com.lvai.entity.UserNote::getId, n -> n, (v1, v2) -> v1));
        }

        // 2. 批量加载攻略
        List<UserCollection> postColls = groups.getOrDefault(3, Collections.emptyList());
        java.util.Map<Long, StrategyPost> postMap = Collections.emptyMap();
        if (!postColls.isEmpty()) {
            List<Long> postIds = postColls.stream().map(UserCollection::getTargetId).collect(Collectors.toList());
            List<StrategyPost> posts = strategyPostService.list(
                    new LambdaQueryWrapper<StrategyPost>().in(StrategyPost::getId, postIds)
            );
            postMap = posts.stream().collect(Collectors.toMap(StrategyPost::getId, p -> p, (v1, v2) -> v1));
        }

        // 3. 批量加载目的地/景区
        List<UserCollection> destColls = groups.getOrDefault(4, Collections.emptyList());
        java.util.Map<Long, Destination> destMap = Collections.emptyMap();
        if (!destColls.isEmpty()) {
            List<Long> destIds = destColls.stream().map(UserCollection::getTargetId).collect(Collectors.toList());
            List<Destination> dests = destinationService.list(
                    new LambdaQueryWrapper<Destination>().in(Destination::getId, destIds)
            );
            destMap = dests.stream().collect(Collectors.toMap(Destination::getId, d -> d, (v1, v2) -> v1));
        }

        // 组装 VO，保持收藏表中原汁原味的倒排顺序展示给用户
        final java.util.Map<Long, com.lvai.entity.UserNote> finalNoteMap = noteMap;
        final java.util.Map<Long, StrategyPost> finalPostMap = postMap;
        final java.util.Map<Long, Destination> finalDestMap = destMap;

        return collections.stream().map(c -> {
            com.lvai.vo.UserCollectionVO vo = new com.lvai.vo.UserCollectionVO();
            vo.setId(c.getId());
            vo.setTargetId(c.getTargetId());
            vo.setTargetType(c.getTargetType());
            
            if (c.getTargetType() == 1) {
                vo.setData(finalNoteMap.get(c.getTargetId()));
            } else if (c.getTargetType() == 3) {
                vo.setData(finalPostMap.get(c.getTargetId()));
            } else if (c.getTargetType() == 4) {
                vo.setData(finalDestMap.get(c.getTargetId()));
            }
            
            vo.setIsDeleted(vo.getData() == null);
            return vo;
        }).collect(Collectors.toList());
    }
}
