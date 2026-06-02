package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.SearchHotWord;
import java.util.List;

public interface SearchHotWordService extends IService<SearchHotWord> {
    List<SearchHotWord> getHotWords(int limit);
}
