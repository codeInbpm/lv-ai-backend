package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lvai.common.Result;
import com.lvai.entity.UserFootprint;
import com.lvai.service.IUserFootprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "足迹模块")
@RestController
@RequiredArgsConstructor
public class FootprintController {

    private final IUserFootprintService footprintService;

    @GetMapping("/me/footprints")
    @Operation(summary = "我的足迹列表+统计")
    public Result<Map<String, Object>> getMyFootprints() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserFootprint> list = footprintService.getUserFootprints(userId);
        Map<String, Object> stats = footprintService.getFootprintStats(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("stats", stats);
        return Result.success(data);
    }

    @GetMapping("/me/footprints/countries")
    @Operation(summary = "足迹按国家分组")
    public Result<List<Map<String, Object>>> getCountryGroups() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(footprintService.getCountryGroups(userId));
    }

    @GetMapping("/me/footprints/cities")
    @Operation(summary = "足迹按城市分组（含封面图和天数）")
    public Result<List<Map<String, Object>>> getCityGroups() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(footprintService.getCityGroups(userId));
    }

    @PostMapping("/footprint/add")
    @Operation(summary = "添加足迹打卡")
    public Result<UserFootprint> addFootprint(@RequestBody UserFootprint footprint) {
        Long userId = StpUtil.getLoginIdAsLong();
        footprint.setUserId(userId);
        UserFootprint saved = footprintService.addFootprint(footprint);
        return Result.success(saved);
    }
}
