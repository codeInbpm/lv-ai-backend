package com.lvai.controller;

import com.lvai.common.Result;
import com.lvai.entity.TravelInspiration;
import com.lvai.entity.SystemBroadcast;
import com.lvai.entity.Destination;
import com.lvai.entity.Topic;
import com.lvai.service.IHotDestinationService;
import com.lvai.service.IWorldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "看世界模块")
@RestController
@RequestMapping("/world")
@RequiredArgsConstructor
public class WorldController {

    private final IWorldService worldService;
    private final IHotDestinationService hotDestinationService;

    @GetMapping("/inspirations")
    @Operation(summary = "获取出行灵感")
    public Result<List<TravelInspiration>> getInspirations(@RequestParam Integer month) {
        return Result.success(worldService.getInspirations(month));
    }

    @GetMapping("/hot-selfdrive")
    @Operation(summary = "获取热门自驾自由行列表")
    public Result<List<TravelInspiration>> getHotSelfdrive() {
        return Result.success(worldService.getHotSelfdriveInspirations());
    }

    @GetMapping("/inspiration/{id}")
    @Operation(summary = "获取灵感详情")
    public Result<com.lvai.vo.InspirationVO> getInspirationDetail(@PathVariable Long id) {
        return Result.success(worldService.getInspirationDetail(id));
    }

    @GetMapping("/broadcast")
    @Operation(summary = "获取系统广播")
    public Result<List<SystemBroadcast>> getBroadcasts() {
        return Result.success(worldService.getBroadcasts());
    }

    @GetMapping("/destinations/hot")
    @Operation(summary = "获取热门目的地")
    public Result<List<Destination>> getHotDestinations(
            @RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(hotDestinationService.getHotDestinations(limit));
    }
    
    @GetMapping("/destination/{id}")
    @Operation(summary = "获取目的地详情")
    public Result<com.lvai.vo.DestinationVO> getDestinationDetail(@PathVariable Long id) {
        return Result.success(worldService.getDestinationDetail(id));
    }

    @GetMapping("/topics/hot")
    @Operation(summary = "获取热门话题")
    public Result<List<Topic>> getHotTopics() {
        return Result.success(worldService.getHotTopics());
    }
}
