package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ranking_list")
public class RankingList {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String coverImage;
    private Long authorId;
    private Integer viewCount;
    private Integer likeCount;
    private String category;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
