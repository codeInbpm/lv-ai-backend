package com.lvai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.DestinationFood;
import com.lvai.mapper.DestinationFoodMapper;
import com.lvai.service.IDestinationFoodService;
import org.springframework.stereotype.Service;

@Service
public class DestinationFoodServiceImpl extends ServiceImpl<DestinationFoodMapper, DestinationFood> implements IDestinationFoodService {
}
