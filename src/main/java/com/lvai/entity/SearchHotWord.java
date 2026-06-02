package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("search_hot_word")
public class SearchHotWord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private Integer heatScore;
    private Boolean isHot;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
