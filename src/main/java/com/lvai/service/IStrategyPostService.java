package com.lvai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.StrategyPost;

public interface IStrategyPostService extends IService<StrategyPost> {
    Page<StrategyPost> getStrategies(int page, int size, String source, String keyword);
    StrategyPost getStrategyDetail(Long id);
}
