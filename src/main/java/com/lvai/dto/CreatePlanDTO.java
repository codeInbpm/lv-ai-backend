package com.lvai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "创建行程请求")
public class CreatePlanDTO {

    @NotBlank(message = "出发地不能为空")
    @Schema(description = "出发地")
    private String departure;

    @Schema(description = "出发地经度")
    private BigDecimal departureLng;

    @Schema(description = "出发地纬度")
    private BigDecimal departureLat;

    @NotBlank(message = "目的地不能为空")
    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "目的地经度")
    private BigDecimal destinationLng;

    @Schema(description = "目的地纬度")
    private BigDecimal destinationLat;

    @NotNull(message = "出发日期不能为空")
    @Schema(description = "出发日期")
    private LocalDate startDate;

    @NotNull(message = "天数不能为空")
    @Min(value = 1, message = "天数至少1天")
    @Schema(description = "天数")
    private Integer days;

    @Schema(description = "预算(元)")
    private BigDecimal budget;

    @Schema(description = "人数")
    private Integer peopleCount;

    @Schema(description = "偏好标签列表")
    private List<String> preferences;

    @Schema(description = "其他补充说明")
    private String extraNote;
}
