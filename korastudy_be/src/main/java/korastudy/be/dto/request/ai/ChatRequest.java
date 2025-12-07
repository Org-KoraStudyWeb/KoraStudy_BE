package korastudy.be.dto.request.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho chat request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    /**
     * Tin nhắn từ người dùng
     */
    private String message;
    
    /**
     * Lịch sử hội thoại (optional)
     */
    private List<ConversationMessage> conversationHistory;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationMessage {
        private String role; // "user" hoặc "assistant"
        private String content;
    }
}
