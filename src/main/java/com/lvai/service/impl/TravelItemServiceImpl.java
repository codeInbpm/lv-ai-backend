package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.TravelItem;
import com.lvai.mapper.TravelItemMapper;
import com.lvai.service.ITravelItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TravelItemServiceImpl extends ServiceImpl<TravelItemMapper, TravelItem> implements ITravelItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorganizeTimeline(Long dayId) {
        List<TravelItem> items = this.list(
                new LambdaQueryWrapper<TravelItem>()
                        .eq(TravelItem::getDayId, dayId)
        );
        if (items == null || items.isEmpty()) return;

        // 根据 startTime 进行升序重排，为 null 的项置后
        items.sort((a, b) -> {
            if (a.getStartTime() == null && b.getStartTime() == null) return 0;
            if (a.getStartTime() == null) return 1;
            if (b.getStartTime() == null) return -1;
            return a.getStartTime().compareTo(b.getStartTime());
        });

        java.time.LocalTime timeCursor = java.time.LocalTime.of(8, 0); // 默认早8点
        if (items.get(0).getStartTime() != null) {
            timeCursor = items.get(0).getStartTime();
        }

        for (int i = 0; i < items.size(); i++) {
            TravelItem item = items.get(i);
            item.setSortOrder(i + 1);
            item.setStartTime(timeCursor);
            
            int duration = item.getDuration() == null ? 90 : item.getDuration();
            java.time.LocalTime endTime = timeCursor.plusMinutes(duration);
            item.setEndTime(endTime);
            
            this.updateById(item);

            // 级联重算游标：交通与住宿无缓冲，其他项加 30 分钟通勤缓冲
            boolean isTrafficOrHotel = (item.getType() != null && (item.getType() == 1 || item.getType() == 2)) 
                    || (item.getName() != null && item.getName().matches(".*(飞机|火车|高铁|动车|车|巴士|入住|客客).*"));
            int buffer = isTrafficOrHotel ? 0 : 30;
            timeCursor = endTime.plusMinutes(buffer);
        }
    }
}
