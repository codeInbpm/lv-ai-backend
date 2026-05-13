package com.lvai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvai.common.Result;
import com.lvai.entity.Destination;
import com.lvai.service.IDestinationService;
import com.lvai.vo.DestinationDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "目的地模块")
@RestController
@RequestMapping("/destination")
@RequiredArgsConstructor
public class DestinationController {

    private final IDestinationService destinationService;

    @GetMapping("/hot")
    @Operation(summary = "获取热门目的地列表")
    public Result<Page<Destination>> getHotDestinations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(destinationService.getHotDestinations(page, size));
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "获取目的地详情(包含景点和美食)")
    public Result<DestinationDetailVO> getDestinationDetail(@PathVariable Long id) {
        return Result.success(destinationService.getDestinationDetail(id));
    }
}
