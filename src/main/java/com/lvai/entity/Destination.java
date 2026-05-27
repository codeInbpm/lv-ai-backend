package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("destination")
@Schema(description = "热门目的地")
public class Destination implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private BigDecimal lat;
    private BigDecimal lng;
    private String description;
    private String imageUrl;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
    
    @Schema(description = "动态热度分数")
    private Long hotScore;
    
    @Schema(description = "打卡次数")
    private Integer checkinCount;
    
    @Schema(description = "笔记/攻略数量")
    private Integer noteCount;
    
    private Integer sortOrder;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
