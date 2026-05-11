package com.lvai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_generation_log")
@Schema(description = "AI调用记录")
public class AiGenerationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "AI提供商: deepseek/qwen/openai")
    private String provider;

    @Schema(description = "AI模型")
    private String model;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "用户输入")
    private String userInput;

    @Schema(description = "AI输出")
    private String aiOutput;

    @Schema(description = "输入Token数")
    private Integer inputTokens;

    @Schema(description = "输出Token数")
    private Integer outputTokens;

    @Schema(description = "耗时(毫秒)")
    private Long costMs;

    @Schema(description = "状态: 0失败 1成功")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
