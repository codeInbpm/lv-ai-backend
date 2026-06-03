package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvai.dto.CommunityStatsDTO;
import com.lvai.entity.Destination;
import com.lvai.entity.StrategyPost;
import com.lvai.entity.UserCollection;
import com.lvai.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TencentMapServiceImpl implements TencentMapService {
    @Value("${tencent.map.key}")
    private String mapKey;

    @Value("${tencent.map.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取输入关键字的补完与提示
     *
     * @param keyword 关键字
     * @param lat     用户当前位置横坐标
     * @param lng     用户当前位置纵坐标
     * @return 关键字搜索出的内容
     */
    @Override
    public JsonNode getSuggestion(String keyword, Double lat, Double lng) {
        if (StringUtils.isBlank(keyword)) return null;
        String suggestionUrl = baseUrl + "/ws/place/v1/suggestion";

        String url = String.format("%s?keyword=%s&location=%f,%f&key=%s",
                suggestionUrl,
                URLEncoder.encode(keyword, StandardCharsets.UTF_8), lat, lng, mapKey
        );

        try {
            String resp = restTemplate.getForObject(url, String.class);
            log.info("关键字搜索返回内容{}", resp);
            return objectMapper.readTree(resp).get("data");
        } catch (Exception e) {
            throw new RuntimeException("腾讯地图接口调用失败", e);
        }
    }

    /**
     * 根据定位搜索定位附近指定类型的兴趣点
     *
     * @param searchType 兴趣点类型， 默认 景点
     * @param lat        用户当前/搜索的位置横坐标
     * @param lng        用户当前/搜索的位置纵坐标
     */
    @Override
    public JsonNode getNearby(String searchType, Double lat, Double lng) {
        if (StringUtils.isBlank(searchType)) searchType = "景点";
        String nearbyUrl = baseUrl + "/ws/place/v1/search";
        // boundary: 1000-周边1km兴趣点
        String url = String.format("%s?keyword=%s&boundary=nearby(%f,%f,1000)&key=%s",
                nearbyUrl,
                URLEncoder.encode(searchType, StandardCharsets.UTF_8), lat, lng, mapKey);

        try {
            String resp = restTemplate.getForObject(url, String.class);
            log.info("search周边搜索返回内容{}", resp);
            return objectMapper.readTree(resp).get("data");
        } catch (Exception e) {
            throw new RuntimeException("腾讯地图接口调用失败", e);
        }
    }
}
