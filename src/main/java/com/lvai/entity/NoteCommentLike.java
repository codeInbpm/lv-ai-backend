package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("note_comment_like")
@Schema(description = "笔记评论点赞")
public class NoteCommentLike implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
