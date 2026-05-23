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
    private Long destinationId;  // 对应的目的地ID，用于前端精确跳转
    private Integer viewCount;     // 浏览量
    private Integer likeCount;     // 点赞/推荐数
    private Integer isHot;         // 是否热门推荐
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
