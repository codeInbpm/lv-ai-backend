package com.lvai.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.*;
import com.lvai.mapper.ContentDraftMapper;
import com.lvai.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentDraftServiceImpl extends ServiceImpl<ContentDraftMapper, ContentDraft> implements IContentDraftService {

    private final IAiService aiService;
    private final ITravelPlanService planService;
    private final ITravelDayService dayService;
    private final ITravelItemService itemService;
    private final IPlanCheckinRecordService checkinService;
    private final ITravelExpenseService expenseService;

    @Override
    @Async("asyncExecutor") // Use system's async executor, or default if missing
    public void generateDraftForPlan(Long planId, Long userId) {
        log.info("开始为行程生成游记草稿，planId={}", planId);
        try {
            // 1. 检查是否已经生成过该行程的草稿
            long count = count(new LambdaQueryWrapper<ContentDraft>()
                    .eq(ContentDraft::getPlanId, planId)
                    .eq(ContentDraft::getDraftType, 3)); // 3: 游记
            if (count > 0) {
                log.info("行程 {} 的游记草稿已存在，跳过生成", planId);
                return;
            }

            // 2. 收集数据
            TravelPlan plan = planService.getById(planId);
            if (plan == null) return;

            // 每日行程概览
            List<TravelDay> days = dayService.list(new LambdaQueryWrapper<TravelDay>()
                    .eq(TravelDay::getPlanId, planId)
                    .orderByAsc(TravelDay::getDayIndex));
            
            StringBuilder daysSummary = new StringBuilder();
            for (TravelDay day : days) {
                daysSummary.append("Day").append(day.getDayIndex()).append(" (")
                        .append(day.getDate() != null ? day.getDate().toString() : "未知日期").append(")：")
                        .append(day.getTitle()).append("\n");
                
                List<TravelItem> items = itemService.list(new LambdaQueryWrapper<TravelItem>()
                        .eq(TravelItem::getDayId, day.getId())
                        .orderByAsc(TravelItem::getSortOrder));
                for (TravelItem item : items) {
                    daysSummary.append(" • ").append(item.getName()).append("\n");
                }
            }

            // 打卡记录
            List<PlanCheckinRecord> checkins = checkinService.list(new LambdaQueryWrapper<PlanCheckinRecord>()
                    .eq(PlanCheckinRecord::getPlanId, planId)
                    .orderByAsc(PlanCheckinRecord::getCreateTime));
            
            // 如果完全没有打卡数据，生成出来的游记可能很空洞，这里可以选择是否继续，按当前设计继续执行
            StringBuilder checkinSummary = new StringBuilder();
            for (PlanCheckinRecord checkin : checkins) {
                TravelItem item = itemService.getById(checkin.getItemId());
                String itemName = item != null ? item.getName() : "未知地点";
                checkinSummary.append("- ").append(itemName).append("：");
                if (checkin.getCost() != null && checkin.getCost().compareTo(BigDecimal.ZERO) > 0) {
                    checkinSummary.append("花费¥").append(checkin.getCost()).append("，");
                }
                checkinSummary.append("用户感想“").append(checkin.getContent() != null ? checkin.getContent() : "打卡了").append("”\n");
            }

            // 消费记录
            List<TravelExpense> expenses = expenseService.list(new LambdaQueryWrapper<TravelExpense>()
                    .eq(TravelExpense::getPlanId, planId));
            StringBuilder expenseSummary = new StringBuilder();
            BigDecimal totalExpense = BigDecimal.ZERO;
            for (TravelExpense expense : expenses) {
                totalExpense = totalExpense.add(expense.getAmount());
                String typeName = switch(expense.getType()) {
                    case 1 -> "餐饮";
                    case 2 -> "住宿";
                    case 3 -> "交通";
                    case 4 -> "门票";
                    case 5 -> "购物";
                    default -> "其他";
                };
                expenseSummary.append("- ").append(typeName).append("：¥").append(expense.getAmount());
                if (expense.getRemark() != null && !expense.getRemark().isEmpty()) {
                    expenseSummary.append(" (").append(expense.getRemark()).append(")");
                }
                expenseSummary.append("\n");
            }
            expenseSummary.append("总计记录花费：¥").append(totalExpense).append("\n");

            // 3. 构建 Prompt
            String promptTemplate = ResourceUtil.readUtf8Str("prompt/community-guide-prompt.txt");
            String finalPrompt = promptTemplate
                    .replace("{planTitle}", plan.getTitle() != null ? plan.getTitle() : "我的旅行")
                    .replace("{days}", plan.getDays() != null ? plan.getDays().toString() : "1")
                    .replace("{peopleCount}", plan.getPeopleCount() != null ? plan.getPeopleCount().toString() : "1")
                    .replace("{destination}", plan.getDestination() != null ? plan.getDestination() : "未知")
                    .replace("{budget}", plan.getBudget() != null ? plan.getBudget().toString() : "未知")
                    .replace("{actualCost}", plan.getActualCost() != null ? plan.getActualCost().toString() : totalExpense.toString())
                    .replace("{preferences}", plan.getPreferences() != null ? plan.getPreferences() : "无")
                    .replace("{daysSummary}", daysSummary.toString().isEmpty() ? "暂无行程详情" : daysSummary.toString())
                    .replace("{checkinRecords}", checkinSummary.toString().isEmpty() ? "暂无打卡记录" : checkinSummary.toString())
                    .replace("{expenseSummary}", expenseSummary.toString().isEmpty() ? "暂无消费记录" : expenseSummary.toString());

            // 4. 调用 AI
            String aiResponse = aiService.generateContent(
                    "你是一个专业的旅游博主，擅长撰写高质量的小红书游记。", 
                    finalPrompt, 
                    userId
            );

            // 5. 解析并保存草稿
            String jsonStr = aiResponse;
            int start = aiResponse.indexOf("{");
            int end = aiResponse.lastIndexOf("}");
            if (start >= 0 && end > start) {
                jsonStr = aiResponse.substring(start, end + 1);
            }

            JSONObject jsonObject = JSON.parseObject(jsonStr);
            ContentDraft draft = new ContentDraft();
            draft.setUserId(userId);
            draft.setPlanId(planId);
            draft.setDraftType(3); // 3 游记
            draft.setTitle(jsonObject.getString("title"));
            draft.setSummary(jsonObject.getString("summary"));
            draft.setContent(jsonObject.getString("content"));
            
            // 组装 metadata
            JSONObject metadata = new JSONObject();
            metadata.put("coverPrompt", jsonObject.getString("coverPrompt"));
            metadata.put("highlights", jsonObject.getJSONArray("highlights"));
            metadata.put("tips", jsonObject.getJSONArray("tips"));
            metadata.put("tags", jsonObject.getJSONArray("tags"));
            metadata.put("mood", jsonObject.getString("mood"));
            metadata.put("shareText", jsonObject.getString("shareText"));
            
            draft.setAiMetadata(metadata.toJSONString());
            
            save(draft);
            log.info("游记草稿生成成功并保存，planId={}", planId);

        } catch (Exception e) {
            log.error("为行程 {} 生成游记草稿失败", planId, e);
            // 这里失败静默处理，不影响主流程
        }
    }
}
