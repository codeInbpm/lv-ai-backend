package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("plan_execution_record")
@Schema(description = "行程执行记录(打卡/记账)")
public class PlanExecutionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "行程ID")
    private Long planId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "记录类型: 1打卡 2记账 3日记 4照片")
    private Integer type;

    @Schema(description = "关联的行程明细ID")
    private Long itemId;

    @Schema(description = "关联的每日行程ID")
    private Long dayId;

    @Schema(description = "内容描述")
    private String content;

    @Schema(description = "金额(记账时)")
    private BigDecimal amount;

    @Schema(description = "费用类型: 1餐饮 2住宿 3交通 4门票 5购物 6其他")
    private Integer costType;

    @Schema(description = "图片URLs JSON数组")
    private String images;

    @Schema(description = "位置名称")
    private String locationName;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
