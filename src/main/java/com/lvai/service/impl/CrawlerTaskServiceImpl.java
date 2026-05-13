package com.lvai.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.CrawlerTask;
import com.lvai.mapper.CrawlerTaskMapper;
import com.lvai.service.ICrawlerTaskService;
import org.springframework.stereotype.Service;

@Service
public class CrawlerTaskServiceImpl extends ServiceImpl<CrawlerTaskMapper, CrawlerTask> implements ICrawlerTaskService {
}