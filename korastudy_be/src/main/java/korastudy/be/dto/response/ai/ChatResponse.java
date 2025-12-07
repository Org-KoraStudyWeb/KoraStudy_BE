package korastudy.be.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho chat response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * Câu trả lời từ AI
     */
    private String message;
    
    /**
     * Timestamp
     */
    private Long timestamp;
}
