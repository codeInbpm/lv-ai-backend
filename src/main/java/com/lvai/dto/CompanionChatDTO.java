package com.lvai.dto;
import lombok.Data;
@Data
public class CompanionChatDTO {
    private String sessionId; // usually planId.toString()
    private Long planId;      // optional context
    private String message;
    private String location;  // optional current location
}