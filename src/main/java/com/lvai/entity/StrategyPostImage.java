package com.lvai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("strategy_post_image")
public class StrategyPostImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String imageUrl;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}