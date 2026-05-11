package com.lvai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.PlanExecutionRecord;
import com.lvai.mapper.PlanExecutionRecordMapper;
import com.lvai.service.IPlanExecutionRecordService;
import org.springframework.stereotype.Service;

@Service
public class PlanExecutionRecordServiceImpl extends ServiceImpl<PlanExecutionRecordMapper, PlanExecutionRecord> implements IPlanExecutionRecordService {
}
