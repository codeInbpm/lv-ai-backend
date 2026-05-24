package com.lvai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.UserFootprint;
import com.lvai.mapper.UserFootprintMapper;
import com.lvai.service.IUserFootprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFootprintServiceImpl extends ServiceImpl<UserFootprintMapper, UserFootprint>
        implements IUserFootprintService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserFootprint addFootprint(UserFootprint footprint) {
        if (StrUtil.isBlank(footprint.getCountry())) {
            footprint.setCountry("中国");
        }
        save(footprint);
        return footprint;
    }

    @Override
    public List<UserFootprint> getUserFootprints(Long userId) {
        return list(new LambdaQueryWrapper<UserFootprint>()
                .eq(UserFootprint::getUserId, userId)
                .orderByDesc(UserFootprint::getCreateTime));
    }

    @Override
    public Map<String, Object> getFootprintStats(Long userId) {
        List<UserFootprint> footprints = list(new LambdaQueryWrapper<UserFootprint>()
                .eq(UserFootprint::getUserId, userId));

        long cityCount = footprints.stream()
                .filter(f -> StrUtil.isNotBlank(f.getCity()))
                .map(UserFootprint::getCity)
                .distinct().count();

        long provinceCount = footprints.stream()
                .filter(f -> StrUtil.isNotBlank(f.getProvince()))
                .map(UserFootprint::getProvince)
                .distinct().count();

        long checkinDays = footprints.stream()
                .filter(f -> f.getCreateTime() != null)
                .map(f -> f.getCreateTime().toLocalDate())
                .distinct().count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("cityCount", cityCount);
        stats.put("provinceCount", provinceCount);
        stats.put("totalFootprints", footprints.size());
        stats.put("checkinDays", checkinDays);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getCountryGroups(Long userId) {
        List<UserFootprint> footprints = list(new LambdaQueryWrapper<UserFootprint>()
                .eq(UserFootprint::getUserId, userId));

        Map<String, List<UserFootprint>> grouped = footprints.stream()
                .filter(f -> StrUtil.isNotBlank(f.getCountry()))
                .collect(Collectors.groupingBy(UserFootprint::getCountry));

        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((country, list) -> {
            long cityCount = list.stream()
                    .filter(f -> StrUtil.isNotBlank(f.getCity()))
                    .map(UserFootprint::getCity)
                    .distinct().count();
            Map<String, Object> item = new HashMap<>();
            item.put("country", country);
            item.put("cityCount", cityCount);
            item.put("footprintCount", list.size());
            result.add(item);
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> getCityGroups(Long userId) {
        List<UserFootprint> footprints = list(new LambdaQueryWrapper<UserFootprint>()
                .eq(UserFootprint::getUserId, userId)
                .orderByAsc(UserFootprint::getCreateTime));

        Map<String, List<UserFootprint>> grouped = footprints.stream()
                .filter(f -> StrUtil.isNotBlank(f.getCity()))
                .collect(Collectors.groupingBy(f -> f.getCity() + "|" + (f.getCountry() != null ? f.getCountry() : "中国")));

        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((key, list) -> {
            String[] parts = key.split("\\|", 2);
            String city = parts[0];
            String country = parts.length > 1 ? parts[1] : "中国";

            // 计算天数
            long days = list.stream()
                    .filter(f -> f.getCreateTime() != null)
                    .map(f -> f.getCreateTime().toLocalDate())
                    .distinct().count();

            // 获取封面图（第一张有图的）
            String coverImage = list.stream()
                    .filter(f -> StrUtil.isNotBlank(f.getImages()))
                    .findFirst()
                    .map(UserFootprint::getImages)
                    .orElse("[]");

            // 获取首次打卡时间
            String firstTime = list.stream()
                    .filter(f -> f.getCreateTime() != null)
                    .map(f -> f.getCreateTime().toString())
                    .findFirst()
                    .orElse("");

            Map<String, Object> item = new HashMap<>();
            item.put("city", city);
            item.put("country", country);
            item.put("days", days);
            item.put("footprintCount", list.size());
            item.put("coverImages", coverImage);
            item.put("firstTime", firstTime);
            result.add(item);
        });

        // 按天数降序
        result.sort((a, b) -> Long.compare((Long) b.get("days"), (Long) a.get("days")));
        return result;
    }
}
