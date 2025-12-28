package korastudy.be.controller;

import korastudy.be.dto.request.ai.ChatRequest;
import korastudy.be.dto.response.ai.ChatResponse;
import korastudy.be.service.impl.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý API chatbot AI sử dụng Gemini + RAG
 * Giúp người dùng học tiếng Hàn với AI
 * 
 * Flow:
 * 1. Nhận request từ Frontend
 * 2. GeminiService kiểm tra RAG enabled hay không
 * 3. Nếu RAG enabled và available → gọi RAG service
 * 4. Nếu không → gọi Gemini API trực tiếp
 * 5. Trả về response với sources (nếu dùng RAG)
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {
    
    private final GeminiService geminiService;

    /**
     * Endpoint chat với AI
     * POST /api/v1/ai/chat
     * 
     * @param request - ChatRequest chứa tin nhắn và lịch sử hội thoại
     * @return ChatResponse với câu trả lời từ AI và sources (nếu có)
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest request) {
        try {
            log.info("Received chat request: {}", request.getMessage());
            
            // Gọi service để lấy response với sources
            GeminiService.AIResponse aiResponse = geminiService.generateResponseWithSources(
                request.getMessage(), 
                request.getConversationHistory()
            );
            
            // Build response với sources
            ChatResponse response = ChatResponse.builder()
                    .message(aiResponse.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .sources(aiResponse.getSources())
                    .build();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "AI response generated successfully");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error generating AI response", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi xử lý yêu cầu: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "AI service is running");
        result.put("data", "OK");
        
        return ResponseEntity.ok(result);
    }
}
