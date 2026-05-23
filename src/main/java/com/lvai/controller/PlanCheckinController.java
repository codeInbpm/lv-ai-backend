package com.lvai.controller;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.Result;
import com.lvai.dto.CheckInDTO;
import com.lvai.entity.PlanCheckinRecord;
import com.lvai.service.IPlanCheckinRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
public class PlanCheckinController {

    private final IPlanCheckinRecordService checkinRecordService;
    private final com.lvai.service.ITravelItemService travelItemService;
    private final com.lvai.service.ITravelExpenseService travelExpenseService;

    @PostMapping("/checkin")
    public Result<String> submitCheckin(@RequestBody CheckInDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        PlanCheckinRecord record = new PlanCheckinRecord();
        record.setUserId(userId);
        record.setPlanId(dto.getPlanId());
        record.setDayId(dto.getDayId());
        record.setItemId(dto.getItemId());
        record.setContent(dto.getContent());
        record.setCost(dto.getCost());
        if (dto.getImages() != null) {
            record.setImages(JSON.toJSONString(dto.getImages()));
        }
        checkinRecordService.save(record);

        // 更新 TravelItem 状态
        com.lvai.entity.TravelItem item = travelItemService.getById(dto.getItemId());
        if (item != null) {
            item.setCheckedIn(1);
            item.setCheckInTime(java.time.LocalDateTime.now());
            if (dto.getCost() != null && dto.getCost().compareTo(java.math.BigDecimal.ZERO) > 0) {
                item.setActualCost(dto.getCost());
            }
            travelItemService.updateById(item);

            // 记录一笔账单
            if (dto.getCost() != null && dto.getCost().compareTo(java.math.BigDecimal.ZERO) > 0) {
                com.lvai.entity.TravelExpense expense = new com.lvai.entity.TravelExpense();
                expense.setUserId(userId);
                expense.setPlanId(dto.getPlanId());
                expense.setDayId(dto.getDayId());
                expense.setAmount(dto.getCost());
                
                int itemType = item.getType() != null ? item.getType() : 6;
                int expenseType = 6;
                if (itemType == 1) expenseType = 4; // 景点 -> 门票
                else if (itemType == 2) expenseType = 1; // 美食 -> 餐饮
                else if (itemType == 3) expenseType = 2; // 酒店 -> 住宿
                else if (itemType == 4) expenseType = 3; // 交通 -> 交通
                else if (itemType == 5) expenseType = 5; // 购物 -> 购物
                
                expense.setType(expenseType);
                expense.setRemark(item.getName());
                expense.setExpenseDate(java.time.LocalDate.now());
                travelExpenseService.save(expense);
            }
        }

        return Result.success("打卡成功");
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