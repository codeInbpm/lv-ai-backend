package com.lvai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.SysDict;
import com.lvai.mapper.SysDictMapper;
import com.lvai.service.ISysDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "sys:dict:";
    private static final long CACHE_TIMEOUT = 24; // 缓存失效时间：24小时

    @Override
    @SuppressWarnings("unchecked")
    public List<SysDict> getDictByType(String dictType) {
        String key = CACHE_PREFIX + dictType;
        try {
            // 1. 先尝试从 Redis 缓存中获取
            List<SysDict> dictList = (List<SysDict>) redisTemplate.opsForValue().get(key);
            if (dictList != null) {
                log.debug("获取字典缓存成功 dictType: {}", dictType);
                return dictList;
            }
        } catch (Exception e) {
            log.error("Redis获取字典失败 dictType: {}, 错误: {}", dictType, e.getMessage());
        }

        // 2. 缓存不存在，从 MySQL 中查询（状态正常、升序排序）
        List<SysDict> dictList = this.list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType)
                .eq(SysDict::getStatus, 0)
                .orderByAsc(SysDict::getSortOrder)
        );

        // 3. 写入 Redis 缓存并返回
        if (dictList != null && !dictList.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(key, dictList, CACHE_TIMEOUT, TimeUnit.HOURS);
                log.debug("写入字典缓存成功 dictType: {}", dictType);
            } catch (Exception e) {
                log.error("Redis写入字典失败 dictType: {}, 错误: {}", dictType, e.getMessage());
            }
        }

        return dictList;
    }

    @Override
    public Map<String, List<SysDict>> getDictsByTypes(List<String> dictTypes) {
        Map<String, List<SysDict>> result = new HashMap<>();
        if (dictTypes == null || dictTypes.isEmpty()) {
            return result;
        }
        for (String type : dictTypes) {
            result.put(type, this.getDictByType(type));
        }
        return result;
    }

    @Override
    public void clearDictCache(String dictType) {
        String key = CACHE_PREFIX + dictType;
        redisTemplate.delete(key);
        log.info("清除字典缓存成功 dictType: {}", dictType);
    }

    // -------------------------------------------------------------
//    -- 重写写操作接口，实现数据更新时自动清除缓存，避免脏数据
    // -------------------------------------------------------------

    @Override
    public boolean save(SysDict entity) {
        boolean success = super.save(entity);
        if (success) {
            this.clearDictCache(entity.getDictType());
        }
        return success;
    }

    @Override
    public boolean updateById(SysDict entity) {
        SysDict oldEntity = this.getById(entity.getId());
        boolean success = super.updateById(entity);
        if (success) {
            if (oldEntity != null) {
                this.clearDictCache(oldEntity.getDictType());
            }
            this.clearDictCache(entity.getDictType());
        }
        return success;
    }

    @Override
    public boolean removeById(Serializable id) {
        SysDict entity = this.getById(id);
        boolean success = super.removeById(id);
        if (success && entity != null) {
            this.clearDictCache(entity.getDictType());
        }
        return success;
    }
}
