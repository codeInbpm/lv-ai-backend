package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.StrategyLike;

public interface IStrategyLikeService extends IService<StrategyLike> {
    /**
     * 切换点赞状态
     * @return true为点赞，false为取消点赞
     */
    boolean toggleLike(Long strategyId, Long userId);
}
