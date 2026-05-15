package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.NoteComment;
import com.lvai.entity.NoteCommentLike;
import com.lvai.entity.User;
import com.lvai.entity.UserNote;
import com.lvai.mapper.*;
import com.lvai.service.INoteCommentService;
import com.lvai.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteCommentServiceImpl extends ServiceImpl<NoteCommentMapper, NoteComment> implements INoteCommentService {

    private final UserMapper userMapper;
    private final NoteCommentLikeMapper noteCommentLikeMapper;
    private final UserNoteMapper userNoteMapper;

    @Override
    @Transactional
    public CommentVO addComment(Long noteId, Long userId, String content, Long parentId, Long replyToId) {
        NoteComment comment = new NoteComment();
        comment.setNoteId(noteId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setLikeCount(0);
        this.save(comment);

        // 更新笔记评论数
        UserNote note = userNoteMapper.selectById(noteId);
        if (note != null) {
            note.setCommentCount((note.getCommentCount() == null ? 0 : note.getCommentCount()) + 1);
            userNoteMapper.updateById(note);
        }

        return convertToVO(comment, userId);
    }

    @Override
    public List<CommentVO> getCommentsByNoteId(Long noteId, Long userId, String sort) {
        List<NoteComment> comments = this.list(new LambdaQueryWrapper<NoteComment>()
                .eq(NoteComment::getNoteId, noteId)
                .orderByDesc(sort.equals("latest"), NoteComment::getCreateTime)
                .orderByDesc(sort.equals("hot"), NoteComment::getLikeCount));

        if (comments.isEmpty()) return new ArrayList<>();

        List<Long> userIds = comments.stream().map(NoteComment::getUserId).collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        List<Long> commentIds = comments.stream().map(NoteComment::getId).collect(Collectors.toList());
        List<NoteCommentLike> likes = new ArrayList<>();
        if (userId != null) {
            likes = noteCommentLikeMapper.selectList(new LambdaQueryWrapper<NoteCommentLike>()
                    .in(NoteCommentLike::getCommentId, commentIds)
                    .eq(NoteCommentLike::getUserId, userId));
        }
        List<Long> likedCommentIds = likes.stream().map(NoteCommentLike::getCommentId).collect(Collectors.toList());

        List<CommentVO> allVOs = comments.stream().map(c -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(c, vo);
            User u = userMap.get(c.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
            }
            vo.setHasLiked(likedCommentIds.contains(c.getId()));
            return vo;
        }).collect(Collectors.toList());

        // 组装树形结构 (简单两层：主评论 + 回复)
        List<CommentVO> rootComments = allVOs.stream()
                .filter(v -> v.getParentId() == null || v.getParentId() == 0)
                .collect(Collectors.toList());
        
        for (CommentVO root : rootComments) {
            List<CommentVO> children = allVOs.stream()
                    .filter(v -> root.getId().equals(v.getParentId()))
                    .collect(Collectors.toList());
            root.setChildren(children);
        }

        return rootComments;
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(Long commentId, Long userId) {
        LambdaQueryWrapper<NoteCommentLike> query = new LambdaQueryWrapper<>();
        query.eq(NoteCommentLike::getCommentId, commentId).eq(NoteCommentLike::getUserId, userId);
        NoteCommentLike exist = noteCommentLikeMapper.selectOne(query);
        
        NoteComment comment = this.getById(commentId);
        if (comment == null) return false;

        if (exist != null) {
            noteCommentLikeMapper.deleteById(exist.getId());
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            this.updateById(comment);
            return false;
        } else {
            NoteCommentLike like = new NoteCommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            noteCommentLikeMapper.insert(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            this.updateById(comment);
            return true;
        }
    }

    private CommentVO convertToVO(NoteComment comment, Long currentUserId) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        User u = userMapper.selectById(comment.getUserId());
        if (u != null) {
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
        }
        vo.setHasLiked(false); // 新增评论默认未点赞
        return vo;
    }
}
