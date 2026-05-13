package com.lvai.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.PlanCheckinRecord;
import com.lvai.mapper.PlanCheckinRecordMapper;
import com.lvai.service.IPlanCheckinRecordService;
import org.springframework.stereotype.Service;

@Service
public class PlanCheckinRecordServiceImpl extends ServiceImpl<PlanCheckinRecordMapper, PlanCheckinRecord> implements IPlanCheckinRecordService {
}