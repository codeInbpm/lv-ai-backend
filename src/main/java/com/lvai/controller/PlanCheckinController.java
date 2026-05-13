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