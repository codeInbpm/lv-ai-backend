package com.lvai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.common.BusinessException;
import com.lvai.entity.StrategyPost;
import com.lvai.mapper.StrategyPostMapper;
import com.lvai.service.IStrategyPostService;
import org.springframework.stereotype.Service;

@Service
public class StrategyPostServiceImpl extends ServiceImpl<StrategyPostMapper, StrategyPost> implements IStrategyPostService {

    @Override
    public Page<StrategyPost> getStrategies(int page, int size, String source, String keyword) {
        Page<StrategyPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<StrategyPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrategyPost::getStatus, 1);
        
        if (StrUtil.isNotBlank(source) && !"all".equals(source)) {
            if ("external".equals(source)) {
                wrapper.ne(StrategyPost::getSource, "internal");
            } else {
                wrapper.eq(StrategyPost::getSource, source);
            }
        }

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(StrategyPost::getTitle, keyword).or().like(StrategyPost::getDestination, keyword));
        }

        // 排序规则：热度优先，然后最新
        wrapper.orderByDesc(StrategyPost::getLikeCount)
               .orderByDesc(StrategyPost::getCreateTime);

        return page(pageParam, wrapper);
    }

    @Override
    public StrategyPost getStrategyDetail(Long id) {
        StrategyPost post = getById(id);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException("攻略不存在或已下架");
        }
        post.setViewCount(post.getViewCount() + 1);
        updateById(post);
        return post;
    }
}
