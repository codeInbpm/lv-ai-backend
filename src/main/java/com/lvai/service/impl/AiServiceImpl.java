package com.lvai.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.common.BusinessException;
import com.lvai.dto.CreatePlanDTO;
import com.lvai.entity.AiGenerationLog;
import com.lvai.mapper.AiGenerationLogMapper;
import com.lvai.service.IAiService;
import com.lvai.vo.AiPlanResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiServiceImpl extends ServiceImpl<AiGenerationLogMapper, AiGenerationLog> implements IAiService {

    @Value("${ai.provider}")
    private String aiProvider;

    private final ChatModel openAiChatModel;
    private final ChatModel dashscopeChatModel;
    private final ChatModel deepSeekChatModel;

    public AiServiceImpl(AiGenerationLogMapper aiGenerationLogMapper,
                         @Qualifier("openAiChatModel") ChatModel openAiChatModel,
                         @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,
                         @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel) {
        this.openAiChatModel = openAiChatModel;
        this.dashscopeChatModel = dashscopeChatModel;
        this.deepSeekChatModel = deepSeekChatModel;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiPlanResultVO generateTravelPlan(CreatePlanDTO dto, Long userId) {
        String systemPrompt = loadSystemPrompt();
        String userInput = buildUserInput(dto);
        String modelName = getActiveModelName();

        AiGenerationLog logEntity = new AiGenerationLog();
        logEntity.setUserId(userId);
        logEntity.setSystemPrompt(systemPrompt);
        logEntity.setUserInput(userInput);
        logEntity.setProvider(aiProvider);
        logEntity.setModel(modelName);
        logEntity.setStatus(0);
        save(logEntity);

        long startMs = System.currentTimeMillis();
        try {
            ChatModel chatModel = getChatModel();
            Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userInput)));

            log.info("AI 开始规划行程: [{}], User: {}", modelName, userId);
            ChatResponse chatResponse = chatModel.call(prompt);

            String content = "";
            String reasoning = "";
            if (chatResponse != null && chatResponse.getResult() != null) {
                AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
                content = assistantMessage.getText();
                if (assistantMessage.getMetadata() != null) {
                    reasoning = (String) assistantMessage.getMetadata().getOrDefault("reasoning_content", "");
                }
            }

            long costMs = System.currentTimeMillis() - startMs;
            logEntity.setAiOutput(content);
            logEntity.setReasoningContent(reasoning);
            logEntity.setStatus(1);
            logEntity.setCostMs(costMs);

            if (chatResponse != null && chatResponse.getMetadata() != null && chatResponse.getMetadata().getUsage() != null) {
                logEntity.setInputTokens(Math.toIntExact(chatResponse.getMetadata().getUsage().getPromptTokens()));
                logEntity.setOutputTokens(Math.toIntExact(chatResponse.getMetadata().getUsage().getTotalTokens()));
            }
            updateById(logEntity);

            return parseAiOutput(content, logEntity.getId());

        } catch (Exception e) {
            log.error("AI 行程生成失败", e);
            logEntity.setStatus(2);
            logEntity.setErrorMsg(e.getMessage());
            logEntity.setCostMs(System.currentTimeMillis() - startMs);
            updateById(logEntity);
            throw new BusinessException("AI 生成失败: " + e.getMessage());
        }
    }

    private ChatModel getChatModel() {
        ChatModel model = switch (aiProvider.toLowerCase()) {
            case "qwen" -> dashscopeChatModel;
            case "deepseek" -> deepSeekChatModel;
            default -> openAiChatModel;
        };
        if (model == null) {
            throw new BusinessException("AI 模型服务未启动: " + aiProvider);
        }
        return model;
    }

    private String getActiveModelName() {
        return switch (aiProvider.toLowerCase()) {
            case "qwen" -> "qwen-max";
            case "deepseek" -> "deepseek-v4-pro";
            default -> "gpt-4o-mini";
        };
    }

    private String loadSystemPrompt() {
        try {
            return ResourceUtil.readUtf8Str("prompt/travel-system-prompt.txt");
        } catch (Exception e) {
            log.warn("读取系统提示词失败，使用默认提示词");
            return getDefaultSystemPrompt();
        }
    }

    private String buildUserInput(CreatePlanDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为我规划一次旅行：\n");
        sb.append("出发地：").append(dto.getDeparture()).append("\n");
        sb.append("目的地：").append(dto.getDestination()).append("\n");
        sb.append("出发日期：").append(dto.getStartDate()).append("\n");
        sb.append("天数：").append(dto.getDays()).append("天\n");
        if (dto.getBudget() != null) sb.append("总预算：").append(dto.getBudget()).append("元\n");
        if (dto.getPeopleCount() != null) sb.append("人数：").append(dto.getPeopleCount()).append("人\n");
        if (dto.getPreferences() != null && !dto.getPreferences().isEmpty()) {
            sb.append("偏好：").append(String.join("、", dto.getPreferences())).append("\n");
        }
        if (dto.getExtraNote() != null) sb.append("补充说明：").append(dto.getExtraNote()).append("\n");
        return sb.toString();
    }

    private AiPlanResultVO parseAiOutput(String aiOutput, Long logId) {
        String jsonStr = aiOutput;
        int start = aiOutput.indexOf("{");
        int end = aiOutput.lastIndexOf("}");
        if (start >= 0 && end > start) {
            jsonStr = aiOutput.substring(start, end + 1);
        }

        try {
            AiPlanResultVO result = JSON.parseObject(jsonStr, AiPlanResultVO.class);
            result.setAiLogId(logId);
            return result;
        } catch (Exception e) {
            log.warn("解析AI输出JSON失败，尝试手动构建: {} \n原始输出: {}", e.getMessage(), aiOutput);
            AiPlanResultVO result = new AiPlanResultVO();
            result.setTitle("AI生成行程");
            result.setDescription(aiOutput.substring(0, Math.min(500, aiOutput.length())));
            result.setDays(new ArrayList<>());
            result.setAiLogId(logId);
            return result;
        }
    }

    private String getDefaultSystemPrompt() {
        return """
                你是一个专业的旅游规划师... (省略部分以保持简洁)
                """;
    }
}
