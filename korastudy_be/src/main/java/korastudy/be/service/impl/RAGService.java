package korastudy.be.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service để gọi Python RAG Service
 * Tích hợp dữ liệu TOPIK vào chatbot
 */
@Service
@Slf4j
public class RAGService {
    
    @Value("${rag.service.url:http://localhost:5000}")
    private String ragServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public RAGService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Kiểm tra RAG service có đang chạy không
     */
    public boolean isServiceAvailable() {
        try {
            String url = ragServiceUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return "ok".equals(body.get("status")) && 
                       Boolean.TRUE.equals(body.get("initialized"));
            }
            
            return false;
            
        } catch (Exception e) {
            log.warn("RAG service not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Response từ RAG bao gồm câu trả lời và sources
     */
    public static class RAGResponse {
        private String answer;
        private List<Map<String, Object>> sources;
        
        public RAGResponse(String answer, List<Map<String, Object>> sources) {
            this.answer = answer;
            this.sources = sources != null ? sources : new ArrayList<>();
        }
        
        public String getAnswer() { return answer; }
        public List<Map<String, Object>> getSources() { return sources; }
    }
    
    /**
     * Gọi Python RAG service để truy vấn
     * 
     * @param message - Câu hỏi của user
     * @param conversationHistory - Lịch sử hội thoại
     * @return RAGResponse chứa câu trả lời và sources
     */
    public RAGResponse queryRAGWithSources(String message, List<Map<String, String>> conversationHistory) {
        try {
            String url = ragServiceUrl + "/query";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("conversationHistory", conversationHistory != null ? conversationHistory : new ArrayList<>());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("Calling RAG service: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                if (Boolean.TRUE.equals(body.get("success"))) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    
                    if (data != null && data.get("message") != null) {
                        String answer = (String) data.get("message");
                        
                        // Lấy sources từ response
                        List<Map<String, Object>> sources = new ArrayList<>();
                        List<?> rawSources = (List<?>) data.get("sources");
                        if (rawSources != null) {
                            for (Object src : rawSources) {
                                if (src instanceof Map) {
                                    sources.add((Map<String, Object>) src);
                                }
                            }
                            log.info("RAG used {} source documents", sources.size());
                        }
                        
                        return new RAGResponse(answer, sources);
                    }
                }
            }
            
            throw new RuntimeException("RAG service returned invalid response");
            
        } catch (RestClientException e) {
            log.error("Error calling RAG service", e);
            throw new RuntimeException("Không thể kết nối với RAG service: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling RAG service", e);
            throw new RuntimeException("Lỗi khi gọi RAG service: " + e.getMessage());
        }
    }
    
    /**
     * Gọi Python RAG service để truy vấn (backward compatible)
     * 
     * @param message - Câu hỏi của user
     * @param conversationHistory - Lịch sử hội thoại
     * @return Câu trả lời từ RAG
     */
    public String queryRAG(String message, List<Map<String, String>> conversationHistory) {
        return queryRAGWithSources(message, conversationHistory).getAnswer();
    }
    
    /**
     * Khởi tạo RAG với dữ liệu
     * 
     * @param filePath - Đường dẫn đến file dữ liệu JSON
     */
    public void initializeRAG(String filePath) {
        try {
            String url = ragServiceUrl + "/initialize";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("file_path", filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("Initializing RAG with data from: {}", filePath);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("RAG initialized successfully");
            } else {
                log.error("Failed to initialize RAG: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error initializing RAG", e);
            throw new RuntimeException("Lỗi khi khởi tạo RAG: " + e.getMessage());
        }
    }
    
    /**
     * Reload dữ liệu mới
     */
    public void reloadRAG(String filePath) {
        try {
            String url = ragServiceUrl + "/reload";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("file_path", filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("Reloading RAG with data from: {}", filePath);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("RAG reloaded successfully");
            } else {
                log.error("Failed to reload RAG: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error reloading RAG", e);
            throw new RuntimeException("Lỗi khi reload RAG: " + e.getMessage());
        }
    }
}
