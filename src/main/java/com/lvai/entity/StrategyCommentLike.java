package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("strategy_comment_like")
@Schema(description = "攻略评论点赞")
public class StrategyCommentLike implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "评论ID")
    private Long commentId;

    @Schema(description = "用户ID")
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
