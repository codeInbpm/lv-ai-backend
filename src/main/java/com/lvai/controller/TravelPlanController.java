package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvai.common.Result;
import com.lvai.dto.CreatePlanDTO;
import com.lvai.dto.PlanQueryDTO;
import com.lvai.entity.TravelDay;
import com.lvai.entity.TravelItem;
import com.lvai.entity.TravelPlan;
import com.lvai.service.ITravelDayService;
import com.lvai.service.ITravelItemService;
import com.lvai.service.ITravelPlanService;
import com.lvai.vo.PlanDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "行程管理模块")
@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
public class TravelPlanController {

    private final ITravelPlanService planService;
    private final ITravelItemService travelItemService;
    private final ITravelDayService travelDayService;

    @PostMapping("/create")
    @Operation(summary = "AI生成行程(核心接口)")
    public Result<PlanDetailVO> createPlan(@Valid @RequestBody CreatePlanDTO dto) {
        return Result.success(planService.createPlanWithAI(dto));
    }

    @GetMapping("/{planId}")
    @Operation(summary = "获取行程详情")
    public Result<PlanDetailVO> getPlanDetail(@PathVariable Long planId) {
        return Result.success(planService.getPlanDetail(planId));
    }

    @GetMapping("/list")
    @Operation(summary = "我的行程列表")
    public Result<IPage<TravelPlan>> getMyPlans(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(planService.getUserPlans(userId, status, page, size));
    }

