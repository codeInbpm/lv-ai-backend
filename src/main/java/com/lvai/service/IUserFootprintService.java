package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.UserFootprint;

import java.util.List;
import java.util.Map;

public interface IUserFootprintService extends IService<UserFootprint> {

    /** 添加打卡 */
    UserFootprint addFootprint(UserFootprint footprint);

    /** 获取用户足迹列表 */
    List<UserFootprint> getUserFootprints(Long userId);

    /** 获取用户足迹统计 */
    Map<String, Object> getFootprintStats(Long userId);

    /** 按国家分组 */
    List<Map<String, Object>> getCountryGroups(Long userId);

    /** 按城市分组（含封面图和天数） */
    List<Map<String, Object>> getCityGroups(Long userId);
}
