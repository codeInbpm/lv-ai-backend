package com.lvai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户收藏项")
public class UserCollectionVO {
    @Schema(description = "收藏记录ID")
    private Long id;
    @Schema(description = "目标ID")
    private Long targetId;
    @Schema(description = "目标类型(1:笔记, 2:游记, 3:攻略)")
    private Integer targetType;
    @Schema(description = "具体内容对象(如果已删除则为null)")
    private Object data;
    @Schema(description = "是否已删除")
    private Boolean isDeleted;
}
