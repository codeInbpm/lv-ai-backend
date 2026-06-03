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
import com.lvai.vo.UserNotePublishVO;
import com.lvai.common.BusinessException;
import com.lvai.entity.ContentDraft;
import com.lvai.service.IContentDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import com.lvai.event.UserBehaviorEvent;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

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
    private final IContentDraftService draftService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishNote(Long userId, UserNotePublishVO publishVO) {
        String type = publishVO.getType();
        if (type == null) {
            throw new BusinessException("类型不能为空");
        }

        // 根据类型校验必填字段
        if ("guide".equals(type)) {
            if (publishVO.getDays() == null || publishVO.getDays() <= 0) {
                throw new BusinessException("攻略天数必须大于0");
            }
            if (publishVO.getCost() == null || publishVO.getCost().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new BusinessException("人均花费必须大于等于0");
            }
            if (publishVO.getSeason() == null || publishVO.getSeason().trim().isEmpty()) {
                throw new BusinessException("适合季节不能为空");
            }
        } else if ("travel".equals(type)) {
            if (publishVO.getTripDate() == null) {
                throw new BusinessException("出行日期不能为空");
            }
            if (publishVO.getCompanions() == null || publishVO.getCompanions().trim().isEmpty()) {
                throw new BusinessException("同行伙伴不能为空");
            }
        }

        boolean isDraft = Boolean.TRUE.equals(publishVO.getIsDraft());

        // 正式发布攻略或游记时校验扩展数据 JSON 的合法性
        if (!isDraft && ("guide".equals(type) || "travel".equals(type))) {
            if (publishVO.getExtraData() == null || publishVO.getExtraData().trim().isEmpty()) {
                throw new BusinessException("结构化扩展数据不能为空");
            }
            try {
                com.alibaba.fastjson2.JSON.parseObject(publishVO.getExtraData());
            } catch (Exception e) {
                throw new BusinessException("结构化扩展数据JSON格式非法");
            }
        }

        // 构造或更新笔记记录
        UserNote note = null;
        if (publishVO.getId() != null) {
            note = this.getById(publishVO.getId());
            if (note != null && !note.getUserId().equals(userId)) {
                throw new BusinessException("无权修改该内容");
            }
        }

        if (note == null) {
            note = new UserNote();
            note.setUserId(userId);
            note.setLikeCount(0);
            note.setCommentCount(0);
            note.setIsFeatured(0);
        }

        note.setTitle(publishVO.getTitle());
        note.setContent(publishVO.getContent());
        note.setCoverUrl(publishVO.getCoverUrl());
        note.setImages(publishVO.getImages());
        note.setLocationName(publishVO.getLocationName());
        note.setLocationAddress(publishVO.getLocationAddress());
        note.setLongitude(publishVO.getLongitude());
        note.setLatitude(publishVO.getLatitude());
        note.setTopicTags(publishVO.getTopicTags());
        note.setType(type);

        // 设置类型专属字段并清理其它类型专属字段
        if ("guide".equals(type)) {
            note.setDays(publishVO.getDays());
            note.setCost(publishVO.getCost());
            note.setSeason(publishVO.getSeason());
            note.setTripDate(null);
            note.setCompanions(null);
        } else if ("travel".equals(type)) {
            note.setDays(null);
            note.setCost(null);
            note.setSeason(null);
            note.setTripDate(publishVO.getTripDate());
            note.setCompanions(publishVO.getCompanions());
        } else {
            note.setDays(null);
            note.setCost(null);
            note.setSeason(null);
            note.setTripDate(null);
            note.setCompanions(null);
        }

        note.setExtraData(publishVO.getExtraData());
        note.setStatus(isDraft ? 0 : 1);

        this.saveOrUpdate(note);

        // 处理草稿箱 content_draft 联动
        if (isDraft) {
            ContentDraft draft = null;
            if (publishVO.getDraftId() != null) {
                draft = draftService.getById(publishVO.getDraftId());
            }
            if (draft == null) {
                draft = new ContentDraft();
                draft.setCreateTime(java.time.LocalDateTime.now());
            }
            draft.setUserId(userId);
            
            // draftType 字段 (1=笔记 2=攻略 3=游记)
            int draftType = 1;
            if ("guide".equals(type)) {
                draftType = 2;
            } else if ("travel".equals(type)) {
                draftType = 3;
            }
            draft.setDraftType(draftType);
            draft.setTitle(publishVO.getTitle() != null && !publishVO.getTitle().trim().isEmpty() ? publishVO.getTitle() : "无标题草稿");

            // 把所有的发布数据包装成 JSON 字符串存入 content
            JSONObject draftJson = new JSONObject();
            draftJson.put("noteId", note.getId());
            draftJson.put("type", type);
            draftJson.put("title", publishVO.getTitle());
            draftJson.put("content", publishVO.getContent());
            draftJson.put("coverUrl", publishVO.getCoverUrl());
            if (publishVO.getImages() != null && !publishVO.getImages().trim().isEmpty()) {
                try {
                    draftJson.put("images", JSON.parseArray(publishVO.getImages()));
                } catch (Exception e) {
                    draftJson.put("images", publishVO.getImages());
                }
            }
            draftJson.put("locationName", publishVO.getLocationName());
            draftJson.put("locationAddress", publishVO.getLocationAddress());
            draftJson.put("longitude", publishVO.getLongitude());
            draftJson.put("latitude", publishVO.getLatitude());
            draftJson.put("topicTags", publishVO.getTopicTags());
            
            if ("guide".equals(type)) {
                draftJson.put("days", publishVO.getDays());
                draftJson.put("cost", publishVO.getCost());
                draftJson.put("season", publishVO.getSeason());
            } else if ("travel".equals(type)) {
                draftJson.put("tripDate", publishVO.getTripDate());
                draftJson.put("companions", publishVO.getCompanions());
            }
            draftJson.put("extraData", publishVO.getExtraData());

            draft.setContent(draftJson.toJSONString());
            draft.setUpdateTime(java.time.LocalDateTime.now());
            draftService.saveOrUpdate(draft);
        } else {
            // 正式发布：如果有草稿，清理
            if (publishVO.getDraftId() != null) {
                draftService.removeById(publishVO.getDraftId());
            }

            // 发布事件
            if (note.getLocationName() != null && !note.getLocationName().trim().isEmpty()) {
                eventPublisher.publishEvent(new UserBehaviorEvent(this, note.getLocationName(), "NOTE"));
            }
        }

        return note.getId();
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
