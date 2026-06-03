package com.lvai.service;


import com.fasterxml.jackson.databind.JsonNode;

public interface TencentMapService {

    /**
     * 获取输入关键字的补完与提示
     *
     * @param keyword 关键字
     * @param lat     用户当前位置横坐标
     * @param lng     用户当前位置纵坐标
     * @return 关键字搜索出的内容
     */
    JsonNode getSuggestion(String keyword, Double lat, Double lng);

    /**
     * 根据定位搜索定位附近指定类型的兴趣点
     * @param searchType 兴趣点类型， 默认 景点
     * @param lat 用户当前/搜索的位置横坐标
     * @param lng 用户当前/搜索的位置纵坐标
     */
    JsonNode getNearby(String searchType, Double lat, Double lng);
}
