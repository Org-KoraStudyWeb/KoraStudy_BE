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
 * Controller xử lý API chatbot AI sử dụng Gemini
 * Giúp người dùng học tiếng Hàn với AI
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
     * @return ChatResponse với câu trả lời từ AI
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest request) {
        try {
            log.info("Received chat request: {}", request.getMessage());
            
            String aiResponse = geminiService.generateResponse(
                request.getMessage(), 
                request.getConversationHistory()
            );
            
            ChatResponse response = ChatResponse.builder()
                    .message(aiResponse)
                    .timestamp(System.currentTimeMillis())
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
