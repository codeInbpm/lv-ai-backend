package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.Result;
import com.lvai.dto.CheckInDTO;
import com.lvai.entity.PlanExecutionRecord;
import com.lvai.entity.TravelItem;
import com.lvai.service.IPlanExecutionRecordService;
import com.lvai.service.ITravelItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "行程执行模块")
@RestController
@RequestMapping("/execution")
@RequiredArgsConstructor
public class ExecutionController {

    private final IPlanExecutionRecordService recordService;
    private final ITravelItemService itemService;

    @PostMapping("/check-in")
    @Operation(summary = "打卡 / 记账 / 写日记")
    public Result<PlanExecutionRecord> checkIn(@Valid @RequestBody CheckInDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        PlanExecutionRecord record = new PlanExecutionRecord();
        record.setPlanId(dto.getPlanId());
        record.setUserId(userId);
        record.setDayId(dto.getDayId());
        record.setItemId(dto.getItemId());
        record.setType(dto.getType());
        record.setContent(dto.getContent());
        record.setAmount(dto.getAmount());
        record.setCostType(dto.getCostType());
        record.setLocationName(dto.getLocationName());
        record.setLng(dto.getLng());
        record.setLat(dto.getLat());
        if (dto.getImages() != null) {
            record.setImages(com.alibaba.fastjson2.JSON.toJSONString(dto.getImages()));
        }
        recordService.save(record);

        // 如果是打卡类型，标记 item 为已打卡
        if (dto.getType() == 1 && dto.getItemId() != null) {
            TravelItem item = itemService.getById(dto.getItemId());
            if (item != null) {
                item.setCheckedIn(1);
                item.setCheckInTime(java.time.LocalDateTime.now());
                item.setCheckInNote(dto.getContent());
                if (dto.getImages() != null) {
                    item.setCheckInPhotos(com.alibaba.fastjson2.JSON.toJSONString(dto.getImages()));
                }
                itemService.updateById(item);
            }
        }

        return Result.success(record);
    }

    @GetMapping("/records/{planId}")
    @Operation(summary = "获取行程执行记录列表")
    public Result<List<PlanExecutionRecord>> getRecords(@PathVariable Long planId) {
        List<PlanExecutionRecord> records = recordService.list(
                new LambdaQueryWrapper<PlanExecutionRecord>()
                        .eq(PlanExecutionRecord::getPlanId, planId)
                        .orderByDesc(PlanExecutionRecord::getCreateTime)
        );
        return Result.success(records);
    }

    @GetMapping("/stats/{planId}")
    @Operation(summary = "获取行程费用统计")
    public Result<?> getStats(@PathVariable Long planId) {
        List<PlanExecutionRecord> records = recordService.list(
                new LambdaQueryWrapper<PlanExecutionRecord>()
                        .eq(PlanExecutionRecord::getPlanId, planId)
                        .eq(PlanExecutionRecord::getType, 2)
        );
        java.math.BigDecimal total = records.stream()
                .filter(r -> r.getAmount() != null)
                .map(PlanExecutionRecord::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return Result.success(java.util.Map.of("totalCost", total, "records", records));
    }
}
