package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_note")
@Schema(description = "用户笔记")
public class UserNote implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "内容类型：note=笔记, guide=攻略, travel=游记")
    private String type;

    @Schema(description = "攻略天数")
    private Integer days;

    @Schema(description = "人均花费（元）")
    private BigDecimal cost;

    @Schema(description = "适合季节")
    private String season;

    @Schema(description = "出行日期")
    private LocalDate tripDate;

    @Schema(description = "同行伙伴")
    private String companions;

    @Schema(description = "其他扩展数据")
    private String extraData;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "笔记标题")
    private String title;

    @Schema(description = "笔记内容(富文本)")
    private String content;

    @Schema(description = "封面图片URL")
    private String coverUrl;

    @Schema(description = "多图列表(JSON)")
    private String images;

    @Schema(description = "地点名称")
    private String locationName;

    @Schema(description = "地点地址")
    private String locationAddress;

    @Schema(description = "经度")
    private java.math.BigDecimal longitude;

    @Schema(description = "纬度")
    private java.math.BigDecimal latitude;

    @Schema(description = "话题标签(逗号分隔)")
    private String topicTags;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "状态(0草稿 1已发布)")
    private Integer status;

    @Schema(description = "是否精选(0否 1是，同步至攻略)")
    private Integer isFeatured;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "是否删除(0未删 1已删)")
    private Integer deleted;
}
