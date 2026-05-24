package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_footprint")
@Schema(description = "用户足迹")
public class UserFootprint {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "关联行程ID")
    private Long planId;

    @Schema(description = "关联行程明细ID")
    private Long itemId;

    @Schema(description = "打卡地名称")
    private String locationName;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "打卡感想")
    private String content;

    @Schema(description = "打卡图片URLs(JSON数组)")
    private String images;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "打卡时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
