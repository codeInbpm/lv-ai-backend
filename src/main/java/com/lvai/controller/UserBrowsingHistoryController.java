package com.lvai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvai.common.Result;
import com.lvai.entity.UserBrowsingHistory;
import com.lvai.service.IUserBrowsingHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/history")
@Tag(name = "用户浏览历史接口")
public class UserBrowsingHistoryController {

    @Autowired
    private IUserBrowsingHistoryService userBrowsingHistoryService;

    @GetMapping("/list")
    @Operation(summary = "获取用户浏览历史列表")
    public Result<Page<UserBrowsingHistory>> getBrowseHistory(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserBrowsingHistory> result = userBrowsingHistoryService.getBrowseHistory(userId, page, size);
        return Result.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "添加浏览历史")
    public Result<Void> addBrowseHistory(@RequestBody UserBrowsingHistory req) {
        userBrowsingHistoryService.addBrowseHistory(req.getUserId(), req.getTargetType(), req.getTargetId(), req.getTitle(), req.getCoverUrl());
        return Result.success(null);
    }

    @PostMapping("/clear")
    @Operation(summary = "清空全部浏览历史")
    public Result<Void> clearBrowseHistory(@RequestBody java.util.Map<String, Long> params) {
        Long userId = params.get("userId");
        if (userId != null) {
            userBrowsingHistoryService.clearBrowseHistory(userId);
        }
        return Result.success(null);
    }

    @PostMapping("/clear/target")
    @Operation(summary = "清空指定浏览历史")
    public Result<Void> clearBrowseHistoryByTarget(@RequestBody UserBrowsingHistory req) {
        userBrowsingHistoryService.clearBrowseHistoryByTarget(req.getUserId(), req.getTargetType(), req.getTargetId());
        return Result.success(null);
    }
}
