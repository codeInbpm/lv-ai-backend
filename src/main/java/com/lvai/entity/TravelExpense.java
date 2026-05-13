package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("travel_expense")
@Schema(description = "记账记录")
public class TravelExpense implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long planId;
    private Long dayId;
    private BigDecimal amount;
    private Integer type; // 1餐饮 2住宿 3交通 4门票 5购物 6其他
    private String remark;
    private LocalDate expenseDate;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
