package com.lvai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvai.common.Result;
import com.lvai.dto.PlanQueryDTO;
import com.lvai.entity.TravelPlan;
import com.lvai.service.ITravelPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "发现/攻略模块")
@RestController
@RequestMapping("/discover")
@RequiredArgsConstructor
public class DiscoverController {

    private final ITravelPlanService planService;

    @GetMapping("/list")
    @Operation(summary = "发现-公开行程列表")
    public Result<IPage<TravelPlan>> getPublicPlans(PlanQueryDTO dto) {
        return Result.success(planService.getPublicPlans(dto));
    }

    @GetMapping("/hot")
    @Operation(summary = "热门目的地推荐")
    public Result<?> getHotDestinations() {
        // 可接入第三方数据或内置热门目的地
        return Result.success(java.util.List.of(
                java.util.Map.of("name", "三亚", "image", "", "desc", "阳光沙滩碧海蓝天"),
                java.util.Map.of("name", "成都", "image", "", "desc", "美食之都 休闲天堂"),
                java.util.Map.of("name", "西藏", "image", "", "desc", "离天空最近的地方"),
                java.util.Map.of("name", "云南", "image", "", "desc", "彩云之南 自然奇境"),
                java.util.Map.of("name", "杭州", "image", "", "desc", "人间天堂 山水杭州"),
                java.util.Map.of("name", "厦门", "image", "", "desc", "浪漫海滨 鼓浪屿")
        ));
    }
}
