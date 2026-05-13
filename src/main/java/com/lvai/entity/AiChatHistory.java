package com.lvai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_chat_history")
public class AiChatHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private Long userId;
    private String role;
    private String content;
    private Integer tokens;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}