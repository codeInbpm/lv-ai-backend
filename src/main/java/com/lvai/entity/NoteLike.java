package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("note_like")
@Schema(description = "笔记点赞")
public class NoteLike implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
