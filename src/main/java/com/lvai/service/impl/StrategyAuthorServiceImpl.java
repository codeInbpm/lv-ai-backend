package com.lvai.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.StrategyAuthor;
import com.lvai.mapper.StrategyAuthorMapper;
import com.lvai.service.IStrategyAuthorService;
import org.springframework.stereotype.Service;

@Service
public class StrategyAuthorServiceImpl extends ServiceImpl<StrategyAuthorMapper, StrategyAuthor> implements IStrategyAuthorService {
}