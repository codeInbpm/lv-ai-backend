package com.lvai.service;
import com.lvai.dto.CompanionChatDTO;

public interface IAiCompanionService {
    String chat(CompanionChatDTO dto, Long userId);
}