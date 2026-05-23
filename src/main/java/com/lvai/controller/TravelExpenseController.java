package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.Result;
import com.lvai.entity.TravelExpense;
import com.lvai.service.ITravelExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
public class TravelExpenseController {

    private final ITravelExpenseService travelExpenseService;

    @GetMapping("/list")
    public Result<List<TravelExpense>> getExpenses() {
        long userId = StpUtil.getLoginIdAsLong();
        List<TravelExpense> list = travelExpenseService.list(
                new LambdaQueryWrapper<TravelExpense>()
                        .eq(TravelExpense::getUserId, userId)
                        .orderByDesc(TravelExpense::getExpenseDate)
                        .orderByDesc(TravelExpense::getCreateTime)
        );
        return Result.success(list);
    }
}
