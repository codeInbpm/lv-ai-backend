package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("destination_spot")
@Schema(description = "目的地关联景点")
public class DestinationSpot implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long destinationId;
    private String name;
    private BigDecimal score;
    private Integer commentCount;
    private String tags;
    private String openTime;
    private String ticketInfo;
    private BigDecimal lat;
    private BigDecimal lng;
    private String description;
    private String imageUrl;
    private String address;
    private Integer suggestedDuration;
    private Integer isMustVisit;
    private String rankInfo;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
