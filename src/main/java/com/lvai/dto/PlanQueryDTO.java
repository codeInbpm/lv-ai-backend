package com.lvai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "行程查询请求")
public class PlanQueryDTO {
    @Schema(description = "关键词")
    private String keyword;
    @Schema(description = "目的地")
    private String destination;
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}
