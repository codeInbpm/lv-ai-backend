package com.lvai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.TravelItem;
import com.lvai.mapper.TravelItemMapper;
import com.lvai.service.ITravelItemService;
import org.springframework.stereotype.Service;

@Service
public class TravelItemServiceImpl extends ServiceImpl<TravelItemMapper, TravelItem> implements ITravelItemService {
}
