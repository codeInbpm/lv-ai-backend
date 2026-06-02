package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.RankingListItem;
import com.lvai.mapper.RankingListItemMapper;
import com.lvai.service.RankingListItemService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RankingListItemServiceImpl extends ServiceImpl<RankingListItemMapper, RankingListItem> implements RankingListItemService {

    @Override
    public List<RankingListItem> getItemsByRankingId(Long rankingId) {
        LambdaQueryWrapper<RankingListItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RankingListItem::getRankingId, rankingId)
                .orderByAsc(RankingListItem::getRankNum);
        return this.list(queryWrapper);
    }
}
