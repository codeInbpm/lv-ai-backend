package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.NoteLike;
import com.lvai.entity.UserNote;
import com.lvai.mapper.NoteLikeMapper;
import com.lvai.mapper.UserNoteMapper;
import com.lvai.service.INoteLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteLikeServiceImpl extends ServiceImpl<NoteLikeMapper, NoteLike> implements INoteLikeService {

    private final UserNoteMapper userNoteMapper;

    @Override
    @Transactional
    public boolean toggleLike(Long noteId, Long userId) {
        LambdaQueryWrapper<NoteLike> query = new LambdaQueryWrapper<>();
        query.eq(NoteLike::getNoteId, noteId).eq(NoteLike::getUserId, userId);
        NoteLike exist = this.getOne(query);
        
        UserNote note = userNoteMapper.selectById(noteId);
        if (note == null) return false;

        if (exist != null) {
            this.removeById(exist.getId());
            note.setLikeCount(Math.max(0, note.getLikeCount() - 1));
            userNoteMapper.updateById(note);
            return false;
        } else {
            NoteLike like = new NoteLike();
            like.setNoteId(noteId);
            like.setUserId(userId);
            this.save(like);
            note.setLikeCount(note.getLikeCount() + 1);
            userNoteMapper.updateById(note);
            return true;
        }
    }
}
