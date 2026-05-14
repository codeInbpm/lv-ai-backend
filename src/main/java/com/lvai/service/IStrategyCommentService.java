package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.StrategyComment;
import com.lvai.vo.CommentVO;
import java.util.List;

public interface IStrategyCommentService extends IService<StrategyComment> {
    CommentVO addComment(Long strategyId, Long userId, String content, Long parentId, Long replyToId);
    List<CommentVO> getCommentsByStrategyId(Long strategyId, Long userId, String sort);
    boolean toggleCommentLike(Long commentId, Long userId);
    void deleteComment(Long commentId, Long userId);
}
