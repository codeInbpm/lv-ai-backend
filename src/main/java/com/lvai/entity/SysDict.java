package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict")
@Schema(description = "系统字典实体")
public class SysDict implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "字典标签(展示名称)")
    private String dictLabel;

    @Schema(description = "字典键值(存储编码)")
    private String dictValue;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否默认值(0-否，1-是)")
    private Integer isDefault;

    @Schema(description = "状态(0-正常，1-停用)")
    private Integer status;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
