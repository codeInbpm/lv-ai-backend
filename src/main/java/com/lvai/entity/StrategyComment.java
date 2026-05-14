package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("strategy_comment")
@Schema(description = "攻略评论")
public class StrategyComment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "攻略ID")
    private Long strategyId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID(0为一级评论)")
    private Long parentId;

    @Schema(description = "被回复的具体评论ID")
    private Long replyToId;

    @Schema(description = "被回复的用户ID")
    private Long replyToUserId;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableLogic
    @Schema(description = "是否删除(0未删 1已删)")
    private Integer deleted;
}
