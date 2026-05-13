package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvai.common.Result;
import com.lvai.entity.StrategyPost;
import com.lvai.service.IStrategyPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "精选攻略模块")
@RestController
@RequestMapping("/strategy")
@RequiredArgsConstructor
public class StrategyController {

    private final IStrategyPostService strategyPostService;

    @GetMapping("/list")
    @Operation(summary = "获取攻略列表")
    public Result<Page<StrategyPost>> getStrategies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String source,
            @RequestParam(required = false) String keyword) {
        return Result.success(strategyPostService.getStrategies(page, size, source, keyword));
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "获取攻略详情")
    public Result<StrategyPost> getStrategyDetail(@PathVariable Long id) {
        return Result.success(strategyPostService.getStrategyDetail(id));
    }

    @PostMapping("/publish")
    @Operation(summary = "发布 UGC 站内攻略")
    public Result<StrategyPost> publishStrategy(@RequestBody StrategyPost post) {
        long userId = StpUtil.getLoginIdAsLong();
        post.setUserId(userId);
        post.setSource("internal");
        post.setStatus(1); // 默认直接审核通过展示
        strategyPostService.save(post);
        return Result.success(post);
    }
}
