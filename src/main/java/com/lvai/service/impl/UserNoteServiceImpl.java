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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNoteServiceImpl extends ServiceImpl<UserNoteMapper, UserNote> implements IUserNoteService {

    private final StrategyLikeMapper strategyLikeMapper;
    private final StrategyPostMapper strategyPostMapper;
    private final UserCollectionMapper userCollectionMapper;

    @Override
    public UserNote publishNote(Long userId, UserNote note) {
        note.setUserId(userId);
        note.setStatus(1);
        note.setIsFeatured(0);
        note.setLikeCount(0);
        note.setCommentCount(0);
        this.save(note);
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
}
