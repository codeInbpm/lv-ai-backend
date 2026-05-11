package com.lvai.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.common.BusinessException;
import com.lvai.dto.CreatePlanDTO;
import com.lvai.entity.AiGenerationLog;
import com.lvai.mapper.AiGenerationLogMapper;
import com.lvai.service.IAiService;
import com.lvai.vo.AiPlanResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl extends ServiceImpl<AiGenerationLogMapper, AiGenerationLog> implements IAiService {

    @Value("${ai.provider}")
    private String aiProvider;

    @Value("${ai.deepseek.api-key}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.base-url}")
    private String deepseekBaseUrl;

    @Value("${ai.deepseek.model}")
    private String deepseekModel;

    @Value("${ai.qwen.api-key}")
    private String qwenApiKey;

    @Value("${ai.qwen.base-url}")
    private String qwenBaseUrl;

    @Value("${ai.qwen.model}")
    private String qwenModel;

    @Value("${ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${ai.openai.base-url}")
    private String openaiBaseUrl;

    @Value("${ai.openai.model}")
    private String openaiModel;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public AiPlanResultVO generateTravelPlan(CreatePlanDTO dto, Long userId) {
        String systemPrompt = loadSystemPrompt();
        String userInput = buildUserInput(dto);

        long startMs = System.currentTimeMillis();
        AiGenerationLog logEntity = new AiGenerationLog();
        logEntity.setUserId(userId);
        logEntity.setSystemPrompt(systemPrompt);
        logEntity.setUserInput(userInput);

        try {
            String aiOutput = callAiApi(systemPrompt, userInput, logEntity);
            long costMs = System.currentTimeMillis() - startMs;

            logEntity.setAiOutput(aiOutput);
            logEntity.setStatus(1);
            logEntity.setCostMs(costMs);
            save(logEntity);

            return parseAiOutput(aiOutput, logEntity.getId());
        } catch (Exception e) {
            logEntity.setStatus(0);
            logEntity.setErrorMsg(e.getMessage());
            logEntity.setCostMs(System.currentTimeMillis() - startMs);
            save(logEntity);
            throw new BusinessException("AI生成失败: " + e.getMessage());
        }
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
        if (dto.getBudget() != null) {
            sb.append("总预算：").append(dto.getBudget()).append("元\n");
        }
        if (dto.getPeopleCount() != null) {
            sb.append("人数：").append(dto.getPeopleCount()).append("人\n");
        }
        if (dto.getPreferences() != null && !dto.getPreferences().isEmpty()) {
            sb.append("偏好：").append(String.join("、", dto.getPreferences())).append("\n");
        }
        if (dto.getExtraNote() != null) {
            sb.append("补充说明：").append(dto.getExtraNote()).append("\n");
        }
        return sb.toString();
    }

    private String callAiApi(String systemPrompt, String userInput, AiGenerationLog logEntity) throws IOException {
        String apiKey;
        String baseUrl;
        String model;

        switch (aiProvider.toLowerCase()) {
            case "qwen" -> { apiKey = qwenApiKey; baseUrl = qwenBaseUrl; model = qwenModel; }
            case "openai" -> { apiKey = openaiApiKey; baseUrl = openaiBaseUrl; model = openaiModel; }
            default -> { apiKey = deepseekApiKey; baseUrl = deepseekBaseUrl; model = deepseekModel; }
        }

        logEntity.setProvider(aiProvider);
        logEntity.setModel(model);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 4096);
        requestBody.put("temperature", 0.7);

        JSONArray messages = new JSONArray();
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userInput);
        messages.add(userMsg);
        requestBody.put("messages", messages);

        RequestBody body = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI接口请求失败: " + response.code());
            }
            String respStr = response.body().string();
            JSONObject respJson = JSON.parseObject(respStr);

            JSONObject usage = respJson.getJSONObject("usage");
            if (usage != null) {
                logEntity.setInputTokens(usage.getIntValue("prompt_tokens"));
                logEntity.setOutputTokens(usage.getIntValue("completion_tokens"));
            }

            return respJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    private AiPlanResultVO parseAiOutput(String aiOutput, Long logId) {
        // 尝试提取JSON内容
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
            log.warn("解析AI输出JSON失败，尝试手动构建: {}", e.getMessage());
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
                你是一个专业的旅游规划师。请根据用户提供的信息，生成一份详细的旅行行程规划。
                
                要求：
                1. 行程安排合理，时间分配科学
                2. 包含景点、美食、住宿、交通等各类行程项
                3. 提供实用的旅行Tips
                4. 考虑用户预算，给出费用估算
                
                请严格按照以下JSON格式输出（不要输出其他内容）：
                {
                  "title": "行程标题",
                  "description": "行程整体描述",
                  "days": [
                    {
                      "dayIndex": 1,
                      "title": "第一天标题",
                      "description": "当天概述",
                      "items": [
                        {
                          "sortOrder": 1,
                          "type": 1,
                          "name": "景点名称",
                          "address": "地址",
                          "lng": 116.397128,
                          "lat": 39.916527,
                          "startTime": "09:00",
                          "endTime": "11:00",
                          "duration": 120,
                          "estimatedCost": 100,
                          "description": "描述",
                          "tips": "实用Tips"
                        }
                      ]
                    }
                  ]
                }
                
                类型说明: 1=景点 2=美食 3=酒店 4=交通 5=购物 6=其他
                """;
    }
}
