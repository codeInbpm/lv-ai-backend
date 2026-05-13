package com.lvai.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.AiChatHistory;
import com.lvai.mapper.AiChatHistoryMapper;
import com.lvai.service.IAiChatHistoryService;
import org.springframework.stereotype.Service;

@Service
public class AiChatHistoryServiceImpl extends ServiceImpl<AiChatHistoryMapper, AiChatHistory> implements IAiChatHistoryService {
}