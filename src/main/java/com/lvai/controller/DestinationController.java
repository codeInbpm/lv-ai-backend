package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvai.common.Result;
import com.lvai.entity.Destination;
import com.lvai.entity.UserCollection;
import com.lvai.service.IDestinationService;
import com.lvai.service.IUserCollectionService;
import com.lvai.vo.DestinationDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "目的地模块")
@RestController
@RequestMapping("/destination")
@RequiredArgsConstructor
public class DestinationController {

    private final IDestinationService destinationService;
    private final IUserCollectionService userCollectionService;

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

    @PostMapping("/interaction/{id}/collect")
    @Operation(summary = "收藏/取消收藏目的地")
    public Result<Boolean> toggleCollect(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<UserCollection> query = new LambdaQueryWrapper<>();
        query.eq(UserCollection::getTargetId, id)
             .eq(UserCollection::getTargetType, 4) // 4表示景区/目的地
             .eq(UserCollection::getUserId, userId);
        
        UserCollection exist = userCollectionService.getOne(query);
        if (exist != null) {
            userCollectionService.removeById(exist.getId());
            return Result.success(false);
        } else {
            UserCollection collection = new UserCollection();
            collection.setUserId(userId);
            collection.setTargetId(id);
            collection.setTargetType(4);
            userCollectionService.save(collection);
            return Result.success(true);
        }
    }

    @GetMapping("/interaction/{id}/status")
    @Operation(summary = "获取当前目的地/景区收藏状态")
    public Result<Map<String, Boolean>> getInteractionStatus(@PathVariable Long id) {
        Map<String, Boolean> status = new HashMap<>();
        status.put("hasLiked", false);
        status.put("hasCollected", false);
        
        if (StpUtil.isLogin()) {
            long userId = StpUtil.getLoginIdAsLong();
            LambdaQueryWrapper<UserCollection> collectQuery = new LambdaQueryWrapper<>();
            collectQuery.eq(UserCollection::getTargetId, id)
                        .eq(UserCollection::getTargetType, 4)
                        .eq(UserCollection::getUserId, userId);
            status.put("hasCollected", userCollectionService.count(collectQuery) > 0);
        }
        return Result.success(status);
    }
}
