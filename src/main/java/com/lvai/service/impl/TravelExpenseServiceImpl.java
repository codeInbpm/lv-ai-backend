package com.lvai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.TravelExpense;
import com.lvai.mapper.TravelExpenseMapper;
import com.lvai.service.ITravelExpenseService;
import org.springframework.stereotype.Service;

@Service
public class TravelExpenseServiceImpl extends ServiceImpl<TravelExpenseMapper, TravelExpense> implements ITravelExpenseService {
}
