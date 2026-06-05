package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.Result;
import com.lvai.dto.CheckInDTO;
import com.lvai.entity.PlanCheckinRecord;
import com.lvai.entity.TravelItem;
import com.lvai.entity.TravelExpense;
import com.lvai.service.IPlanCheckinRecordService;
import com.lvai.service.ITravelItemService;
import com.lvai.service.ITravelExpenseService;
import com.lvai.service.IAiTravelCompanionService;
import com.lvai.service.ITravelDayService;
import com.lvai.service.ITravelPlanService;
import com.lvai.entity.TravelDay;
import com.lvai.entity.TravelPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
public class PlanCheckinController {

    private final IPlanCheckinRecordService checkinRecordService;
    private final ITravelItemService travelItemService;
    private final ITravelExpenseService travelExpenseService;
    private final IAiTravelCompanionService aiTravelCompanionService;
    private final ITravelDayService travelDayService;
    private final ITravelPlanService travelPlanService;

    @PostMapping("/checkin")
    public Result<String> submitCheckin(@RequestBody CheckInDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        PlanCheckinRecord record = new PlanCheckinRecord();
        record.setUserId(userId);
        record.setPlanId(dto.getPlanId());
        record.setDayId(dto.getDayId());
        record.setItemId(dto.getItemId());
        record.setContent(dto.getContent());
        BigDecimal totalCost = BigDecimal.ZERO;
        if (dto.getExpenses() != null && !dto.getExpenses().isEmpty()) {
            for (CheckInDTO.ExpenseItem e : dto.getExpenses()) {
                if (e.getAmount() != null && e.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    totalCost = totalCost.add(e.getAmount());
                }
            }
        } else if (dto.getCost() != null) {
            totalCost = dto.getCost();
        }
        record.setCost(totalCost);

        if (dto.getImages() != null) {
            record.setImages(JSON.toJSONString(dto.getImages()));
        }
        if (dto.getExpenses() != null && !dto.getExpenses().isEmpty()) {
            record.setExpenses(JSON.toJSONString(dto.getExpenses()));
        }
        checkinRecordService.save(record);

        // 更新 TravelItem 状态与打卡轨迹信息
        TravelItem item = travelItemService.getById(dto.getItemId());
        if (item != null) {
            item.setCheckedIn(1);
            item.setCheckInTime(LocalDateTime.now());
            if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                item.setActualCost(totalCost);
            }
            if (dto.getActualStartTime() != null) {
                item.setActualStartTime(dto.getActualStartTime());
            } else {
                item.setActualStartTime(LocalDateTime.now());
            }
            if (dto.getCheckinLocation() != null) {
                item.setCheckinLocation(dto.getCheckinLocation());
            }
            if (dto.getContent() != null) {
                item.setCheckInNote(dto.getContent());
            }
            if (dto.getImages() != null) {
                item.setCheckInPhotos(JSON.toJSONString(dto.getImages()));
            }
            travelItemService.updateById(item);

            // 1. 联动检查该天下的行程明细项是否都已打卡完成。如果是，标记 travel_day 为完成 (finished = 1)
            List<TravelItem> dayItems = travelItemService.list(
                    new LambdaQueryWrapper<TravelItem>()
                            .eq(TravelItem::getDayId, item.getDayId())
            );
            boolean dayFinished = dayItems.stream().allMatch(it -> it.getCheckedIn() != null && it.getCheckedIn() == 1);
            if (dayFinished) {
                TravelDay day = travelDayService.getById(item.getDayId());
                if (day != null && (day.getFinished() == null || day.getFinished() == 0)) {
                    day.setFinished(1);
                    travelDayService.updateById(day);
                }
            }

            // 2. 联动检查行程的所有项是否都已打卡完成。如果是，标记 travel_plan 状态为已完成 (status = 3)
            List<TravelItem> planItems = travelItemService.list(
                    new LambdaQueryWrapper<TravelItem>()
                            .eq(TravelItem::getPlanId, item.getPlanId())
            );
            boolean planFinished = planItems.stream().allMatch(it -> it.getCheckedIn() != null && it.getCheckedIn() == 1);
            if (planFinished) {
                TravelPlan plan = travelPlanService.getById(item.getPlanId());
                if (plan != null && (plan.getStatus() == null || plan.getStatus() != 3)) {
                    travelPlanService.updatePlanStatus(item.getPlanId(), 3);
                }
            }

            // 同步记录一笔或多笔消费账单
            if (dto.getExpenses() != null && !dto.getExpenses().isEmpty()) {
                for (CheckInDTO.ExpenseItem e : dto.getExpenses()) {
                    if (e.getAmount() != null && e.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                        TravelExpense expense = new TravelExpense();
                        expense.setUserId(userId);
                        expense.setPlanId(dto.getPlanId());
                        expense.setDayId(dto.getDayId());
                        expense.setAmount(e.getAmount());
                        expense.setType(e.getCostType() != null ? e.getCostType() : 6);
                        expense.setRemark(item.getName() + " - 打卡记账");
                        expense.setExpenseDate(LocalDate.now());
                        travelExpenseService.save(expense);
                    }
                }
            } else if (dto.getCost() != null && dto.getCost().compareTo(BigDecimal.ZERO) > 0) {
                TravelExpense expense = new TravelExpense();
                expense.setUserId(userId);
                expense.setPlanId(dto.getPlanId());
                expense.setDayId(dto.getDayId());
                expense.setAmount(dto.getCost());

                // 默认映射逻辑
                int itemType = item.getType() != null ? item.getType() : 6;
                int expenseType = 6;
                if (itemType == 1) expenseType = 4;      // 景点 -> 门票
                else if (itemType == 2) expenseType = 1; // 美食 -> 餐饮
                else if (itemType == 3) expenseType = 2; // 酒店 -> 住宿
                else if (itemType == 4) expenseType = 3; // 交通 -> 交通
                else if (itemType == 5) expenseType = 5; // 购物 -> 购物

                expense.setType(dto.getCostType() != null ? dto.getCostType() : expenseType);
                expense.setRemark(item.getName() + " - 打卡记账");
                expense.setExpenseDate(LocalDate.now());
                travelExpenseService.save(expense);
            }
        }

        return Result.success("打卡成功");
    }

    @PostMapping("/checkin/ai-suggest")
    public Result<String> aiSuggestCheckin(@RequestParam Long itemId, @RequestParam(required = false) String userInput) {
        long userId = StpUtil.getLoginIdAsLong();
        String answer = aiTravelCompanionService.callAiForCheckin(itemId, userInput, userId);
        return Result.success("success", answer);
    }

    @PostMapping("/checkin/daily-summary")
    public Result<String> dailySummary(@RequestParam Long planId, @RequestParam Long dayId) {
        long userId = StpUtil.getLoginIdAsLong();
        String answer = aiTravelCompanionService.generateDailySummary(planId, dayId, userId);
        return Result.success("success", answer);
    }

    @GetMapping("/{planId}/checkins")
    public Result<List<PlanCheckinRecord>> getCheckins(@PathVariable Long planId) {
        List<PlanCheckinRecord> records = checkinRecordService.list(
                new LambdaQueryWrapper<PlanCheckinRecord>()
                        .eq(PlanCheckinRecord::getPlanId, planId)
                        .orderByAsc(PlanCheckinRecord::getCreateTime)
        );
        return Result.success(records);
    }
}