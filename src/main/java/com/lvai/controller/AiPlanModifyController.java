package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lvai.common.Result;
import com.lvai.dto.AiPlanModifyDTO;
import com.lvai.service.IAiPlanModifyService;
import com.lvai.vo.AiPlanModifyResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 行程微调增量修改模块")
@RestController
@RequestMapping("/ai/plan")
@RequiredArgsConstructor
public class AiPlanModifyController {

    private final IAiPlanModifyService aiPlanModifyService;

    @PostMapping("/modify")
    @Operation(summary = "AI 对话式增量修改行程细项")
    public Result<AiPlanModifyResultVO> modifyPlan(@Valid @RequestBody AiPlanModifyDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        AiPlanModifyResultVO result = aiPlanModifyService.modifyPlanIncrementally(dto, userId);
        return Result.success("行程智能修改成功", result);
    }
}
