package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.common.BusinessException;
import com.lvai.entity.Destination;
import com.lvai.entity.DestinationFood;
import com.lvai.entity.DestinationSpot;
import com.lvai.mapper.DestinationMapper;
import com.lvai.service.IDestinationFoodService;
import com.lvai.service.IDestinationService;
import com.lvai.service.IDestinationSpotService;
import com.lvai.vo.DestinationDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationServiceImpl extends ServiceImpl<DestinationMapper, Destination> implements IDestinationService {

    private final IDestinationSpotService spotService;
    private final IDestinationFoodService foodService;

    @Override
    public Page<Destination> getHotDestinations(int page, int size) {
        Page<Destination> pageParam = new Page<>(page, size);
        return page(pageParam, new LambdaQueryWrapper<Destination>()
                .orderByDesc(Destination::getHotScore)
                .orderByDesc(Destination::getSortOrder)
                .orderByDesc(Destination::getViewCount));
    }

    @Override
    public DestinationDetailVO getDestinationDetail(Long id) {
        Destination destination = getById(id);
        if (destination == null) {
            throw new BusinessException("目的地不存在");
        }
        
        // 增加浏览量
        destination.setViewCount(destination.getViewCount() + 1);
        updateById(destination);

        List<DestinationSpot> spots = spotService.list(new LambdaQueryWrapper<DestinationSpot>()
                .eq(DestinationSpot::getDestinationId, id)
                .orderByDesc(DestinationSpot::getSortOrder));

        List<DestinationFood> foods = foodService.list(new LambdaQueryWrapper<DestinationFood>()
                .eq(DestinationFood::getDestinationId, id)
                .orderByDesc(DestinationFood::getSortOrder));

        DestinationDetailVO vo = new DestinationDetailVO();
        vo.setDestination(destination);
        vo.setSpots(spots);
        vo.setFoods(foods);
        return vo;
    }
}
