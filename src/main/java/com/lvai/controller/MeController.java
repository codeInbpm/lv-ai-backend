package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvai.common.Result;
import com.lvai.entity.TravelPlan;
import com.lvai.entity.User;
import com.lvai.service.ITravelPlanService;
import com.lvai.service.IUserFootprintService;
import com.lvai.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "我的页面模块")
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {

    private final ITravelPlanService planService;
    private final IUserService userService;
    private final IUserFootprintService footprintService;

    @GetMapping("/trips")
    @Operation(summary = "我的行程列表")
    public Result<IPage<TravelPlan>> getMyTrips(
            @RequestParam(defaultValue = "0") int status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<TravelPlan> wrapper = new LambdaQueryWrapper<TravelPlan>()
                .eq(TravelPlan::getUserId, userId)
                .orderByDesc(TravelPlan::getCreateTime);
        if (status > 0) {
            wrapper.eq(TravelPlan::getStatus, status);
        }
        IPage<TravelPlan> result = planService.page(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @GetMapping("/settings")
    @Operation(summary = "设置页信息")
    public Result<Map<String, Object>> getSettings() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("phone", user.getPhone());
        data.put("gender", user.getGender());
        return Result.success(data);
    }
}
