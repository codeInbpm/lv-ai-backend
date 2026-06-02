package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ranking_list_item")
public class RankingListItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long rankingId;
    private Integer rankNum;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String targetImage;
    private BigDecimal rating;
    private Integer commentCount;
    private String recommendReason;
    private LocalDateTime createTime;
}
