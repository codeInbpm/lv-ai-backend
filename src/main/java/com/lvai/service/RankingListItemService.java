package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.RankingListItem;
import java.util.List;

public interface RankingListItemService extends IService<RankingListItem> {
    List<RankingListItem> getItemsByRankingId(Long rankingId);
}
