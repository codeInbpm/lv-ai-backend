package com.lvai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.BusinessException;
import com.lvai.entity.*;
import com.lvai.mapper.AiGenerationLogMapper;
import com.lvai.service.*;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiTravelCompanionServiceImpl implements IAiTravelCompanionService {

    @Value("${ai.provider}")
    private String aiProvider;

    private final ChatModel openAiChatModel;
    private final ChatModel dashscopeChatModel;
    private final ChatModel deepSeekChatModel;

    private final ITravelPlanService travelPlanService;
    private final ITravelDayService travelDayService;
    private final ITravelItemService travelItemService;
    private final ITravelExpenseService travelExpenseService;
    private final AiGenerationLogMapper aiGenerationLogMapper;

    public AiTravelCompanionServiceImpl(@Qualifier("openAiChatModel") ChatModel openAiChatModel,
                                         @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,
                                         @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel,
                                         ITravelPlanService travelPlanService,
                                         ITravelDayService travelDayService,
                                         ITravelItemService travelItemService,
                                         ITravelExpenseService travelExpenseService,
                                         AiGenerationLogMapper aiGenerationLogMapper) {
        this.openAiChatModel = openAiChatModel;
        this.dashscopeChatModel = dashscopeChatModel;
        this.deepSeekChatModel = deepSeekChatModel;
        this.travelPlanService = travelPlanService;
        this.travelDayService = travelDayService;
        this.travelItemService = travelItemService;
        this.travelExpenseService = travelExpenseService;
        this.aiGenerationLogMapper = aiGenerationLogMapper;
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

    @Override
    public String callAiForCheckin(Long itemId, String userInput, Long userId) {
        TravelItem item = travelItemService.getById(itemId);
        if (item == null) {
            throw new BusinessException("找不到该行程项目");
        }
        TravelPlan plan = travelPlanService.getById(item.getPlanId());
        if (plan == null) {
            throw new BusinessException("找不到关联的行程路线");
        }
        TravelDay day = travelDayService.getById(item.getDayId());
        if (day == null) {
            throw new BusinessException("找不到关联的行程日程天数");
        }

        // 计算本日已记账花费
        List<TravelExpense> dayExpenses = travelExpenseService.list(
                new LambdaQueryWrapper<TravelExpense>()
                        .eq(TravelExpense::getDayId, day.getId())
        );
        BigDecimal dayActualCost = dayExpenses.stream()
                .map(TravelExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 行程项类型
        String itemTypeStr = switch (item.getType()) {
            case 1 -> "交通";
            case 2 -> "住宿";
            case 3 -> "景点";
            case 4 -> "餐饮";
            case 5 -> "购物";
            default -> "其他/活动";
        };

        String itemDetail = String.format("明细名称：%s\n类型：%s\n预计花费：%s 元\n地址：%s\n建议时间：%s - %s\n描述：%s",
                item.getName(),
                itemTypeStr,
                item.getEstimatedCost() != null ? item.getEstimatedCost() : "0",
                item.getAddress() != null ? item.getAddress() : "",
                item.getStartTime() != null ? item.getStartTime() : "无",
                item.getEndTime() != null ? item.getEndTime() : "无",
                item.getDescription() != null ? item.getDescription() : "");

        String systemPrompt = """
你是一位贴心、专业的旅行财务与记录助手，正在帮助用户记录行程。

## 当前行程节点信息
{itemDetail}

## 当前用户偏好与历史
- 总体预算：{planBudget} 元
- 当前已记录总花费：{totalActualCost} 元
- 本日已花费：{dayActualCost} 元
- 用户兴趣标签：{preferences}

## 你的任务（多轮对话支持）
根据用户打卡时的输入（可能包含文字、语音、照片描述），完成以下工作：

1. **智能费用分类**：推荐最合适的费用类型（1餐饮 2住宿 3交通 4门票 5购物 6其他）。如果是景点打卡，强烈建议推荐 4门票；如果是餐饮打卡，推荐 1餐饮；如果是交通打卡，推荐 3交通；如果是酒店打卡，推荐 2住宿。
2. **费用金额建议**：如果用户没有提及具体金额，根据行程项本身的信息（如预计花费），给出推荐的合理估算金额。如果用户提及了具体金额（例如“今天花了50元”），请必须直接提取并以该金额返回。
3. **打卡文案润色**：帮用户生成温馨、有趣的打卡感想（50-120字，带适量的 emoji）。
4. **消费建议**：给出下一项可能的消费提醒或省钱Tips。
5. **预算预警**：根据用户的消费金额及预算执行状态（本日花费或总花费是否接近/超出预算），给出温和的财务提醒。

## 输出格式（严格以JSON格式输出，不要包含 ```json 或其它 markdown 标记，必须是可以直接解析的合法JSON对象）
{
  "explanation": "温馨总结文案（带emoji，展示给用户）",
  "suggestedExpense": {
    "amount": 128.5,
    "type": 1,
    "remark": "中午在当地吃俄式大列巴+红菜汤",
    "confidence": 0.92
  },
  "checkinNote": "今天在五大连池看到了梦幻的火山地貌，心情超级放松！边走边拍，空气都带着矿物质的清新味～",
  "budgetStatus": {
    "dayRemaining": 450,
    "totalRemaining": 1200,
    "warning": "今日餐饮已接近预算，晚上建议轻食"
  },
  "nextSuggestion": "下一个景点建议提前买门票，可以省20元"
}
""";

        // 替换 Prompt 中的占位符
        systemPrompt = systemPrompt
                .replace("{itemDetail}", itemDetail)
                .replace("{planBudget}", plan.getBudget() != null ? plan.getBudget().toString() : "未设置")
                .replace("{totalActualCost}", plan.getActualCost() != null ? plan.getActualCost().toString() : "0")
                .replace("{dayActualCost}", dayActualCost.toString())
                .replace("{preferences}", plan.getPreferences() != null ? plan.getPreferences() : "无");

        String userPrompt = userInput != null && !userInput.trim().isEmpty() ? userInput : "今天打卡签到成功！心情很好！";

        return callAiAndLog(systemPrompt, userPrompt, userId);
    }

    @Override
    public String generateDailySummary(Long planId, Long dayId, Long userId) {
        TravelPlan plan = travelPlanService.getById(planId);
        if (plan == null) {
            throw new BusinessException("找不到关联的行程路线");
        }
        TravelDay day = travelDayService.getById(dayId);
        if (day == null) {
            throw new BusinessException("找不到关联的行程日程天数");
        }

        // 今日去过哪些景点/节点（列表）
        List<TravelItem> items = travelItemService.list(
                new LambdaQueryWrapper<TravelItem>()
                        .eq(TravelItem::getDayId, dayId)
                        .orderByAsc(TravelItem::getSortOrder)
        );
        StringBuilder itemsSummary = new StringBuilder();
        for (TravelItem it : items) {
            String status = it.getCheckedIn() != null && it.getCheckedIn() == 1 ? "【已打卡签到】" : "【未签到】";
            itemsSummary.append(String.format("- %s %s (预计费用: %s, 实际花费: %s, 备注: %s)\n",
                    it.getName(), status,
                    it.getEstimatedCost() != null ? it.getEstimatedCost() : "0",
                    it.getActualCost() != null ? it.getActualCost() : "0",
                    it.getCheckInNote() != null ? it.getCheckInNote() : ""));
        }

        // 今日的记账明细
        List<TravelExpense> expenses = travelExpenseService.list(
                new LambdaQueryWrapper<TravelExpense>()
                        .eq(TravelExpense::getDayId, dayId)
        );
        StringBuilder expenseList = new StringBuilder();
        for (TravelExpense ex : expenses) {
            String typeStr = switch (ex.getType()) {
                case 1 -> "餐饮";
                case 2 -> "住宿";
                case 3 -> "交通";
                case 4 -> "门票";
                case 5 -> "购物";
                default -> "其他";
            };
            expenseList.append(String.format("- %s: %s 元 (备注: %s)\n", typeStr, ex.getAmount(), ex.getRemark()));
        }

        String systemPrompt = """
你是一个旅行日记与财务分析师。

行程名称：{planTitle}
第 {dayIndex} 天：{dayTitle}

今日行程节点：
{itemsSummary}

今日记账记录：
{expenseList}

请生成：
1. 一段生动、适合发朋友圈/小红书的当日总结（150-250字，富含emoji）
2. 今日消费结构分析（餐饮/住宿/交通/门票/购物/其他占比）
3. 今日消费的亮点或需要吐槽的点
4. 对明天的预算建议

输出JSON（严格以JSON格式输出，不要包含 ```json 或其它 markdown 标记，必须是可以直接解析的合法JSON对象）：
{
  "daySummary": "今天在黑河自驾游玩，感受了边境风情，体验非常棒...",
  "expenseAnalysis": {
    "total": 689,
    "breakdown": {"餐饮": 245, "交通": 180},
    "insight": "今天餐饮花费较高，主要因为尝试了当地特色俄餐"
  },
  "moodSuggestion": "整体节奏适中，明天可以多安排一些拍照时间",
  "shareReady": true
}
""";

        // 替换 Prompt 中的占位符
        systemPrompt = systemPrompt
                .replace("{planTitle}", plan.getTitle())
                .replace("{dayIndex}", String.valueOf(day.getDayIndex()))
                .replace("{dayTitle}", day.getTitle() != null ? day.getTitle() : "我的旅程")
                .replace("{itemsSummary}", itemsSummary.length() > 0 ? itemsSummary.toString() : "今天暂无打卡节点")
                .replace("{expenseList}", expenseList.length() > 0 ? expenseList.toString() : "今天暂无记账明细");

        String userPrompt = "帮我生成今日的消费分析与旅行日记总结。";

        return callAiAndLog(systemPrompt, userPrompt, userId);
    }

    private String callAiAndLog(String systemPrompt, String userInput, Long userId) {
        String modelName = getActiveModelName();
        AiGenerationLog logEntity = new AiGenerationLog();
        logEntity.setUserId(userId);
        logEntity.setSystemPrompt(systemPrompt);
        logEntity.setUserInput(userInput);
        logEntity.setProvider(aiProvider);
        logEntity.setModel(modelName);
        logEntity.setStatus(0);
        aiGenerationLogMapper.insert(logEntity);

        long startMs = System.currentTimeMillis();
        try {
            ChatModel chatModel = getChatModel();
            Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userInput)));

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
            aiGenerationLogMapper.updateById(logEntity);

            // 过滤大模型可能混杂的 markdown 格式包围，例如 ```json
            if (content.startsWith("```json")) {
                content = content.substring(7);
            } else if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            return content;

        } catch (Exception e) {
            log.error("AI 行程助理场景调用失败", e);
            logEntity.setStatus(2);
            logEntity.setErrorMsg(e.getMessage());
            logEntity.setCostMs(System.currentTimeMillis() - startMs);
            aiGenerationLogMapper.updateById(logEntity);
            throw new BusinessException("AI 伴侣服务调用失败: " + e.getMessage());
        }
    }
}
