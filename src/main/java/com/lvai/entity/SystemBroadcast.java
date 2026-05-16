package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("system_broadcast")
@Schema(description = "系统广播")
public class SystemBroadcast implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;
    private String linkUrl;
    private Integer priority;
    private Integer isActive;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
