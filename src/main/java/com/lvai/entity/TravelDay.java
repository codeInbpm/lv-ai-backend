package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("travel_day")
@Schema(description = "每日行程")
public class TravelDay {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "行程计划ID")
    private Long planId;

    @Schema(description = "第几天")
    private Integer dayIndex;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "当天主题/标题")
    private String title;

    @Schema(description = "当天描述")
    private String description;

    @Schema(description = "当天天气(执行中更新)")
    private String weather;

    @Schema(description = "当天心情(执行中记录)")
    private String mood;

    @Schema(description = "当天日记")
    private String diary;

    @Schema(description = "是否已完成: 0否 1是")
    private Integer finished;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
