package com.lvai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.lvai.common.Result;
import com.lvai.service.TencentMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${tencent.map.key}")
    private String mapKey;

    @Autowired
    private TencentMapService tencentMapService;



    @GetMapping("/key")
    @Operation(summary = "获取腾讯地图开发者Key(安全抽取)")
    public Result<String> getMapKey() {
        return Result.success(mapKey);
    }

    /**
     * 获取输入关键字的补完与提示
     *
     * @param keyword 关键字
     * @param lat     用户当前位置横坐标
     * @param lng     用户当前位置纵坐标
     * @return 关键字搜索出的内容
     */
    @GetMapping("/get-suggest")
    @Operation(summary = " 用于获取输入关键字的补完与提示，帮助用户快速输入")
    public Result<JsonNode> suggestion(@RequestParam String keyword,
                                       @RequestParam Double lat,
                                       @RequestParam Double lng) {

        try {
            return Result.success(tencentMapService.getSuggestion(keyword, lat, lng));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }

    /**
     * 根据定位搜索定位附近指定类型的兴趣点
     * @param searchType 兴趣点类型， 默认 景点
     * @param lat 用户当前/搜索的位置横坐标
     * @param lng 用户当前/搜索的位置纵坐标
     */
    @GetMapping("/nearby")
    @Operation(summary = "查询地图附近兴趣点(POI)")
    public Result<JsonNode> getNearby(
            @RequestParam String searchType,
            @RequestParam Double lat,
            @RequestParam Double lng) {

        try {
            return Result.success(tencentMapService.getNearby(searchType, lat, lng));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }


    /**
     * 输入关键字直接搜索
     *
     * @param keyword 关键字
     * @param lat     用户当前位置横坐标
     * @param lng     用户当前位置纵坐标
     * @return 关键字搜索出的内容
     */
    @GetMapping("/search")
    @Operation(summary = " 用于获取输入关键字的补完与提示，帮助用户快速输入")
    public Result<JsonNode> search(@RequestParam String keyword,
                                   @RequestParam String searchType,
                                       @RequestParam Double lat,
                                       @RequestParam Double lng) {

        try {
            JsonNode suggestion = tencentMapService.getSuggestion(keyword, lat, lng);
            if (suggestion == null || suggestion.isEmpty()) return Result.success();
            JsonNode location = suggestion.get(0).get("location");
            lat = location.get("lat").asDouble();
            lng = location.get("lng").asDouble();
            return Result.success(tencentMapService.getNearby(searchType, lat, lng));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }
}
