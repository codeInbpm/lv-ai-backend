package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("user_browsing_history")
public class UserBrowsingHistory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long targetId;

    /**
     * 1-行程 2-景点/目的地 3-攻略 4-笔记
     */
    private Integer targetType;

    private String title;

    private String coverUrl;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime viewTime;
}
