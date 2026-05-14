package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.StrategyLike;
import com.lvai.entity.StrategyPost;
import com.lvai.mapper.StrategyLikeMapper;
import com.lvai.mapper.StrategyPostMapper;
import com.lvai.service.IStrategyLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StrategyLikeServiceImpl extends ServiceImpl<StrategyLikeMapper, StrategyLike> implements IStrategyLikeService {

    private final StrategyPostMapper strategyPostMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long strategyId, Long userId) {
        LambdaQueryWrapper<StrategyLike> query = new LambdaQueryWrapper<>();
        query.eq(StrategyLike::getStrategyId, strategyId).eq(StrategyLike::getUserId, userId);
        StrategyLike exist = this.getOne(query);

        StrategyPost post = strategyPostMapper.selectById(strategyId);
        if (post == null) {
            throw new RuntimeException("攻略不存在");
        }

        if (exist != null) {
            // 已点赞，执行取消
            this.removeById(exist.getId());
            post.setLikeCount(Math.max(0, (post.getLikeCount() == null ? 0 : post.getLikeCount()) - 1));
            strategyPostMapper.updateById(post);
            return false;
        } else {
            // 未点赞，执行点赞
            StrategyLike like = new StrategyLike();
            like.setStrategyId(strategyId);
            like.setUserId(userId);
            this.save(like);
            post.setLikeCount((post.getLikeCount() == null ? 0 : post.getLikeCount()) + 1);
            strategyPostMapper.updateById(post);
            return true;
        }
    }
}
