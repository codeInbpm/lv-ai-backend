package com.lvai.vo;

import com.lvai.entity.TravelDay;
import com.lvai.entity.TravelItem;
import com.lvai.entity.TravelPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "行程详情")
public class PlanDetailVO {

    @Schema(description = "行程主信息")
    private TravelPlan plan;

    @Schema(description = "每日行程列表")
    private List<DayWithItems> days;

    @Data
    @Schema(description = "每日行程(含明细)")
    public static class DayWithItems {
        private TravelDay day;
        private List<TravelItem> items;
    }
}
