package com.lvai.service;

import com.lvai.dto.CreatePlanDTO;
import com.lvai.vo.AiPlanResultVO;

public interface IAiService {
    AiPlanResultVO generateTravelPlan(CreatePlanDTO dto, Long userId);
    String generateContent(String systemPrompt, String userInput, Long userId);
}
