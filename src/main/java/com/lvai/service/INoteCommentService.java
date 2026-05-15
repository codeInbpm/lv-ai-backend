package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.NoteComment;
import com.lvai.vo.CommentVO;

import java.util.List;

public interface INoteCommentService extends IService<NoteComment> {
    CommentVO addComment(Long noteId, Long userId, String content, Long parentId, Long replyToId);
    List<CommentVO> getCommentsByNoteId(Long noteId, Long userId, String sort);
    boolean toggleCommentLike(Long commentId, Long userId);
}