    @PutMapping("/{planId}")
    @Operation(summary = "更新行程")
    public Result<TravelPlan> updatePlan(@PathVariable Long planId, @RequestBody TravelPlan plan) {
        plan.setId(planId);
        return Result.success(planService.updatePlan(plan));
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "删除行程")
    public Result<Void> deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        return Result.success();
    }

    @PutMapping("/{planId}/status")
    @Operation(summary = "更新行程状态(开始/完成)")
    public Result<Void> updateStatus(@PathVariable Long planId, @RequestParam Integer status) {
        planService.updatePlanStatus(planId, status);
        return Result.success();
    }

    @PostMapping("/{planId}/collect")
    @Operation(summary = "收藏/取消收藏行程")
    public Result<Boolean> toggleCollect(@PathVariable Long planId) {
        return Result.success(planService.toggleCollection(planId));
    }

    @PostMapping("/item")
    @Operation(summary = "添加行程明细项")
    public Result<TravelItem> addPlanItem(@RequestBody TravelItem item) {
        if (item.getDayId() == null || item.getPlanId() == null || item.getName() == null) {
            return Result.error("参数错误，dayId, planId, name 不能为空");
        }
        // 查找当前最大的 sortOrder
        TravelItem maxItem = travelItemService.getOne(
                new LambdaQueryWrapper<TravelItem>()
                        .eq(TravelItem::getDayId, item.getDayId())
                        .orderByDesc(TravelItem::getSortOrder)
                        .last("limit 1")
        );
        int sortOrder = maxItem == null ? 1 : maxItem.getSortOrder() + 1;
        item.setSortOrder(sortOrder);
        item.setCheckedIn(0);
        travelItemService.save(item);
        return Result.success(item);
    }

    @GetMapping("/item/{itemId}")
    @Operation(summary = "获取行程明细项详情")
    public Result<TravelItem> getPlanItemDetail(@PathVariable Long itemId) {
        TravelItem item = travelItemService.getById(itemId);
        if (item == null) {
            return Result.error("行程明细不存在");
        }
        return Result.success(item);
    }

    @PutMapping("/item/{itemId}")
    @Operation(summary = "修改行程明细项")
    public Result<TravelItem> updatePlanItem(@PathVariable Long itemId, @RequestBody TravelItem item) {
        item.setId(itemId);
        boolean success = travelItemService.updateById(item);
        if (!success) {
            return Result.error("修改行程明细项失败");
        }
        return Result.success(travelItemService.getById(itemId));
    }

    @DeleteMapping("/item/{itemId}")
    @Operation(summary = "删除行程明细项")
    public Result<Void> deletePlanItem(@PathVariable Long itemId) {
        boolean success = travelItemService.removeById(itemId);
        if (!success) {
            return Result.error("删除行程明细项失败或已不存在");
        }
        return Result.success();
    }

    @PutMapping("/items/sort")
    @Operation(summary = "批量修改和重排行程明细")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> sortPlanItems(@RequestBody List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Result.success();
        }
        
        // 1. 查找重排后首个日程项的原起航时间作为一天的黄金时间线起点
        java.time.LocalTime timeCursor = java.time.LocalTime.of(8, 0); // 默认早上8:00
        TravelItem firstItem = travelItemService.getById(itemIds.get(0));
        if (firstItem != null && firstItem.getStartTime() != null) {
            timeCursor = firstItem.getStartTime();
        }

        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            TravelItem updateItem = travelItemService.getById(itemId);
            if (updateItem == null) continue;

            // 更新排序主顺序
            updateItem.setSortOrder(i + 1);

            // 2. 级联重整时间线：开始时间对齐游标
            updateItem.setStartTime(timeCursor);
            int duration = updateItem.getDuration() == null ? 90 : updateItem.getDuration();
            java.time.LocalTime endTime = timeCursor.plusMinutes(duration);
            updateItem.setEndTime(endTime);

            travelItemService.updateById(updateItem);

            // 3. 游标向后滚动：如果是交通(type=1或名称含交通工具)或者住宿(type=2)无缝衔接；其他游览点之间加 30 分钟缓冲通勤时间
            boolean isTrafficOrHotel = (updateItem.getType() != null && (updateItem.getType() == 1 || updateItem.getType() == 4 || updateItem.getType() == 2)) 
                    || (updateItem.getName() != null && updateItem.getName().matches(".*(飞机|火车|高铁|动车|车|巴士|入住|客客).*"));
            
            int bufferMinutes = isTrafficOrHotel ? 0 : 30;
            timeCursor = endTime.plusMinutes(bufferMinutes);
        }
        return Result.success();
    }

    @PutMapping("/day/{dayId}")
    @Operation(summary = "修改每日行程的主题或描述")
    public Result<TravelDay> updatePlanDay(@PathVariable Long dayId, @RequestBody TravelDay day) {
        day.setId(dayId);
        boolean success = travelDayService.updateById(day);
        if (!success) {
            return Result.error("修改每日行程失败");
        }
        return Result.success(travelDayService.getById(dayId));
    }

    @PostMapping("/day")
    @Operation(summary = "为行程添加一天")
    @Transactional(rollbackFor = Exception.class)
    public Result<TravelDay> addPlanDay(@RequestParam Long planId) {
        TravelPlan plan = planService.getById(planId);
        if (plan == null) {
            return Result.error("行程计划不存在");
        }
        // 查找当前最大的 dayIndex
        TravelDay maxDay = travelDayService.getOne(
                new LambdaQueryWrapper<TravelDay>()
                        .eq(TravelDay::getPlanId, planId)
                        .orderByDesc(TravelDay::getDayIndex)
                        .last("limit 1")
        );
        int dayIndex = maxDay == null ? 1 : maxDay.getDayIndex() + 1;

        TravelDay newDay = new TravelDay();
        newDay.setPlanId(planId);
        newDay.setDayIndex(dayIndex);
        if (plan.getStartDate() != null) {
            newDay.setDate(plan.getStartDate().plusDays(dayIndex - 1));
        }
        newDay.setTitle("第" + dayIndex + "天安排");
        newDay.setDescription("点击编辑添加今日主题和行程安排");
        newDay.setFinished(0);
        travelDayService.save(newDay);

        // 更新行程计划的主表信息：天数 + 1
        plan.setDays(dayIndex);
        if (plan.getStartDate() != null) {
            plan.setEndDate(plan.getStartDate().plusDays(dayIndex - 1));
        }
        planService.updateById(plan);

        return Result.success(newDay);
    }

    @DeleteMapping("/day/{dayId}")
    @Operation(summary = "删除行程中的一天(级联删除天下的行程项，排后面的天数自动往前补位)")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deletePlanDay(@PathVariable Long dayId) {
        TravelDay deleteDay = travelDayService.getById(dayId);
        if (deleteDay == null) {
            return Result.error("行程天数记录不存在");
        }
        Long planId = deleteDay.getPlanId();
        int deletedDayIndex = deleteDay.getDayIndex();

        // 1. 删除这天对应的所有行程细项
        travelItemService.remove(
                new LambdaQueryWrapper<TravelItem>()
                        .eq(TravelItem::getDayId, dayId)
        );

        // 2. 删除这天本身
        travelDayService.removeById(dayId);

        // 3. 将排在这一天之后的所有天的 dayIndex - 1，并更新对应日期
        List<TravelDay> subsequentDays = travelDayService.list(
                new LambdaQueryWrapper<TravelDay>()
                        .eq(TravelDay::getPlanId, planId)
                        .gt(TravelDay::getDayIndex, deletedDayIndex)
                        .orderByAsc(TravelDay::getDayIndex)
        );

        TravelPlan plan = planService.getById(planId);
        if (plan != null) {
            for (TravelDay d : subsequentDays) {
                int newIndex = d.getDayIndex() - 1;
                d.setDayIndex(newIndex);
                if (plan.getStartDate() != null) {
                    d.setDate(plan.getStartDate().plusDays(newIndex - 1));
                }
                travelDayService.updateById(d);
            }

            // 4. 更新 travel_plan 主表的 days (天数 - 1) 与 endDate
            int newDays = Math.max(0, plan.getDays() - 1);
            plan.setDays(newDays);
            if (plan.getStartDate() != null) {
                plan.setEndDate(newDays == 0 ? plan.getStartDate() : plan.getStartDate().plusDays(newDays - 1));
            }
            planService.updateById(plan);
        }

        return Result.success();
    }
}
