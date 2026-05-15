package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lvai.common.Result;
import com.lvai.dto.CommunityStatsDTO;
import com.lvai.service.ICommunityService;
import com.lvai.service.IUserBrowsingHistoryService;
import com.lvai.service.IContentDraftService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "社区互动管理", description = "获取收藏、草稿、历史记录等信息")
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final ICommunityService communityService;
    private final IUserBrowsingHistoryService historyService;
    private final IContentDraftService draftService;

    @GetMapping("/stats")
    @Operation(summary = "获取用户社区数据统计(关注/粉丝/获赞)")
    public Result<CommunityStatsDTO> getCommunityStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(communityService.getUserCommunityStats(userId));
    }

    @GetMapping("/collections/strategies")
    @Operation(summary = "获取用户收藏的攻略")
    public Result<java.util.List<com.lvai.entity.StrategyPost>> getCollectedStrategies() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(communityService.getCollectedStrategies(userId));
    }

    @GetMapping("/history")
    @Operation(summary = "获取用户浏览历史")
    public Result<java.util.List<com.lvai.entity.UserBrowsingHistory>> getBrowsingHistory() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(historyService.list(
                new LambdaQueryWrapper<com.lvai.entity.UserBrowsingHistory>()
                        .eq(com.lvai.entity.UserBrowsingHistory::getUserId, userId)
                        .orderByDesc(com.lvai.entity.UserBrowsingHistory::getViewTime)
        ));
    }

    @GetMapping("/drafts")
    @Operation(summary = "获取用户草稿箱")
    public Result<java.util.List<com.lvai.entity.ContentDraft>> getDrafts() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(draftService.list(
                new LambdaQueryWrapper<com.lvai.entity.ContentDraft>()
                        .eq(com.lvai.entity.ContentDraft::getUserId, userId)
                        .orderByDesc(com.lvai.entity.ContentDraft::getUpdateTime)
        ));
    }
}
