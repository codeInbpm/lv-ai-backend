package com.lvai.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class AiPlanModifyDTO {
    @NotNull(message = "行程ID不能为空")
    private Long planId;
    
    @NotNull(message = "天数ID不能为空")
    private Long dayId;
    
    @NotBlank(message = "对话信息不能为空")
    private String message;
    
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
}
