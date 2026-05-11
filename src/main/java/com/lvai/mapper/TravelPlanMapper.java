package com.lvai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvai.entity.TravelPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TravelPlanMapper extends BaseMapper<TravelPlan> {
    IPage<TravelPlan> selectPublicPlans(IPage<TravelPlan> page, @Param("keyword") String keyword, @Param("destination") String destination);
}
