package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.Result;
import com.lvai.entity.StrategyLike;
import com.lvai.entity.UserCollection;
import com.lvai.service.IStrategyCommentService;
import com.lvai.service.IStrategyLikeService;
import com.lvai.service.IUserCollectionService;
import com.lvai.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "攻略交互模块")
@RestController
@RequestMapping("/strategy/interaction")
@RequiredArgsConstructor
public class StrategyInteractionController {

    private final IStrategyLikeService strategyLikeService;
    private final IStrategyCommentService strategyCommentService;
    private final IUserCollectionService userCollectionService;

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞/取消点赞攻略")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean isLiked = strategyLikeService.toggleLike(id, userId);
        return Result.success(isLiked);
    }

    @PostMapping("/{id}/collect")
    @Operation(summary = "收藏/取消收藏攻略")
    public Result<Boolean> toggleCollect(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<UserCollection> query = new LambdaQueryWrapper<>();
        query.eq(UserCollection::getTargetId, id)
             .eq(UserCollection::getTargetType, 3) // 3表示攻略
             .eq(UserCollection::getUserId, userId);
        
        UserCollection exist = userCollectionService.getOne(query);
        if (exist != null) {
            userCollectionService.removeById(exist.getId());
            return Result.success(false);
        } else {
            UserCollection collection = new UserCollection();
            collection.setUserId(userId);
            collection.setTargetId(id);
            collection.setTargetType(3);
            userCollectionService.save(collection);
            return Result.success(true);
        }
    }

    @PostMapping("/{id}/comment")
    @Operation(summary = "发表评论")
    public Result<?> addComment(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            String content = (String) params.get("content");
            Number parentIdNum = (Number) params.get("parentId");
            Long parentId = parentIdNum != null ? parentIdNum.longValue() : 0L;
            
            Number replyToIdNum = (Number) params.get("replyToId");
            Long replyToId = replyToIdNum != null ? replyToIdNum.longValue() : null;
            
            CommentVO comment = strategyCommentService.addComment(id, userId, content, parentId, replyToId);
            return Result.success(comment);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "Error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "获取攻略评论列表")
    public Result<List<CommentVO>> getComments(@PathVariable Long id, @RequestParam(defaultValue = "latest") String sort) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        List<CommentVO> comments = strategyCommentService.getCommentsByStrategyId(id, userId, sort);
        return Result.success(comments);
    }

    @PostMapping("/comment/{id}/like")
    @Operation(summary = "点赞/取消点赞评论")
    public Result<Boolean> toggleCommentLike(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean isLiked = strategyCommentService.toggleCommentLike(id, userId);
        return Result.success(isLiked);
    }

    @DeleteMapping("/comment/{id}")
    @Operation(summary = "删除自己的评论")
    public Result<Void> deleteComment(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        strategyCommentService.deleteComment(id, userId);
        return Result.success();
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "获取当前用户的交互状态")
    public Result<Map<String, Boolean>> getInteractionStatus(@PathVariable Long id) {
        Map<String, Boolean> status = new HashMap<>();
        status.put("hasLiked", false);
        status.put("hasCollected", false);
        
        if (StpUtil.isLogin()) {
            long userId = StpUtil.getLoginIdAsLong();
            
            // Check like
            LambdaQueryWrapper<StrategyLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(StrategyLike::getStrategyId, id).eq(StrategyLike::getUserId, userId);
            status.put("hasLiked", strategyLikeService.count(likeQuery) > 0);
            
            // Check collect
            LambdaQueryWrapper<UserCollection> collectQuery = new LambdaQueryWrapper<>();
            collectQuery.eq(UserCollection::getTargetId, id)
                        .eq(UserCollection::getTargetType, 3)
                        .eq(UserCollection::getUserId, userId);
            status.put("hasCollected", userCollectionService.count(collectQuery) > 0);
        }
        
        return Result.success(status);
    }
}
