package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.StrategyComment;
import com.lvai.entity.StrategyPost;
import com.lvai.mapper.StrategyCommentMapper;
import com.lvai.mapper.StrategyPostMapper;
import com.lvai.service.IStrategyCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.lvai.entity.User;
import com.lvai.mapper.UserMapper;
import com.lvai.vo.CommentVO;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class StrategyCommentServiceImpl extends ServiceImpl<StrategyCommentMapper, StrategyComment> implements IStrategyCommentService {

    private final StrategyPostMapper strategyPostMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO addComment(Long strategyId, Long userId, String content, Long parentId, Long replyToId) {
        StrategyPost post = strategyPostMapper.selectById(strategyId);
        if (post == null) {
            throw new RuntimeException("攻略不存在");
        }

        StrategyComment comment = new StrategyComment();
        comment.setStrategyId(strategyId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId == null ? 0L : parentId);
        comment.setLikeCount(0);
        
        if (replyToId != null) {
            comment.setReplyToId(replyToId);
            StrategyComment replyTarget = this.getById(replyToId);
            if (replyTarget != null) {
                comment.setReplyToUserId(replyTarget.getUserId());
            }
        }
        
        this.save(comment);

        post.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
        strategyPostMapper.updateById(post);

        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());
        vo.setUserId(userId);
        vo.setParentId(comment.getParentId());
        vo.setReplyToId(comment.getReplyToId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setLikeCount(0);
        vo.setChildren(new ArrayList<>());
        
        User user = userMapper.selectById(userId);
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        
        if (comment.getReplyToUserId() != null) {
            User replyUser = userMapper.selectById(comment.getReplyToUserId());
            if (replyUser != null) {
                vo.setReplyToNickname(replyUser.getNickname());
            }
        }

        return vo;
    }

    @Override
    public List<CommentVO> getCommentsByStrategyId(Long strategyId) {
        LambdaQueryWrapper<StrategyComment> query = new LambdaQueryWrapper<>();
        query.eq(StrategyComment::getStrategyId, strategyId)
             .orderByAsc(StrategyComment::getCreateTime); // ASC so we can sort main by DESC later, or just sort in memory
        List<StrategyComment> list = this.list(query);
        
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Fetch all users to avoid N+1
        List<Long> userIds = list.stream().map(StrategyComment::getUserId).distinct().collect(Collectors.toList());
        List<Long> replyUserIds = list.stream().map(StrategyComment::getReplyToUserId).filter(id -> id != null).distinct().collect(Collectors.toList());
        userIds.addAll(replyUserIds);
        userIds = userIds.stream().distinct().collect(Collectors.toList());
        
        java.util.Map<Long, User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }
        
        List<CommentVO> allVos = list.stream().map(c -> {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setContent(c.getContent());
            vo.setCreateTime(c.getCreateTime());
            vo.setUserId(c.getUserId());
            vo.setParentId(c.getParentId() == null ? 0L : c.getParentId());
            vo.setReplyToId(c.getReplyToId());
            vo.setReplyToUserId(c.getReplyToUserId());
            vo.setLikeCount(c.getLikeCount() == null ? 0 : c.getLikeCount());
            vo.setChildren(new ArrayList<>());
            
            User user = userMap.get(c.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            if (c.getReplyToUserId() != null) {
                User replyUser = userMap.get(c.getReplyToUserId());
                if (replyUser != null) {
                    vo.setReplyToNickname(replyUser.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList());
        
        // Build 2-level tree (Bilibili style)
        java.util.Map<Long, CommentVO> mainCommentMap = new java.util.LinkedHashMap<>();
        for (CommentVO vo : allVos) {
            if (vo.getParentId() == 0L) {
                mainCommentMap.put(vo.getId(), vo);
            }
        }
        
        for (CommentVO vo : allVos) {
            if (vo.getParentId() != 0L) {
                CommentVO parent = mainCommentMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        
        List<CommentVO> result = new ArrayList<>(mainCommentMap.values());
        // Sort main comments by createTime DESC
        result.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return result;
    }
}
