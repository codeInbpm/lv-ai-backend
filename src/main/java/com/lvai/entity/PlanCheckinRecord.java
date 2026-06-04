package com.lvai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("plan_checkin_record")
public class PlanCheckinRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long dayId;
    private Long itemId;
    private Long userId;
    private String content;
    private BigDecimal cost;
    private String images; // stored as json string
    private String expenses; // stored as json string
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}