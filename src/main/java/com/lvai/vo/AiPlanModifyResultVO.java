package com.lvai.vo;

import lombok.Data;

@Data
public class AiPlanModifyResultVO {
    private String explanation; // AI针对本次行程微调给出的甜美总结话语
    private Boolean success;    // 是否修改成功
    private Long affectedDayId; // 受影响的天数ID
}
