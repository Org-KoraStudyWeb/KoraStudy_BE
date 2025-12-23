package korastudy.be.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO cho chat response
 * Hỗ trợ cả Gemini trực tiếp và RAG
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
    
    /**
     * Danh sách nguồn tham khảo từ RAG (nếu có)
     * Mỗi source chứa content và metadata
     */
    private List<Map<String, Object>> sources;
}
