package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.SearchHotWord;
import com.lvai.mapper.SearchHotWordMapper;
import com.lvai.service.SearchHotWordService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SearchHotWordServiceImpl extends ServiceImpl<SearchHotWordMapper, SearchHotWord> implements SearchHotWordService {

    @Override
    public List<SearchHotWord> getHotWords(int limit) {
        LambdaQueryWrapper<SearchHotWord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SearchHotWord::getSortOrder)
                .orderByDesc(SearchHotWord::getHeatScore)
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }
}
