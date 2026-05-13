package com.lvai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("strategy_post_tag")
public class StrategyPostTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long tagId;
}