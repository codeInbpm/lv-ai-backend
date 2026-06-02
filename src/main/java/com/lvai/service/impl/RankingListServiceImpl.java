package com.lvai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.RankingList;
import com.lvai.mapper.RankingListMapper;
import com.lvai.service.RankingListService;
import org.springframework.stereotype.Service;

@Service
public class RankingListServiceImpl extends ServiceImpl<RankingListMapper, RankingList> implements RankingListService {
}
