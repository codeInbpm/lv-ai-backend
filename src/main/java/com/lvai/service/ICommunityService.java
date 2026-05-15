package com.lvai.service;

import com.lvai.dto.CommunityStatsDTO;
import com.lvai.entity.StrategyPost;
import java.util.List;

public interface ICommunityService {
    /**
     * 获取用户社区统计数据
     * @param userId 用户ID
     * @return 统计数据
     */
    CommunityStatsDTO getUserCommunityStats(Long userId);

    /**
     * 获取用户收藏的攻略
     */
    List<StrategyPost> getCollectedStrategies(Long userId);
}
