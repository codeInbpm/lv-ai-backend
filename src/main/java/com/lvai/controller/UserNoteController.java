package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvai.common.Result;
import com.lvai.entity.StrategyPost;
import com.lvai.entity.UserNote;
import com.lvai.service.IUserNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "我的笔记模块")
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class UserNoteController {

    private final IUserNoteService userNoteService;

    @PostMapping("/publish")
    @Operation(summary = "发布笔记")
    public Result<UserNote> publishNote(@RequestBody UserNote note) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userNoteService.publishNote(userId, note));
    }

    @GetMapping("/list")
    @Operation(summary = "获取我的笔记列表")
    public Result<Page<UserNote>> getMyNotes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userNoteService.getMyNotes(userId, page, size));
    }

    @GetMapping("/liked")
    @Operation(summary = "获取我赞过的(攻略)")
    public Result<Page<StrategyPost>> getMyLiked(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userNoteService.getMyLiked(userId, page, size));
    }

    @GetMapping("/collected")
    @Operation(summary = "获取我收藏的(攻略)")
    public Result<Page<StrategyPost>> getMyCollected(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userNoteService.getMyCollected(userId, page, size));
    }
}
