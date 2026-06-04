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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @Qualifier("anthropicChatModel")
    private ChatModel anthropicChatModel;

    private volatile ChatModel localAnthropicChatModel;

    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${spring.ai.anthropic.base-url:}")
    private String anthropicBaseUrl;

    @Value("${tencent.map.key:}")
    private String tencentMapKey;

    public AiServiceImpl(AiGenerationLogMapper aiGenerationLogMapper,
                         @Qualifier("openAiChatModel") ChatModel openAiChatModel,
                         @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,
                         @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel) {
        this.openAiChatModel = openAiChatModel;
        this.dashscopeChatModel = dashscopeChatModel;
        this.deepSeekChatModel = deepSeekChatModel;
    }

    @Override
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

    @Override
    public String generateContent(String systemPrompt, String userInput, Long userId) {
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

            log.info("AI 开始生成内容: [{}], User: {}", modelName, userId);
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

            return content;

        } catch (Exception e) {
            log.error("AI 内容生成失败", e);
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
            case "mimo-v2-pro" -> anthropicChatModel != null ? anthropicChatModel : getOrCreateAnthropicChatModel();
            case "deepseek" -> deepSeekChatModel;
            default -> openAiChatModel;
        };
        if (model == null) {
            throw new BusinessException("AI 模型服务未启动: " + aiProvider);
        }
        return model;
    }

    private synchronized ChatModel getOrCreateAnthropicChatModel() {
        if (localAnthropicChatModel != null) {
            return localAnthropicChatModel;
        }
        try {
            org.springframework.ai.anthropic.api.AnthropicApi anthropicApi = 
                org.springframework.ai.anthropic.api.AnthropicApi.builder()
                    .baseUrl(anthropicBaseUrl)
                    .apiKey(anthropicApiKey)
                    .restClientBuilder(org.springframework.web.client.RestClient.builder())
                    .webClientBuilder(org.springframework.web.reactive.function.client.WebClient.builder())
                    .build();
            
            localAnthropicChatModel = org.springframework.ai.anthropic.AnthropicChatModel.builder()
                    .anthropicApi(anthropicApi)
                    .build();
            return localAnthropicChatModel;
        } catch (Exception e) {
            log.error("手动构建 AnthropicChatModel 失败，请检查 spring.ai.anthropic 配置", e);
            throw new BusinessException("构建 AI 模型服务失败: " + e.getMessage());
        }
    }

    private String getActiveModelName() {
        return switch (aiProvider.toLowerCase()) {
            case "qwen" -> "qwen-max";
            case "mimo-v2-pro" -> "mimo-v2-pro";
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
        appendDrivingRouteInfo(dto, sb);
        return sb.toString();
    }

    private java.math.BigDecimal[] getCoordinates(String address) {
        if (address == null || address.trim().isEmpty()) return null;
        String trimmed = address.trim();
        // 校验是否为经纬度格式，例如: "39.915285,116.403857" 或 "39.915285，116.403857"
        if (trimmed.matches("^-?\\d+(\\.\\d+)?[,\\uff0c]-?\\d+(\\.\\d+)?$")) {
            String[] parts = trimmed.split("[,\\uff0c]");
            try {
                return new java.math.BigDecimal[]{
                    new java.math.BigDecimal(parts[0].trim()),
                    new java.math.BigDecimal(parts[1].trim())
                };
            } catch (Exception e) {
                log.warn("直接解析坐标失败: " + address + ", 将尝试通过 geocoder 查询", e);
            }
        }
        try {
            String url = "https://apis.map.qq.com/ws/geocoder/v1/?address=" + java.net.URLEncoder.encode(trimmed, "UTF-8") + "&key=" + tencentMapKey;
            String res = cn.hutool.http.HttpUtil.get(url, 3000);
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(res);
            if (json.getIntValue("status") == 0) {
                com.alibaba.fastjson2.JSONObject loc = json.getJSONObject("result").getJSONObject("location");
                return new java.math.BigDecimal[]{loc.getBigDecimal("lat"), loc.getBigDecimal("lng")};
            } else {
                log.warn("调用腾讯 geocoder API 失败，状态码: {}, 消息: {}, 地址: {}", json.getIntValue("status"), json.getString("message"), address);
            }
        } catch (Exception e) {
            log.error("解析地址经纬度失败: " + address, e);
        }
        return null;
    }

    private void appendDrivingRouteInfo(CreatePlanDTO dto, StringBuilder sb) {
        if (dto.getPreferences() != null && dto.getPreferences().contains("自驾") && dto.getDrivingPolicy() != null) {
            java.math.BigDecimal depLat = dto.getDepartureLat();
            java.math.BigDecimal depLng = dto.getDepartureLng();
            java.math.BigDecimal destLat = dto.getDestinationLat();
            java.math.BigDecimal destLng = dto.getDestinationLng();
            
            if (depLat == null || depLng == null) {
                java.math.BigDecimal[] coords = getCoordinates(dto.getDeparture());
                if (coords != null) { depLat = coords[0]; depLng = coords[1]; }
            }
            if (destLat == null || destLng == null) {
                java.math.BigDecimal[] coords = getCoordinates(dto.getDestination());
                if (coords != null) { destLat = coords[0]; destLng = coords[1]; }
            }
            
            if (depLat != null && depLng != null && destLat != null && destLng != null) {
                try {
                    String url = String.format("https://apis.map.qq.com/ws/direction/v1/driving/?from=%s,%s&to=%s,%s&policy=%s",
                            depLat, depLng,
                            destLat, destLng,
                            dto.getDrivingPolicy());
                    if (dto.getPlateNumber() != null && !dto.getPlateNumber().isEmpty()) {
                        url += "&plate_number=" + java.net.URLEncoder.encode(dto.getPlateNumber(), "UTF-8");
                    }
                    url += "&key=" + tencentMapKey;
                    
                    String res = cn.hutool.http.HttpUtil.get(url, 5000);
                    com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(res);
                    if (json.getIntValue("status") == 0) {
                        com.alibaba.fastjson2.JSONArray routes = json.getJSONObject("result").getJSONArray("routes");
                        if (routes != null && !routes.isEmpty()) {
                            com.alibaba.fastjson2.JSONObject route = routes.getJSONObject(0);
                            double distanceKm = route.getDoubleValue("distance") / 1000.0;
                            int durationMin = route.getIntValue("duration");
                            int trafficLightCount = route.getIntValue("traffic_light_count");
                            double toll = route.getDoubleValue("toll");
                            
                            com.alibaba.fastjson2.JSONArray tags = route.getJSONArray("tags");
                            List<String> tagList = new ArrayList<>();
                            if (tags != null) {
                                for (int i = 0; i < tags.size(); i++) {
                                    String tag = tags.getString(i);
                                    String tagCn = switch (tag) {
                                        case "RECOMMEND" -> "推荐路线";
                                        case "LEAST_LIGHT" -> "红绿灯少";
                                        case "LEAST_TIME" -> "时间最短";
                                        case "LEAST_DISTANCE" -> "距离最短";
                                        case "EXPERIENCE" -> "经验路线";
                                        default -> tag;
                                    };
                                    tagList.add(tagCn);
                                }
                            }
                            
                            String drivingPolicyChinese = switch (dto.getDrivingPolicy()) {
                                case "REAL_TRAFFIC" -> "智能推荐(路况)";
                                case "LEAST_TIME" -> "时间优先";
                                case "LEAST_FEE" -> "少收费";
                                case "AVOID_HIGHWAY" -> "不走高速";
                                case "PRIORITY_HIGHWAY" -> "高速优先";
                                default -> dto.getDrivingPolicy();
                            };

                            double durationHours = durationMin / 60.0;
                            
                            sb.append("\n【重要 - 用户选择了自驾模式】\n\n");
                            sb.append("用户已通过小程序填写了自驾相关信息，请严格按照以下自驾要求进行行程规划：\n\n");
                            sb.append("- 用户明确选择自驾出行，请以自驾为核心制定整个旅行方案。\n");
                            sb.append("- 每天纯驾驶时间建议控制在 4-7 小时以内，避免用户疲劳驾驶。\n");
                            sb.append("- 优先规划风景优美、适合自驾的路线，合理安排途中的打卡点、休息点和住宿。\n");
                            sb.append("- 必须充分参考以下腾讯地图返回的自驾实际数据：\n\n");
                            
                            sb.append("自驾策略：").append(drivingPolicyChinese).append("\n");
                            if (dto.getPlateNumber() != null && !dto.getPlateNumber().trim().isEmpty()) {
                                sb.append("车牌号：").append(dto.getPlateNumber().trim()).append("\n");
                            }
                            sb.append(String.format("总距离：%.1f 公里\n", distanceKm));
                            sb.append(String.format("预计纯驾驶时间：%d 分钟（约 %.1f 小时）\n", durationMin, durationHours));
                            sb.append(String.format("高速过路费估算：%.1f 元\n", toll));
                            sb.append(String.format("途径红绿灯数：%d 个\n", trafficLightCount));
                            if (!tagList.isEmpty()) {
                                sb.append("路线特色标签：").append(String.join("、", tagList)).append("\n");
                            }

                            com.alibaba.fastjson2.JSONObject restriction = route.getJSONObject("restriction");
                            String restrictionInfo = "限行信息：此路线方案未涉及已知限行区域。";
                            if (restriction != null) {
                                int status = restriction.getIntValue("status");
                                if (status == 0) {
                                    restrictionInfo = "限行信息：未限行（途经没有限行城市，或路线方案未涉及限行区域）。";
                                } else if (status == 1) {
                                    restrictionInfo = "限行信息：途经包含限行的城市（当前未指定车牌避让，可能有部分限行路段，建议提醒用户）。";
                                } else if (status == 3) {
                                    restrictionInfo = "限行信息：已避让限行（已根据车牌避开限行区域）。";
                                } else if (status == 4) {
                                    restrictionInfo = "限行信息：无法避开限行区域（本方案包含当前车牌受限的限行路段，请在行程中特别提醒用户！）。";
                                } else {
                                    restrictionInfo = "限行信息：状态码 " + status + "（请在规划中提醒用户注意核对当地车辆限行政策）。";
                                }
                            }
                            sb.append(restrictionInfo).append("\n\n");

                            // 把腾讯官方 routes[0] 完整详情 JSON (已剔除 polyline 以节省 Token) 喂给大模型
                            com.alibaba.fastjson2.JSONObject cleanRoute = (com.alibaba.fastjson2.JSONObject) route.clone();
                            cleanRoute.remove("polyline");
                            com.alibaba.fastjson2.JSONArray steps = cleanRoute.getJSONArray("steps");
                            if (steps != null) {
                                for (int i = 0; i < steps.size(); i++) {
                                    com.alibaba.fastjson2.JSONObject step = steps.getJSONObject(i);
                                    step.remove("polyline_idx");
                                }
                            }
                            sb.append("【腾讯官方驾车路线规划原始详情 JSON (已剔除坐标串以节省 Token)】\n");
                            sb.append(com.alibaba.fastjson2.JSON.toJSONString(cleanRoute, com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat)).append("\n\n");
                            
                            sb.append("请严格基于以上真实距离、时间和【腾讯官方驾车路线规划原始详情 JSON】的步骤(steps)来拆分每天的行程：\n");
                            sb.append("- 不要让某一天驾驶距离过长或过于集中，如果全程较长，在每天的日程里描述今天需要驾驶多少公里，经过哪些主要道路。\n");
                            sb.append("- 合理安排行车路线，尽量减少反复折返。\n");
                            sb.append("- 当需要输出每天景点之间的具体行车路线时，请参考 JSON 中的 steps 步骤，在每天日程的交通项目(type=1)的 description 或 tips 里写出和官方导航一样精准、一模一样的行车路段和转弯等细节描述。\n");
                            sb.append("- 在路线沿途或附近推荐符合用户其他偏好（美食、摄影、亲子等）的景点和活动。\n");
                            sb.append("- 给出每天推荐的住宿地点建议。\n\n");
                            
                            if (dto.getExtraNote() != null && !dto.getExtraNote().trim().isEmpty()) {
                                sb.append("额外用户自驾偏好/要求：\n");
                                sb.append(dto.getExtraNote().trim()).append("\n\n");
                            }
                            
                            sb.append("请在整个行程规划中突出自驾特色，包含每天预计驾驶里程、主要路段、休息建议，并提醒用户注意行车安全和限行规定。\n");
                        } else {
                            log.warn("调用腾讯地图驾车路线API返回异常状态码: {}", json.getString("message"));
                        }
                    }
                } catch (Exception e) {
                    log.error("获取腾讯地图驾车路线失败", e);
                }
            }
        }
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
