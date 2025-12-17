package korastudy.be.service.impl;

import korastudy.be.dto.request.ai.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service tích hợp Gemini API để tạo chatbot AI
 * Hỗ trợ học tiếng Hàn với RAG
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;
    
    @Value("${rag.enabled:false}")
    private boolean ragEnabled;

    private final RestTemplate restTemplate;
    private final RAGService ragService;

    /**
     * Tạo response từ Gemini AI hoặc RAG
     */
    public String generateResponse(String message, List<ChatRequest.ConversationMessage> conversationHistory) {
        try {
            // Kiểm tra xem có dùng RAG không
            if (ragEnabled && ragService.isServiceAvailable()) {
                log.info("Using RAG for response generation");
                return generateResponseWithRAG(message, conversationHistory);
            } else {
                log.info("Using direct Gemini API");
                return generateResponseWithGemini(message, conversationHistory);
            }
            
        } catch (Exception e) {
            log.error("Error generating response", e);
            throw new RuntimeException("Lỗi khi tạo phản hồi: " + e.getMessage());
        }
    }
    
    /**
     * Tạo response sử dụng RAG
     */
    private String generateResponseWithRAG(String message, List<ChatRequest.ConversationMessage> conversationHistory) {
        try {
            // Convert conversation history sang format cho RAG
            List<Map<String, String>> history = conversationHistory != null ? 
                conversationHistory.stream()
                    .map(msg -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("role", msg.getRole());
                        map.put("content", msg.getContent());
                        return map;
                    })
                    .collect(Collectors.toList()) :
                new ArrayList<>();
            
            // Gọi RAG service
            return ragService.queryRAG(message, history);
            
        } catch (Exception e) {
            log.error("Error calling RAG, falling back to direct Gemini", e);
            // Fallback to direct Gemini nếu RAG lỗi
            return generateResponseWithGemini(message, conversationHistory);
        }
    }
    
    /**
     * Tạo response sử dụng Gemini API trực tiếp
     */
    private String generateResponseWithGemini(String message, List<ChatRequest.ConversationMessage> conversationHistory) {
        try {
            // Tạo system prompt
            String systemPrompt = """
                Bạn là một trợ lý AI chuyên về tiếng Hàn trên nền tảng học tiếng Hàn KoraStudy.
                Nhiệm vụ của bạn là:
                - Giúp học viên học tiếng Hàn (từ vựng, ngữ pháp, phát âm, văn hóa)
                - Trả lời các câu hỏi về tiếng Hàn
                - Giải thích ý nghĩa từ vựng, cấu trúc câu
                - Cung cấp ví dụ minh họa
                - Sửa lỗi và đưa ra gợi ý cải thiện
                - QUAN TRỌNG: LUÔN trả lời bằng tiếng Việt, dễ hiểu và thân thiện
                - Khi đưa ví dụ tiếng Hàn, hãy kèm theo phiên âm Hàn-Việt và nghĩa tiếng Việt
                
                Hãy trả lời ngắn gọn, súc tích và hữu ích.
                """;

            // Tạo context từ lịch sử hội thoại
            StringBuilder contextText = new StringBuilder(systemPrompt);
            contextText.append("\n\n");

            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                contextText.append("Lịch sử hội thoại:\n");
                // Chỉ lấy 5 tin nhắn gần nhất
                int startIndex = Math.max(0, conversationHistory.size() - 5);
                for (int i = startIndex; i < conversationHistory.size(); i++) {
                    ChatRequest.ConversationMessage msg = conversationHistory.get(i);
                    String role = "user".equals(msg.getRole()) ? "Người dùng" : "AI";
                    contextText.append(role).append(": ").append(msg.getContent()).append("\n");
                }
                contextText.append("\n");
            }

            contextText.append("Người dùng hỏi: ").append(message).append("\n\nHãy trả lời bằng tiếng Việt:");

            // Tạo request body theo format Gemini API
            Map<String, Object> requestBody = new HashMap<>();
            
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", contextText.toString());
            parts.add(part);
            
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // Gọi Gemini API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String urlWithKey = apiUrl + "?key=" + apiKey;
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("Calling Gemini API...");
            ResponseEntity<Map> response = restTemplate.exchange(
                urlWithKey,
                HttpMethod.POST,
                entity,
                Map.class
            );

            // Parse response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentObj = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> partsObj = (List<Map<String, Object>>) contentObj.get("parts");
                    
                    if (partsObj != null && !partsObj.isEmpty()) {
                        return (String) partsObj.get(0).get("text");
                    }
                }
            }

            throw new RuntimeException("Không nhận được phản hồi hợp lệ từ AI");

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Lỗi khi gọi Gemini API: " + e.getMessage());
        }
    }
}
