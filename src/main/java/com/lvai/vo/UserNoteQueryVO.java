package com.lvai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户笔记查询参数VO")
public class UserNoteQueryVO implements Serializable {

    @Schema(description = "内容类型：note=笔记, guide=攻略, travel=游记")
    private String type;

    @Schema(description = "关键字搜索(标题/内容)")
    private String keyword;

    @Schema(description = "状态(0草稿 1已发布)")
    private Integer status;
}
