package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.entity.SysDict;

import java.util.List;
import java.util.Map;

public interface ISysDictService extends IService<SysDict> {

    /**
     * 根据字典类型获取字典数据列表（包含缓存机制）
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<SysDict> getDictByType(String dictType);

    /**
     * 批量根据多个字典类型获取字典数据（Map格式，包含缓存机制）
     *
     * @param dictTypes 字典类型列表
     * @return 字典类型与数据列表的映射Map
     */
    Map<String, List<SysDict>> getDictsByTypes(List<String> dictTypes);

    /**
     * 清除特定字典类型的缓存
     *
     * @param dictType 字典类型
     */
    void clearDictCache(String dictType);
}
