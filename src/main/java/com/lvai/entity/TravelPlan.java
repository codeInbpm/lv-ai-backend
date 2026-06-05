package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("travel_plan")
@Schema(description = "旅行计划主表")
public class TravelPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "行程标题")
    private String title;

    @Schema(description = "行程封面图")
    private String coverImage;

    @Schema(description = "出发地")
    private String departure;

    @Schema(description = "出发地经度")
    private BigDecimal departureLng;

    @Schema(description = "出发地纬度")
    private BigDecimal departureLat;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "目的地经度")
    private BigDecimal destinationLng;

    @Schema(description = "目的地纬度")
    private BigDecimal destinationLat;

    @Schema(description = "出发日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "天数")
    private Integer days;

    @Schema(description = "预算(元)")
    private BigDecimal budget;

    @Schema(description = "实际花费(元)")
    private BigDecimal actualCost;

    @Schema(description = "偏好标签JSON数组")
    private String preferences;

    @Schema(description = "人数")
    private Integer peopleCount;

    @Schema(description = "行程描述/AI生成的总结")
    private String description;

    @Schema(description = "AI生成的完整行程JSON")
    private String aiContent;

    @Schema(description = "状态: 0草稿 1未开始 2进行中 3已完成")
    private Integer status;

    @Schema(description = "是否公开: 0私密 1公开")
    private Integer isPublic;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "收藏次数")
    private Integer collectCount;

    @Schema(description = "AI生成日志ID")
    private Long aiLogId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @Schema(description = "行程打卡进度(百分比)")
    private Integer progress;

    @TableLogic
    private Integer deleted;
}
