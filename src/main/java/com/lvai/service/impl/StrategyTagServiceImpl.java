package com.lvai.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.StrategyTag;
import com.lvai.mapper.StrategyTagMapper;
import com.lvai.service.IStrategyTagService;
import org.springframework.stereotype.Service;

@Service
public class StrategyTagServiceImpl extends ServiceImpl<StrategyTagMapper, StrategyTag> implements IStrategyTagService {
}