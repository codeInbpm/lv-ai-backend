package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("travel_item")
@Schema(description = "行程明细项")
public class TravelItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "每日行程ID")
    private Long dayId;

    @Schema(description = "行程计划ID")
    private Long planId;

    @Schema(description = "顺序序号")
    private Integer sortOrder;

    @Schema(description = "类型: 1景点 2美食 3酒店 4交通 5购物 6其他")
    private Integer type;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "建议开始时间")
    private LocalTime startTime;

    @Schema(description = "建议结束时间")
    private LocalTime endTime;

    @Schema(description = "建议停留时长(分钟)")
    private Integer duration;

    @Schema(description = "预计费用")
    private BigDecimal estimatedCost;

    @Schema(description = "实际费用")
    private BigDecimal actualCost;

    @Schema(description = "描述/推荐理由")
    private String description;

    @Schema(description = "AI推荐Tips")
    private String tips;

    @Schema(description = "图片URLs JSON数组")
    private String images;

    @Schema(description = "评分(0-5)")
    private BigDecimal rating;

    @Schema(description = "是否已打卡: 0否 1是")
    private Integer checkedIn;

    @Schema(description = "打卡时间")
    private LocalDateTime checkInTime;

    @Schema(description = "打卡照片URLs JSON数组")
    private String checkInPhotos;

    @Schema(description = "打卡备注")
    private String checkInNote;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际打卡地点")
    private String checkinLocation;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
