package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.dto.CompanionChatDTO;
import com.lvai.entity.AiChatHistory;
import com.lvai.entity.TravelPlan;
import com.lvai.service.IAiChatHistoryService;
import com.lvai.service.IAiCompanionService;
import com.lvai.service.ITravelPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiCompanionServiceImpl implements IAiCompanionService {

    @Value("${ai.provider}")
    private String aiProvider;

    private final ChatModel openAiChatModel;
    private final ChatModel dashscopeChatModel;
    private final ChatModel deepSeekChatModel;
    private final IAiChatHistoryService chatHistoryService;
    private final ITravelPlanService travelPlanService;

    public AiCompanionServiceImpl(@Qualifier("openAiChatModel") ChatModel openAiChatModel,
                                  @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,
                                  @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel,
                                  IAiChatHistoryService chatHistoryService,
                                  ITravelPlanService travelPlanService) {
        this.openAiChatModel = openAiChatModel;
        this.dashscopeChatModel = dashscopeChatModel;
        this.deepSeekChatModel = deepSeekChatModel;
        this.chatHistoryService = chatHistoryService;
        this.travelPlanService = travelPlanService;
    }

    private ChatModel getChatModel() {
        return switch (aiProvider.toLowerCase()) {
            case "qwen" -> dashscopeChatModel;
            case "deepseek" -> deepSeekChatModel;
            default -> openAiChatModel;
        };
    }

    @Override
    public String chat(CompanionChatDTO dto, Long userId) {
        String sessionId = dto.getSessionId();
        
        // 1. Build context
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("你是「旅途AI」的智能旅行伴侣，负责在旅行中解答用户疑问、推荐附近美食景点、提供避坑建议。");
        if (dto.getPlanId() != null) {
            TravelPlan plan = travelPlanService.getById(dto.getPlanId());
            if (plan != null) {
                systemPrompt.append("\n当前用户正在执行行程：").append(plan.getTitle());
                systemPrompt.append("\n行程细节：").append(plan.getDescription());
            }
        }
        if (dto.getLocation() != null) {
            systemPrompt.append("\n用户当前位置：").append(dto.getLocation());
        }
        
        // 2. Load History
        List<AiChatHistory> historyList = chatHistoryService.list(
                new LambdaQueryWrapper<AiChatHistory>()
                        .eq(AiChatHistory::getSessionId, sessionId)
                        .orderByAsc(AiChatHistory::getCreateTime)
                        .last("limit 10")
        );
        
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt.toString()));
        for (AiChatHistory h : historyList) {
            if ("user".equals(h.getRole())) {
                messages.add(new UserMessage(h.getContent()));
            } else {
                messages.add(new AssistantMessage(h.getContent()));
            }
        }
        messages.add(new UserMessage(dto.getMessage()));
        
        // Save user message
        AiChatHistory userHistory = new AiChatHistory();
        userHistory.setSessionId(sessionId);
        userHistory.setUserId(userId);
        userHistory.setRole("user");
        userHistory.setContent(dto.getMessage());
        chatHistoryService.save(userHistory);

        // 3. Call AI
        ChatModel chatModel = getChatModel();
        ChatResponse response = chatModel.call(new Prompt(messages));
        String answer = response.getResult().getOutput().getText();
        Integer tokens = response.getMetadata() != null && response.getMetadata().getUsage() != null ? 
                         Math.toIntExact(response.getMetadata().getUsage().getTotalTokens()) : 0;

        // Save assistant message
        AiChatHistory assistantHistory = new AiChatHistory();
        assistantHistory.setSessionId(sessionId);
        assistantHistory.setUserId(userId);
        assistantHistory.setRole("assistant");
        assistantHistory.setContent(answer);
        assistantHistory.setTokens(tokens);
        chatHistoryService.save(assistantHistory);

        return answer;
    }
}