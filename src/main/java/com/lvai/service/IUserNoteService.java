package com.lvai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.StrategyPost;
import com.lvai.entity.UserNote;

import com.lvai.vo.UserNotePublishVO;

public interface IUserNoteService extends IService<UserNote> {
    Long publishNote(Long userId, UserNotePublishVO publishVO);
    Page<UserNote> getMyNotes(Long userId, int page, int size);
    Page<StrategyPost> getMyLiked(Long userId, int page, int size);
    Page<StrategyPost> getMyCollected(Long userId, int page, int size);
    void deleteNoteWithFiles(Long noteId);
}
