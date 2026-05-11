package com.lvai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.TravelDay;
import com.lvai.mapper.TravelDayMapper;
import com.lvai.service.ITravelDayService;
import org.springframework.stereotype.Service;

@Service
public class TravelDayServiceImpl extends ServiceImpl<TravelDayMapper, TravelDay> implements ITravelDayService {
}
