package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvai.common.Result;
import com.lvai.dto.CreatePlanDTO;
import com.lvai.dto.PlanQueryDTO;
import com.lvai.entity.TravelPlan;
import com.lvai.service.ITravelPlanService;
import com.lvai.vo.PlanDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "行程管理模块")
@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
public class TravelPlanController {

    private final ITravelPlanService planService;

    @PostMapping("/create")
    @Operation(summary = "AI生成行程(核心接口)")
    public Result<PlanDetailVO> createPlan(@Valid @RequestBody CreatePlanDTO dto) {
        return Result.success(planService.createPlanWithAI(dto));
    }

    @GetMapping("/{planId}")
    @Operation(summary = "获取行程详情")
    public Result<PlanDetailVO> getPlanDetail(@PathVariable Long planId) {
        return Result.success(planService.getPlanDetail(planId));
    }

    @GetMapping("/list")
    @Operation(summary = "我的行程列表")
    public Result<IPage<TravelPlan>> getMyPlans(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(planService.getUserPlans(userId, status, page, size));
    }

    @PutMapping("/{planId}")
    @Operation(summary = "更新行程")
    public Result<TravelPlan> updatePlan(@PathVariable Long planId, @RequestBody TravelPlan plan) {
        plan.setId(planId);
        return Result.success(planService.updatePlan(plan));
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "删除行程")
    public Result<Void> deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        return Result.success();
    }

    @PutMapping("/{planId}/status")
    @Operation(summary = "更新行程状态(开始/完成)")
    public Result<Void> updateStatus(@PathVariable Long planId, @RequestParam Integer status) {
        planService.updatePlanStatus(planId, status);
        return Result.success();
    }

    @PostMapping("/{planId}/collect")
    @Operation(summary = "收藏/取消收藏行程")
    public Result<Boolean> toggleCollect(@PathVariable Long planId) {
        return Result.success(planService.toggleCollection(planId));
    }
}
