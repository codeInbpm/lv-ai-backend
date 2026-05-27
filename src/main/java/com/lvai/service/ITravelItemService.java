package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.TravelItem;

public interface ITravelItemService extends IService<TravelItem> {
    void reorganizeTimeline(Long dayId);
}
