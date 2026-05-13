package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("strategy_post")
@Schema(description = "精选攻略")
public class StrategyPost implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String coverUrl;
    private String content;
    private String destination;
    private Integer days;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private Integer status;
    private String source;
    private String externalUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
