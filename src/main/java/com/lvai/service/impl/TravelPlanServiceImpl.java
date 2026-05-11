package com.lvai.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.common.BusinessException;
import com.lvai.dto.CreatePlanDTO;
import com.lvai.dto.PlanQueryDTO;
import com.lvai.entity.TravelDay;
import com.lvai.entity.TravelItem;
import com.lvai.entity.TravelPlan;
import com.lvai.entity.UserCollection;
import com.lvai.mapper.TravelPlanMapper;
import com.lvai.service.IAiService;
import com.lvai.service.ITravelPlanService;
import com.lvai.vo.AiPlanResultVO;
import com.lvai.vo.PlanDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelPlanServiceImpl extends ServiceImpl<TravelPlanMapper, TravelPlan> implements ITravelPlanService {

    private final IAiService aiService;
    private final TravelDayServiceImpl travelDayService;
    private final TravelItemServiceImpl travelItemService;
    private final UserCollectionServiceImpl userCollectionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanDetailVO createPlanWithAI(CreatePlanDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 调用AI生成行程
        AiPlanResultVO aiResult = aiService.generateTravelPlan(dto, userId);

        // 2. 创建行程主记录
        TravelPlan plan = new TravelPlan();
        plan.setUserId(userId);
        plan.setTitle(aiResult.getTitle());
        plan.setDescription(aiResult.getDescription());
        plan.setDeparture(dto.getDeparture());
        plan.setDepartureLng(dto.getDepartureLng());
        plan.setDepartureLat(dto.getDepartureLat());
        plan.setDestination(dto.getDestination());
        plan.setDestinationLng(dto.getDestinationLng());
        plan.setDestinationLat(dto.getDestinationLat());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getStartDate().plusDays(dto.getDays() - 1));
        plan.setDays(dto.getDays());
        plan.setBudget(dto.getBudget());
        plan.setPeopleCount(dto.getPeopleCount());
        plan.setPreferences(JSON.toJSONString(dto.getPreferences()));
        plan.setAiContent(JSON.toJSONString(aiResult));
        plan.setAiLogId(aiResult.getAiLogId());
        plan.setStatus(1);
        plan.setIsPublic(0);
        plan.setViewCount(0);
        plan.setCollectCount(0);
        save(plan);

        // 3. 创建每日行程和明细
        List<PlanDetailVO.DayWithItems> dayWithItemsList = new ArrayList<>();

        if (aiResult.getDays() != null) {
            for (AiPlanResultVO.DayPlan dayPlan : aiResult.getDays()) {
                TravelDay day = new TravelDay();
                day.setPlanId(plan.getId());
                day.setDayIndex(dayPlan.getDayIndex());
                day.setDate(dto.getStartDate().plusDays(dayPlan.getDayIndex() - 1));
                day.setTitle(dayPlan.getTitle());
                day.setDescription(dayPlan.getDescription());
                day.setFinished(0);
                travelDayService.save(day);

                List<TravelItem> items = new ArrayList<>();
                if (dayPlan.getItems() != null) {
                    for (AiPlanResultVO.ItemPlan itemPlan : dayPlan.getItems()) {
                        TravelItem item = new TravelItem();
                        item.setDayId(day.getId());
                        item.setPlanId(plan.getId());
                        item.setSortOrder(itemPlan.getSortOrder());
                        item.setType(itemPlan.getType());
                        item.setName(itemPlan.getName());
                        item.setAddress(itemPlan.getAddress());
                        if (itemPlan.getLng() != null) item.setLng(BigDecimal.valueOf(itemPlan.getLng()));
                        if (itemPlan.getLat() != null) item.setLat(BigDecimal.valueOf(itemPlan.getLat()));
                        if (itemPlan.getStartTime() != null) {
                            try { item.setStartTime(LocalTime.parse(itemPlan.getStartTime())); } catch (Exception ignored) {}
                        }
                        if (itemPlan.getEndTime() != null) {
                            try { item.setEndTime(LocalTime.parse(itemPlan.getEndTime())); } catch (Exception ignored) {}
                        }
                        item.setDuration(itemPlan.getDuration());
                        if (itemPlan.getEstimatedCost() != null) item.setEstimatedCost(BigDecimal.valueOf(itemPlan.getEstimatedCost()));
                        item.setDescription(itemPlan.getDescription());
                        item.setTips(itemPlan.getTips());
                        item.setCheckedIn(0);
                        travelItemService.save(item);
                        items.add(item);
                    }
                }

                PlanDetailVO.DayWithItems dwi = new PlanDetailVO.DayWithItems();
                dwi.setDay(day);
                dwi.setItems(items);
                dayWithItemsList.add(dwi);
            }
        }

        PlanDetailVO vo = new PlanDetailVO();
        vo.setPlan(plan);
        vo.setDays(dayWithItemsList);
        return vo;
    }

    @Override
    public PlanDetailVO getPlanDetail(Long planId) {
        TravelPlan plan = getById(planId);
        if (plan == null) throw new BusinessException("行程不存在");

        plan.setViewCount(plan.getViewCount() + 1);
        updateById(plan);

        List<TravelDay> days = travelDayService.list(
                new LambdaQueryWrapper<TravelDay>()
                        .eq(TravelDay::getPlanId, planId)
                        .orderByAsc(TravelDay::getDayIndex)
        );

        List<PlanDetailVO.DayWithItems> dayWithItemsList = new ArrayList<>();
        for (TravelDay day : days) {
            List<TravelItem> items = travelItemService.list(
                    new LambdaQueryWrapper<TravelItem>()
                            .eq(TravelItem::getDayId, day.getId())
                            .orderByAsc(TravelItem::getSortOrder)
            );
            PlanDetailVO.DayWithItems dwi = new PlanDetailVO.DayWithItems();
            dwi.setDay(day);
            dwi.setItems(items);
            dayWithItemsList.add(dwi);
        }

        PlanDetailVO vo = new PlanDetailVO();
        vo.setPlan(plan);
        vo.setDays(dayWithItemsList);
        return vo;
    }

    @Override
    public IPage<TravelPlan> getUserPlans(Long userId, Integer status, int page, int size) {
        LambdaQueryWrapper<TravelPlan> wrapper = new LambdaQueryWrapper<TravelPlan>()
                .eq(TravelPlan::getUserId, userId)
                .orderByDesc(TravelPlan::getCreateTime);
        if (status != null) wrapper.eq(TravelPlan::getStatus, status);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<TravelPlan> getPublicPlans(PlanQueryDTO dto) {
        return baseMapper.selectPublicPlans(
                new Page<>(dto.getPage(), dto.getSize()),
                dto.getKeyword(),
                dto.getDestination()
        );
    }

    @Override
    public TravelPlan updatePlan(TravelPlan plan) {
        updateById(plan);
        return getById(plan.getId());
    }

    @Override
    public boolean deletePlan(Long planId) {
        Long userId = StpUtil.getLoginIdAsLong();
        TravelPlan plan = getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) throw new BusinessException("无权操作");
        return removeById(planId);
    }

    @Override
    public boolean updatePlanStatus(Long planId, Integer status) {
        TravelPlan plan = getById(planId);
        if (plan == null) throw new BusinessException("行程不存在");
        plan.setStatus(status);
        return updateById(plan);
    }

    @Override
    public boolean toggleCollection(Long planId) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserCollection collection = userCollectionService.getOne(
                new LambdaQueryWrapper<UserCollection>()
                        .eq(UserCollection::getUserId, userId)
                        .eq(UserCollection::getTargetType, 1)
                        .eq(UserCollection::getTargetId, planId)
        );
        if (collection != null) {
            userCollectionService.removeById(collection.getId());
            TravelPlan plan = getById(planId);
            if (plan != null && plan.getCollectCount() > 0) {
                plan.setCollectCount(plan.getCollectCount() - 1);
                updateById(plan);
            }
            return false;
        } else {
            UserCollection newCollection = new UserCollection();
            newCollection.setUserId(userId);
            newCollection.setTargetType(1);
            newCollection.setTargetId(planId);
            userCollectionService.save(newCollection);
            TravelPlan plan = getById(planId);
            if (plan != null) {
                plan.setCollectCount(plan.getCollectCount() + 1);
                updateById(plan);
            }
            return true;
        }
    }
}
