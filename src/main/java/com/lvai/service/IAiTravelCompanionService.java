package com.lvai.service;

public interface IAiTravelCompanionService {
    /**
     * 打卡时调用，AI智能推荐费用分类、预计花费、打卡文案、预算预警及下一项消费Tips
     */
    String callAiForCheckin(Long itemId, String userInput, Long userId);

    /**
     * 每日或全程结束时调用，生成消费总结与朋友圈/小红书分享文案
     */
    String generateDailySummary(Long planId, Long dayId, Long userId);
}
