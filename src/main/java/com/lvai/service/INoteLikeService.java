package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.NoteLike;

public interface INoteLikeService extends IService<NoteLike> {
    boolean toggleLike(Long noteId, Long userId);
}
