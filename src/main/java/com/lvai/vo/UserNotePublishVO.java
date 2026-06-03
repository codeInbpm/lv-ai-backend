package com.lvai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "用户笔记发布VO")
public class UserNotePublishVO implements Serializable {

    @Schema(description = "笔记ID (修改时传入)")
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "笔记标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "笔记内容")
    private String content;

    @Schema(description = "封面图片URL")
    private String coverUrl;

    @Schema(description = "多图列表(JSON字符串)")
    private String images;

    @Schema(description = "地点名称")
    private String locationName;

    @Schema(description = "地点地址")
    private String locationAddress;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "话题标签(逗号分隔)")
    private String topicTags;

    @NotBlank(message = "发布类型不能为空")
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

    @Schema(description = "其他扩展数据(JSON)")
    private String extraData;

    @Schema(description = "是否保存为草稿")
    private Boolean isDraft;

    @Schema(description = "关联的草稿箱ID (修改草稿或由草稿发布时传入)")
    private Long draftId;
}
