package com.lvai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("crawler_task")
public class CrawlerTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private String source;
    private Integer status;
    private Integer maxPages;
    private LocalDateTime lastRunTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}