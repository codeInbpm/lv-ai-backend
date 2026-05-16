package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("travel_inspiration")
@Schema(description = "出行灵感")
public class TravelInspiration implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer month;
    private String title;
    private String subtitle;
    private String coverUrl;
    private Integer recommendCount;
    private String content;
    private Integer isFeatured;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
