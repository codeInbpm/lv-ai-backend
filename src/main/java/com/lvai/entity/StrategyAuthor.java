package com.lvai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("strategy_author")
public class StrategyAuthor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String source;
    private String externalAuthorId;
    private String nickname;
    private String avatarUrl;
    private Integer followerCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}