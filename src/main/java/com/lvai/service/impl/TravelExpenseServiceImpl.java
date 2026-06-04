package com.lvai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.TravelExpense;
import com.lvai.mapper.TravelExpenseMapper;
import com.lvai.service.ITravelExpenseService;
import com.lvai.service.ITravelPlanService;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Service
public class TravelExpenseServiceImpl extends ServiceImpl<TravelExpenseMapper, TravelExpense> implements ITravelExpenseService {

    @Lazy
    @Autowired
    private ITravelPlanService travelPlanService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(TravelExpense entity) {
        boolean result = super.save(entity);
        if (result && entity.getPlanId() != null) {
            travelPlanService.updatePlanActualCost(entity.getPlanId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(TravelExpense entity) {
        boolean result = super.updateById(entity);
        if (result && entity.getPlanId() != null) {
            travelPlanService.updatePlanActualCost(entity.getPlanId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        TravelExpense expense = getById(id);
        boolean result = super.removeById(id);
        if (result && expense != null && expense.getPlanId() != null) {
            travelPlanService.updatePlanActualCost(expense.getPlanId());
        }
        return result;
    }
}
