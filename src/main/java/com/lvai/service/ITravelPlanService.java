package com.lvai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.dto.CreatePlanDTO;
import com.lvai.dto.PlanQueryDTO;
import com.lvai.entity.TravelPlan;
import com.lvai.vo.PlanDetailVO;

public interface ITravelPlanService extends IService<TravelPlan> {
    PlanDetailVO createPlanWithAI(CreatePlanDTO dto);
    void doAsyncGeneratePlan(Long planId, CreatePlanDTO dto, Long userId);
    PlanDetailVO getPlanDetail(Long planId);
    IPage<TravelPlan> getUserPlans(Long userId, Integer status, int page, int size);
    IPage<TravelPlan> getPublicPlans(PlanQueryDTO dto);
    TravelPlan updatePlan(TravelPlan plan);
    boolean deletePlan(Long planId);

    boolean updatePlanStatus(Long planId, Integer status);

    boolean toggleCollection(Long planId);

    Long clonePlan(Long planId);
}
