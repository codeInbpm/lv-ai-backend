package com.lvai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI生成行程结果")
public class AiPlanResultVO {

    @Schema(description = "行程标题")
    private String title;

    @Schema(description = "行程概述")
    private String description;

    @Schema(description = "每日行程")
    private List<DayPlan> days;

    @Schema(description = "AI日志ID")
    private Long aiLogId;

    @Data
    public static class DayPlan {
        private Integer dayIndex;
        private String title;
        private String description;
        private List<ItemPlan> items;
    }

    @Data
    public static class ItemPlan {
        private Integer sortOrder;
        private Integer type;
        private String name;
        private String address;
        private Double lng;
        private Double lat;
        private String startTime;
        private String endTime;
        private Integer duration;
        private Double estimatedCost;
        private String description;
        private String tips;
    }
}
