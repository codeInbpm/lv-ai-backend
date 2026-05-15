package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.Result;
import com.lvai.entity.NoteLike;
import com.lvai.entity.UserCollection;
import com.lvai.service.INoteCommentService;
import com.lvai.service.INoteLikeService;
import com.lvai.service.IUserCollectionService;
import com.lvai.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "笔记交互模块")
@RestController
@RequestMapping("/note/interaction")
@RequiredArgsConstructor
public class NoteInteractionController {

    private final INoteLikeService noteLikeService;
    private final INoteCommentService noteCommentService;
    private final IUserCollectionService userCollectionService;

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞/取消点赞笔记")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean isLiked = noteLikeService.toggleLike(id, userId);
        return Result.success(isLiked);
    }

    @PostMapping("/{id}/collect")
    @Operation(summary = "收藏/取消收藏笔记")
    public Result<Boolean> toggleCollect(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<UserCollection> query = new LambdaQueryWrapper<>();
        query.eq(UserCollection::getTargetId, id)
             .eq(UserCollection::getTargetType, 1) // 1表示笔记
             .eq(UserCollection::getUserId, userId);
        
        UserCollection exist = userCollectionService.getOne(query);
        if (exist != null) {
            userCollectionService.removeById(exist.getId());
            return Result.success(false);
        } else {
            UserCollection collection = new UserCollection();
            collection.setUserId(userId);
            collection.setTargetId(id);
            collection.setTargetType(1);
            userCollectionService.save(collection);
            return Result.success(true);
        }
    }

    @PostMapping("/{id}/comment")
    @Operation(summary = "发表笔记评论")
    public Result<?> addComment(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        long userId = StpUtil.getLoginIdAsLong();
        String content = (String) params.get("content");
        Number parentIdNum = (Number) params.get("parentId");
        Long parentId = parentIdNum != null ? parentIdNum.longValue() : 0L;
        
        CommentVO comment = noteCommentService.addComment(id, userId, content, parentId, 0L);
        return Result.success(comment);
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "获取笔记评论列表")
    public Result<List<CommentVO>> getComments(@PathVariable Long id, @RequestParam(defaultValue = "latest") String sort) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        List<CommentVO> comments = noteCommentService.getCommentsByNoteId(id, userId, sort);
        return Result.success(comments);
    }

    @PostMapping("/comment/{id}/like")
    @Operation(summary = "点赞评论")
    public Result<Boolean> toggleCommentLike(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(noteCommentService.toggleCommentLike(id, userId));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "获取当前用户的笔记交互状态")
    public Result<Map<String, Boolean>> getInteractionStatus(@PathVariable Long id) {
        Map<String, Boolean> status = new HashMap<>();
        status.put("hasLiked", false);
        status.put("hasCollected", false);
        
        if (StpUtil.isLogin()) {
            long userId = StpUtil.getLoginIdAsLong();
            
            // Check like
            LambdaQueryWrapper<NoteLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(NoteLike::getNoteId, id).eq(NoteLike::getUserId, userId);
            status.put("hasLiked", noteLikeService.count(likeQuery) > 0);
            
            // Check collect
            LambdaQueryWrapper<UserCollection> collectQuery = new LambdaQueryWrapper<>();
            collectQuery.eq(UserCollection::getTargetId, id)
                        .eq(UserCollection::getTargetType, 1)
                        .eq(UserCollection::getUserId, userId);
            status.put("hasCollected", userCollectionService.count(collectQuery) > 0);
        }
        
        return Result.success(status);
    }
}
