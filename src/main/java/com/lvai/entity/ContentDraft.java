package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("content_draft")
public class ContentDraft implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer draftType;
    private String title;
    private String content; // JSON string
    private Long planId;
    private String summary;
    private String aiMetadata; // JSON string
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
