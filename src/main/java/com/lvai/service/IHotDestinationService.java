package com.lvai.service;

import com.lvai.entity.Destination;
import java.util.List;

public interface IHotDestinationService {
    /**
     * 计算单个目的地的热度分数
     * @param destinationId 目的地ID
     */
    void calculateHotScore(Long destinationId);

    /**
     * 获取热门目的地列表
     * @param limit 限制数量
     * @return 热门目的地列表
     */
    List<Destination> getHotDestinations(int limit);
}
