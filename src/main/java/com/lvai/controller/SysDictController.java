package com.lvai.controller;

import com.lvai.common.Result;
import com.lvai.entity.SysDict;
import com.lvai.service.ISysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Tag(name = "公共字典模块")
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final ISysDictService dictService;

    @GetMapping("/type/{type}")
    @Operation(summary = "根据字典类型获取字典选项列表")
    public Result<List<SysDict>> getDictByType(@PathVariable String type) {
        return Result.success(dictService.getDictByType(type));
    }

    @GetMapping("/batch")
    @Operation(summary = "批量获取多个类型的字典数据(以逗号分隔，如: weather,mood)")
    public Result<Map<String, List<SysDict>>> getDictsByTypes(@RequestParam String types) {
        List<String> typeList = Arrays.asList(types.split(","));
        return Result.success(dictService.getDictsByTypes(typeList));
    }

    @PostMapping("/refresh/{type}")
    @Operation(summary = "手动清除并更新特定字典类型的 Redis 缓存")
    public Result<Void> refreshCache(@PathVariable String type) {
        dictService.clearDictCache(type);
        return Result.success();
    }
}
