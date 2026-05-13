package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("destination_food")
@Schema(description = "目的地关联美食")
public class DestinationFood implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long destinationId;
    private String name;
    private BigDecimal lat;
    private BigDecimal lng;
    private String description;
    private String imageUrl;
    private String address;
    private BigDecimal averageCost;
    private Integer isMustEat;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
