package com.lvai.event;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.entity.Destination;
import com.lvai.mapper.DestinationMapper;
import com.lvai.service.IHotDestinationService;
import com.lvai.service.impl.HotDestinationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserBehaviorEventListener {

    private final DestinationMapper destinationMapper;
    private final IHotDestinationService hotDestinationService;
    private final StringRedisTemplate stringRedisTemplate;

    @Async
    @EventListener
    public void handleUserBehaviorEvent(UserBehaviorEvent event) {
        String locationName = event.getLocationName();
        if (locationName == null || locationName.trim().isEmpty()) {
            return;
        }

        // 尝试通过名称模糊匹配找到对应的目的地
        List<Destination> destinations = destinationMapper.selectList(
            new LambdaQueryWrapper<Destination>()
                .like(Destination::getName, locationName)
        );

        if (destinations != null && !destinations.isEmpty()) {
            // 匹配到相关的目的地后，重新计算其热度并清除热门缓存
            Destination target = destinations.get(0);
            hotDestinationService.calculateHotScore(target.getId());
            stringRedisTemplate.delete(HotDestinationServiceImpl.HOT_DESTINATION_CACHE_KEY);
        }
    }
}
