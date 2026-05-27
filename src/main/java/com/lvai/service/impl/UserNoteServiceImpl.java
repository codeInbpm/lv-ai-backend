package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.StrategyLike;
import com.lvai.entity.StrategyPost;
import com.lvai.entity.UserNote;
import com.lvai.mapper.StrategyLikeMapper;
import com.lvai.mapper.StrategyPostMapper;
import com.lvai.mapper.UserNoteMapper;
import com.lvai.entity.UserCollection;
import com.lvai.mapper.UserCollectionMapper;
import com.lvai.service.IUserNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import com.lvai.event.UserBehaviorEvent;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNoteServiceImpl extends ServiceImpl<UserNoteMapper, UserNote> implements IUserNoteService {

    private final StrategyLikeMapper strategyLikeMapper;
    private final StrategyPostMapper strategyPostMapper;
    private final UserCollectionMapper userCollectionMapper;
    private final com.lvai.mapper.NoteLikeMapper noteLikeMapper;
    private final com.lvai.mapper.NoteCommentMapper noteCommentMapper;
    private final com.lvai.mapper.NoteCommentLikeMapper noteCommentLikeMapper;
    private final com.lvai.service.IFileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UserNote publishNote(Long userId, UserNote note) {
        note.setUserId(userId);
        note.setStatus(1);
        note.setIsFeatured(0);
        note.setLikeCount(0);
        note.setCommentCount(0);
        this.save(note);
        
        // 发布事件
        if (note.getLocationName() != null && !note.getLocationName().trim().isEmpty()) {
            eventPublisher.publishEvent(new UserBehaviorEvent(this, note.getLocationName(), "NOTE"));
        }
        
        return note;
    }

    @Override
    public Page<UserNote> getMyNotes(Long userId, int page, int size) {
        Page<UserNote> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserNote> query = new LambdaQueryWrapper<>();
        query.eq(UserNote::getUserId, userId).orderByDesc(UserNote::getCreateTime);
        return this.page(pageParam, query);
    }

    @Override
    public Page<StrategyPost> getMyLiked(Long userId, int page, int size) {
        // Find liked strategies
        LambdaQueryWrapper<StrategyLike> likeQuery = new LambdaQueryWrapper<>();
        likeQuery.eq(StrategyLike::getUserId, userId).orderByDesc(StrategyLike::getCreateTime);
        
        Page<StrategyLike> likePage = strategyLikeMapper.selectPage(new Page<>(page, size), likeQuery);
        
        List<Long> strategyIds = likePage.getRecords().stream()
                .map(StrategyLike::getStrategyId)
                .collect(Collectors.toList());
                
        Page<StrategyPost> resultPage = new Page<>(page, size);
        resultPage.setTotal(likePage.getTotal());
        
        if (strategyIds.isEmpty()) {
            return resultPage;
        }
        
        List<StrategyPost> posts = strategyPostMapper.selectBatchIds(strategyIds);
        resultPage.setRecords(posts);
        return resultPage;
    }

    @Override
    public Page<StrategyPost> getMyCollected(Long userId, int page, int size) {
        LambdaQueryWrapper<UserCollection> query = new LambdaQueryWrapper<>();
        query.eq(UserCollection::getUserId, userId)
             .eq(UserCollection::getTargetType, 3)
             .orderByDesc(UserCollection::getCreateTime);
             
        Page<UserCollection> collectPage = userCollectionMapper.selectPage(new Page<>(page, size), query);
        
        List<Long> strategyIds = collectPage.getRecords().stream()
                .map(UserCollection::getTargetId)
                .collect(Collectors.toList());
                
        Page<StrategyPost> resultPage = new Page<>(page, size);
        resultPage.setTotal(collectPage.getTotal());
        
        if (strategyIds.isEmpty()) {
            return resultPage;
        }
        
        List<StrategyPost> posts = strategyPostMapper.selectBatchIds(strategyIds);
        resultPage.setRecords(posts);
        return resultPage;
    }
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteNoteWithFiles(Long noteId) {
        UserNote note = this.getById(noteId);
        if (note != null) {
            // 1. 删除点赞记录
            noteLikeMapper.delete(new LambdaQueryWrapper<com.lvai.entity.NoteLike>().eq(com.lvai.entity.NoteLike::getNoteId, noteId));
            
            // 2. 删除评论及评论点赞
            List<com.lvai.entity.NoteComment> comments = noteCommentMapper.selectList(new LambdaQueryWrapper<com.lvai.entity.NoteComment>().eq(com.lvai.entity.NoteComment::getNoteId, noteId));
            if (!comments.isEmpty()) {
                List<Long> commentIds = comments.stream().map(com.lvai.entity.NoteComment::getId).collect(Collectors.toList());
                noteCommentLikeMapper.delete(new LambdaQueryWrapper<com.lvai.entity.NoteCommentLike>().in(com.lvai.entity.NoteCommentLike::getCommentId, commentIds));
                noteCommentMapper.deleteBatchIds(commentIds);
            }

            // 3. 删除 MinIO 文件
            if (note.getCoverUrl() != null) {
                fileService.deleteFile(note.getCoverUrl());
            }
            if (note.getImages() != null) {
                try {
                    List<String> images = com.alibaba.fastjson2.JSON.parseArray(note.getImages(), String.class);
                    if (images != null) {
                        images.forEach(fileService::deleteFile);
                    }
                } catch (Exception e) {}
            }
            
            // 4. 最后删除笔记记录 (不删除 UserCollection 记录，以便前端显示“已删除”)
            this.removeById(noteId);
        }
    }
}
