package korastudy.be.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service tích hợp Azure Translator API
 * Hỗ trợ dịch Korean ↔ Vietnamese
 */
@Service
@Slf4j
public class AzureTranslatorService {

    @Value("${azure.translator.key}")
    private String translatorKey;

    @Value("${azure.translator.region:eastasia}")
    private String translatorRegion;

    @Value("${azure.translator.endpoint:https://api.cognitive.microsofttranslator.com}")
    private String translatorEndpoint;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AzureTranslatorService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Dịch văn bản từ Korean sang Vietnamese
     */
    public String translateKoreanToVietnamese(String text) {
        return translate(text, "ko", "vi");
    }

    /**
     * Dịch văn bản từ Vietnamese sang Korean
     */
    public String translateVietnameseToKorean(String text) {
        return translate(text, "vi", "ko");
    }

    /**
     * Dịch văn bản giữa 2 ngôn ngữ
     */
    public String translate(String text, String fromLang, String toLang) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return "";
            }

            String url = translatorEndpoint + "/translate?api-version=3.0&from=" + fromLang + "&to=" + toLang;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Ocp-Apim-Subscription-Key", translatorKey);
            headers.set("Ocp-Apim-Subscription-Region", translatorRegion);

            String requestBody = "[{\"Text\": \"" + escapeJson(text) + "\"}]";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    JsonNode translations = root.get(0).get("translations");
                    if (translations != null && translations.isArray() && translations.size() > 0) {
                        String translatedText = translations.get(0).get("text").asText();
                        log.info("Translated '{}' from {} to {}: '{}'", text, fromLang, toLang, translatedText);
                        return translatedText;
                    }
                }
            }

            log.warn("Translation failed for text: {}", text);
            return text; // Return original text if translation fails

        } catch (Exception e) {
            log.error("Error translating text: {}", text, e);
            return text; // Return original text on error
        }
    }

    /**
     * Phát hiện ngôn ngữ của văn bản
     */
    public String detectLanguage(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return "unknown";
            }

            String url = translatorEndpoint + "/detect?api-version=3.0";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Ocp-Apim-Subscription-Key", translatorKey);
            headers.set("Ocp-Apim-Subscription-Region", translatorRegion);

            String requestBody = "[{\"Text\": \"" + escapeJson(text) + "\"}]";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    String language = root.get(0).get("language").asText();
                    log.info("Detected language for '{}': {}", text, language);
                    return language;
                }
            }

            return "unknown";

        } catch (Exception e) {
            log.error("Error detecting language: {}", text, e);
            return "unknown";
        }
    }

    /**
     * Escape JSON special characters
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
