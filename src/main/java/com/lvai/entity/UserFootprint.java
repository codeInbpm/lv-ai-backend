package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_footprint")
@Schema(description = "用户打卡足迹")
public class UserFootprint implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long planId;
    private Long itemId;
    private String locationName;
    private BigDecimal lat;
    private BigDecimal lng;
    private String content;
    private String images; // JSON Array String
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
