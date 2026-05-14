package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvai.common.Result;
import com.lvai.entity.TravelPlan;
import com.lvai.entity.UserCollection;
import com.lvai.service.ITravelPlanService;
import com.lvai.service.IUserCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "个人中心模块")
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IUserCollectionService collectionService;
    private final ITravelPlanService planService;

    @GetMapping("/collections")
    @Operation(summary = "我的收藏")
    public Result<IPage<TravelPlan>> getCollections(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserCollection> collections = collectionService.list(
                new LambdaQueryWrapper<UserCollection>()
                        .eq(UserCollection::getUserId, userId)
                        .eq(UserCollection::getTargetType, 1)
                        .orderByDesc(UserCollection::getCreateTime)
        );
        List<Long> planIds = collections.stream().map(UserCollection::getTargetId).collect(Collectors.toList());
        if (planIds.isEmpty()) return Result.success(new Page<>());

        IPage<TravelPlan> result = planService.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<TravelPlan>().in(TravelPlan::getId, planIds)
        );
        return Result.success(result);
    }

    @GetMapping("/footprints")
    @Operation(summary = "我的足迹(已完成行程的目的地)")
    public Result<?> getFootprints() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<TravelPlan> completedPlans = planService.list(
                new LambdaQueryWrapper<TravelPlan>()
                        .eq(TravelPlan::getUserId, userId)
                        .eq(TravelPlan::getStatus, 3)
                        .select(TravelPlan::getDestination, TravelPlan::getDestinationLng,
                                TravelPlan::getDestinationLat, TravelPlan::getStartDate)
        );
        return Result.success(completedPlans);
    }

    @GetMapping("/stats")
    @Operation(summary = "个人旅行统计")
    public Result<?> getStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        long total = planService.count(new LambdaQueryWrapper<TravelPlan>().eq(TravelPlan::getUserId, userId));
        long completed = planService.count(new LambdaQueryWrapper<TravelPlan>()
                .eq(TravelPlan::getUserId, userId).eq(TravelPlan::getStatus, 3));
        long ongoing = planService.count(new LambdaQueryWrapper<TravelPlan>()
                .eq(TravelPlan::getUserId, userId).eq(TravelPlan::getStatus, 2));
                
        com.lvai.service.IUserNoteService userNoteService = cn.hutool.extra.spring.SpringUtil.getBean(com.lvai.service.IUserNoteService.class);
        long notesCount = userNoteService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lvai.entity.UserNote>().eq(com.lvai.entity.UserNote::getUserId, userId));
        
        return Result.success(java.util.Map.of(
                "totalPlans", total,
                "completedPlans", completed,
                "ongoingPlans", ongoing,
                "notesCount", notesCount
        ));
    }
}
