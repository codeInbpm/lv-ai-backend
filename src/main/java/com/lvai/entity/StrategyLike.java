package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("strategy_like")
@Schema(description = "攻略点赞")
public class StrategyLike implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "攻略ID")
    private Long strategyId;

    @Schema(description = "用户ID")
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "点赞时间")
    private LocalDateTime createTime;
}
