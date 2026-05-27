package com.lvai.service;

import com.lvai.dto.AiPlanModifyDTO;
import com.lvai.vo.AiPlanModifyResultVO;

public interface IAiPlanModifyService {
    AiPlanModifyResultVO modifyPlanIncrementally(AiPlanModifyDTO dto, Long userId);
}
