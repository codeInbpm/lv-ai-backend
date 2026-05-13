package com.lvai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("strategy_tag")
public class StrategyTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}