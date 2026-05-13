package com.lvai.controller;

import com.lvai.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "地图导览模块")
@RestController
@RequestMapping("/map")
public class MapController {

    @GetMapping("/nearby")
    @Operation(summary = "查询地图附近兴趣点(POI)")
    public Result<List<Map<String, Object>>> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "景点") String keyword) {
        
        // 这里的真实逻辑应该是调用腾讯地图 API 或查询数据库中的景点表
        // 为了演示，这里直接返回模拟数据，实际项目中可结合 IDestinationSpotService 经纬度范围查询
        
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> poi1 = new HashMap<>();
        poi1.put("id", 1);
        poi1.put("name", "附近推荐-" + keyword + " 1");
        poi1.put("lat", lat + 0.001);
        poi1.put("lng", lng + 0.001);
        poi1.put("address", "测试地址1");
        list.add(poi1);

        Map<String, Object> poi2 = new HashMap<>();
        poi2.put("id", 2);
        poi2.put("name", "附近推荐-" + keyword + " 2");
        poi2.put("lat", lat - 0.001);
        poi2.put("lng", lng - 0.001);
        poi2.put("address", "测试地址2");
        list.add(poi2);

        return Result.success(list);
    }
}
