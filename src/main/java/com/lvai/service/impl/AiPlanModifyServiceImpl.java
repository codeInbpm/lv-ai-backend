package com.lvai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.BusinessException;
import com.lvai.dto.AiPlanModifyDTO;
import com.lvai.entity.TravelItem;
import com.lvai.service.IAiPlanModifyService;
import com.lvai.service.ITravelItemService;
import com.lvai.vo.AiPlanModifyResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiPlanModifyServiceImpl implements IAiPlanModifyService {

    @Value("${ai.provider}")
    private String aiProvider;

    private final ChatModel openAiChatModel;
    private final ChatModel dashscopeChatModel;
    private final ChatModel deepSeekChatModel;
    private final ITravelItemService travelItemService;

    public AiPlanModifyServiceImpl(@Qualifier("openAiChatModel") ChatModel openAiChatModel,
                                   @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,
                                   @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel,
                                   ITravelItemService travelItemService) {
        this.openAiChatModel = openAiChatModel;
        this.dashscopeChatModel = dashscopeChatModel;
        this.deepSeekChatModel = deepSeekChatModel;
        this.travelItemService = travelItemService;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiPlanModifyResultVO modifyPlanIncrementally(AiPlanModifyDTO dto, Long userId) {
        // 1. 读取当天在数据库中已经排好的明细
        List<TravelItem> existingItems = travelItemService.list(
                new LambdaQueryWrapper<TravelItem>()
                        .eq(TravelItem::getDayId, dto.getDayId())
                        .orderByAsc(TravelItem::getSortOrder)
        );

        // 2. 将现有明细序列化，精简无用字段，只传递给 AI 最关键的对比环境
        JSONArray itemsContext = new JSONArray();
        for (TravelItem item : existingItems) {
            JSONObject obj = new JSONObject();
            obj.put("id", item.getId());
            obj.put("sortOrder", item.getSortOrder());
            obj.put("type", item.getType());
            obj.put("name", item.getName());
            obj.put("address", item.getAddress());
            obj.put("startTime", item.getStartTime() != null ? item.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null);
            obj.put("duration", item.getDuration());
            obj.put("estimatedCost", item.getEstimatedCost());
            obj.put("description", item.getDescription());
            obj.put("tips", item.getTips());
            itemsContext.add(obj);
        }

        // 3. 构建高精度系统级修改 Prompt
        String systemPrompt = """
                你是一个非常专业的 AI 旅行规划助手。现在你需要根据用户的“修改/微调行程指令”，对用户当前的行程明细数据进行高精度增量修改。
                
                ## 用户当前天的行程明细列表如下 (JSON 格式)
                {itemsContext}
                
                
                ## 类型说明 (type)
                - type=1：景点/自然风光
                - type=2：美食/餐厅
                - type=3：住宿/酒店
                - type=4：交通（飞机/高铁/地铁/出租）
                - type=5：购物
                - type=6：其他活动
                
                ## 修改规则与行为指南 (极其重要)
                1. **DELETE 指令**：若用户明确说“删掉某日程”、“不要某日程”或“用新日程替换它（则删除旧项并添加新项）”，请找出该日程的唯一 `id`，发出一份 `DELETE` 操作指令。
                2. **ADD 指令**：若用户要求“增加日程”、“下午加个地方”、“吃火锅”等，发出 `ADD` 操作指令。必须完整填齐 `item` 数据。`startTime` 必须是 `HH:mm` 24小时制，根据前后日程的空挡时间进行非常合理的设计。
                3. **UPDATE 指令**：若用户要求“将某地方改到下午”、“将某地方的时长缩短”、“改名”等，请根据名称匹配出唯一的 `id`，发出 `UPDATE` 指令。只需在 `item` 里包含发生变化的字段值。例如要更改时间，则传入 `item: {"startTime": "16:00"}`。
                4. **时间连贯性**：无需去计算复杂的级联时间（如将所有后续时间表后推一小时），因为后端计算引擎会自动帮你做完这些工作。你只需要给出指定 item 的新 `startTime` 或者新 `duration`，后端会负责级联重整。
                
                ## 严格输出 JSON 格式规范
                请严格按照以下 JSON 格式输出，不要包含任何 Markdown 标记或包裹符，不要有额外的文字说明：
                {
                  "explanation": "针对本次增量修改给用户做出的甜美、温馨的简短汇总总结话语(100字以内，包含适当的 emoji，例如：我已为您把下午吃日料换成了四川火锅，并且后面的行程起止时刻表都贴心为您重整并加了30分钟通勤缓冲哦！🏮)",
                  "commands": [
                    {
                      "action": "DELETE",
                      "itemId": 105
                    },
                    {
                      "action": "ADD",
                      "dayId": {dayId},
                      "item": {
                        "type": 2,
                        "name": "海底捞火锅",
                        "address": "南京东路XXX号",
                        "startTime": "12:00",
                        "duration": 120,
                        "estimatedCost": 150,
                        "description": "吃顿暖暖的特色川味火锅",
                        "tips": "饭点人多，建议提前取号"
                      }
                    },
                    {
                      "action": "UPDATE",
                      "itemId": 106,
                      "item": {
                        "startTime": "15:30",
                        "duration": 60
                      }
                    }
                  ]
                }
                """
                .replace("{itemsContext}", itemsContext.toJSONString())
                .replace("{dayId}", String.valueOf(dto.getDayId()));

        // 4. 调用大模型
        ChatModel chatModel = getChatModel();
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(dto.getMessage()));

        log.info("AI 对话式增量修改行程开始, User: {}, Session: {}", userId, dto.getSessionId());
        ChatResponse chatResponse = chatModel.call(new Prompt(messages));
        
        String aiOutput = "";
        if (chatResponse != null && chatResponse.getResult() != null) {
            aiOutput = chatResponse.getResult().getOutput().getText();
        }

        // 5. 鲁棒解析 AI 输出的 JSON 字符串
        String jsonStr = aiOutput;
        int start = aiOutput.indexOf("{");
        int end = aiOutput.lastIndexOf("}");
        if (start >= 0 && end > start) {
            jsonStr = aiOutput.substring(start, end + 1);
        }

        try {
            JSONObject resultJson = JSON.parseObject(jsonStr);
            JSONArray commands = resultJson.getJSONArray("commands");
            
            // 6. 原子级批量事务执行变更指令
            if (commands != null) {
                for (int i = 0; i < commands.size(); i++) {
                    JSONObject cmd = commands.getJSONObject(i);
                    String action = cmd.getString("action");
                    
                    if ("DELETE".equalsIgnoreCase(action)) {
                        Long itemId = cmd.getLong("itemId");
                        if (itemId != null) {
                            travelItemService.removeById(itemId);
                            log.info("AI 执行增量修改 -> 物理删除行程明细: itemId={}", itemId);
                        }
                    } else if ("ADD".equalsIgnoreCase(action)) {
                        JSONObject itemObj = cmd.getJSONObject("item");
                        if (itemObj != null) {
                            TravelItem newItem = new TravelItem();
                            newItem.setPlanId(dto.getPlanId());
                            newItem.setDayId(dto.getDayId());
                            newItem.setType(itemObj.getInteger("type"));
                            newItem.setName(itemObj.getString("name"));
                            newItem.setAddress(itemObj.getString("address"));
                            newItem.setDuration(itemObj.getInteger("duration"));
                            newItem.setEstimatedCost(itemObj.getBigDecimal("estimatedCost"));
                            newItem.setDescription(itemObj.getString("description"));
                            newItem.setTips(itemObj.getString("tips"));
                            newItem.setCheckedIn(0);
                            
                            String startTimeStr = itemObj.getString("startTime");
                            if (startTimeStr != null && startTimeStr.matches("^\\d{2}:\\d{2}$")) {
                                newItem.setStartTime(LocalTime.parse(startTimeStr));
                            }
                            
                            travelItemService.save(newItem);
                            log.info("AI 执行增量修改 -> 添加行程明细: newItemName={}", newItem.getName());
                        }
                    } else if ("UPDATE".equalsIgnoreCase(action)) {
                        Long itemId = cmd.getLong("itemId");
                        JSONObject itemObj = cmd.getJSONObject("item");
                        if (itemId != null && itemObj != null) {
                            TravelItem existItem = travelItemService.getById(itemId);
                            if (existItem != null) {
                                // 增量无害属性覆盖，绝不覆盖已有足迹数据
                                if (itemObj.containsKey("type")) existItem.setType(itemObj.getInteger("type"));
                                if (itemObj.containsKey("name")) existItem.setName(itemObj.getString("name"));
                                if (itemObj.containsKey("address")) existItem.setAddress(itemObj.getString("address"));
                                if (itemObj.containsKey("duration")) existItem.setDuration(itemObj.getInteger("duration"));
                                if (itemObj.containsKey("estimatedCost")) existItem.setEstimatedCost(itemObj.getBigDecimal("estimatedCost"));
                                if (itemObj.containsKey("description")) existItem.setDescription(itemObj.getString("description"));
                                if (itemObj.containsKey("tips")) existItem.setTips(itemObj.getString("tips"));
                                
                                if (itemObj.containsKey("startTime")) {
                                    String startTimeStr = itemObj.getString("startTime");
                                    if (startTimeStr != null && startTimeStr.matches("^\\d{2}:\\d{2}$")) {
                                        existItem.setStartTime(LocalTime.parse(startTimeStr));
                                    }
                                }
                                
                                travelItemService.updateById(existItem);
                                log.info("AI 执行增量修改 -> 修改行程明细: itemId={}", itemId);
                            }
                        }
                    }
                }
            }

            // 7. 级联重整时间线
            travelItemService.reorganizeTimeline(dto.getDayId());

            // 8. 构造输出
            AiPlanModifyResultVO vo = new AiPlanModifyResultVO();
            vo.setExplanation(resultJson.getString("explanation"));
            vo.setSuccess(true);
            vo.setAffectedDayId(dto.getDayId());
            return vo;

        } catch (Exception e) {
            log.error("AI 增量修改解析执行事务失败", e);
            throw new BusinessException("AI 智能修改行程失败，请尝试换一种口吻交代: " + e.getMessage());
        }
    }
}
