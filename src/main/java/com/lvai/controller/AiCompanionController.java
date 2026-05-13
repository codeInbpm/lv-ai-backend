package com.lvai.controller;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.Result;
import com.lvai.dto.CompanionChatDTO;
import com.lvai.entity.AiChatHistory;
import com.lvai.service.IAiChatHistoryService;
import com.lvai.service.IAiCompanionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/companion")
@RequiredArgsConstructor
public class AiCompanionController {

    private final IAiCompanionService companionService;
    private final IAiChatHistoryService chatHistoryService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody CompanionChatDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        String answer = companionService.chat(dto, userId);
        return Result.success("success", answer);
    }

    @GetMapping("/history")
    public Result<List<AiChatHistory>> getHistory(@RequestParam String sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        List<AiChatHistory> records = chatHistoryService.list(
                new LambdaQueryWrapper<AiChatHistory>()
                        .eq(AiChatHistory::getSessionId, sessionId)
                        .eq(AiChatHistory::getUserId, userId)
                        .orderByAsc(AiChatHistory::getCreateTime)
        );
        return Result.success(records);
    }
}