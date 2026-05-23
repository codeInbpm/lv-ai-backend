package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.*;
import com.lvai.mapper.*;
import com.lvai.service.IWorldService;
import com.lvai.vo.DestinationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorldServiceImpl implements IWorldService {

    private final TravelInspirationMapper inspirationMapper;
    private final SystemBroadcastMapper broadcastMapper;
    private final DestinationMapper destinationMapper;
    private final DestinationSpotMapper spotMapper;
    private final DestinationFoodMapper foodMapper;
    private final TopicMapper topicMapper;

    @Override
    public List<TravelInspiration> getInspirations(Integer month) {
        return inspirationMapper.selectList(
            new LambdaQueryWrapper<TravelInspiration>()
                .eq(TravelInspiration::getMonth, month)
                .orderByDesc(TravelInspiration::getLikeCount, TravelInspiration::getViewCount, TravelInspiration::getIsFeatured)
        );
    }

    @Override
    public List<TravelInspiration> getHotSelfdriveInspirations() {
        return inspirationMapper.selectList(
            new LambdaQueryWrapper<TravelInspiration>()
                .eq(TravelInspiration::getIsHot, 1)
                .orderByDesc(TravelInspiration::getLikeCount, TravelInspiration::getViewCount)
        );
    }

    @Override
    public com.lvai.vo.InspirationVO getInspirationDetail(Long id) {
        TravelInspiration inspiration = inspirationMapper.selectById(id);
        if (inspiration == null) return null;
        
        com.lvai.vo.InspirationVO vo = new com.lvai.vo.InspirationVO();
        BeanUtils.copyProperties(inspiration, vo);
        
        if (inspiration.getDestinationId() != null) {
            Destination destination = destinationMapper.selectById(inspiration.getDestinationId());
            vo.setDestination(destination);
        }
        
        return vo;
    }

    @Override
    public List<SystemBroadcast> getBroadcasts() {
        return broadcastMapper.selectList(
            new LambdaQueryWrapper<SystemBroadcast>()
                .eq(SystemBroadcast::getIsActive, 1)
                .orderByDesc(SystemBroadcast::getPriority)
        );
    }

    @Override
    public List<Destination> getHotDestinations() {
        return destinationMapper.selectList(
            new LambdaQueryWrapper<Destination>()
                .eq(Destination::getIsHot, 1)
                .orderByDesc(Destination::getSortOrder)
        );
    }

    @Override
    public DestinationVO getDestinationDetail(Long id) {
        Destination destination = destinationMapper.selectById(id);
        if (destination == null) return null;
        
        DestinationVO vo = new DestinationVO();
        BeanUtils.copyProperties(destination, vo);
        
        vo.setSpots(spotMapper.selectList(new LambdaQueryWrapper<DestinationSpot>()
            .eq(DestinationSpot::getDestinationId, id)
            .orderByDesc(DestinationSpot::getSortOrder)));
            
        vo.setFoods(foodMapper.selectList(new LambdaQueryWrapper<DestinationFood>()
            .eq(DestinationFood::getDestinationId, id)
            .orderByDesc(DestinationFood::getSortOrder)));
            
        return vo;
    }

    @Override
    public List<Topic> getHotTopics() {
        return topicMapper.selectList(new LambdaQueryWrapper<Topic>()
            .orderByDesc(Topic::getFollowerCount)
            .last("limit 10"));
    }
}
