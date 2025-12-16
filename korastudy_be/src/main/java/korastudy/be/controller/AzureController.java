package korastudy.be.controller;

import korastudy.be.service.impl.AzureSpeechService;
import korastudy.be.service.impl.AzureTranslatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý API Azure Translation và Text-to-Speech
 * Hỗ trợ dịch thuật và phát âm cho flashcard
 */
@RestController
@RequestMapping("/api/v1/azure")
@RequiredArgsConstructor
@Slf4j
public class AzureController {

    private final AzureTranslatorService translatorService;
    private final AzureSpeechService speechService;

    /**
     * Dịch văn bản từ Korean sang Vietnamese
     * POST /api/v1/azure/translate/ko-to-vi
     */
    @PostMapping("/translate/ko-to-vi")
    public ResponseEntity<Map<String, Object>> translateKoreanToVietnamese(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Text cannot be empty"
                ));
            }

            String translatedText = translatorService.translateKoreanToVietnamese(text);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("originalText", text);
            response.put("translatedText", translatedText);
            response.put("fromLanguage", "ko");
            response.put("toLanguage", "vi");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error translating Korean to Vietnamese", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Translation error: " + e.getMessage()
            ));
        }
    }

    /**
     * Dịch văn bản từ Vietnamese sang Korean
     * POST /api/v1/azure/translate/vi-to-ko
     */
    @PostMapping("/translate/vi-to-ko")
    public ResponseEntity<Map<String, Object>> translateVietnameseToKorean(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Text cannot be empty"
                ));
            }

            String translatedText = translatorService.translateVietnameseToKorean(text);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("originalText", text);
            response.put("translatedText", translatedText);
            response.put("fromLanguage", "vi");
            response.put("toLanguage", "ko");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error translating Vietnamese to Korean", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Translation error: " + e.getMessage()
            ));
        }
    }

    /**
     * Phát hiện ngôn ngữ của văn bản
     * POST /api/v1/azure/detect-language
     */
    @PostMapping("/detect-language")
    public ResponseEntity<Map<String, Object>> detectLanguage(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Text cannot be empty"
                ));
            }

            String detectedLanguage = translatorService.detectLanguage(text);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("text", text);
            response.put("detectedLanguage", detectedLanguage);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error detecting language", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Language detection error: " + e.getMessage()
            ));
        }
    }

    /**
     * Tạo audio phát âm tiếng Hàn
     * POST /api/v1/azure/speech/generate
     */
    @PostMapping("/speech/generate")
    public ResponseEntity<Map<String, Object>> generateSpeech(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String voice = request.getOrDefault("voice", "ko-KR-SunHiNeural");

            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Text cannot be empty"
                ));
            }

            String audioBase64 = speechService.generateKoreanSpeech(text, voice);

            if (audioBase64 == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to generate speech"
                ));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("text", text);
            response.put("voice", voice);
            response.put("audioData", audioBase64);
            response.put("audioFormat", "audio/mpeg");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating speech", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Speech generation error: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách giọng đọc tiếng Hàn có sẵn
     * GET /api/v1/azure/speech/voices
     */
    @GetMapping("/speech/voices")
    public ResponseEntity<Map<String, Object>> getAvailableVoices() {
        try {
            String[] voices = speechService.getAvailableKoreanVoices();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("voices", voices);
            response.put("defaultVoice", "ko-KR-SunHiNeural");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting available voices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error retrieving voices: " + e.getMessage()
            ));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Azure services are running");
        result.put("services", Map.of(
            "translator", "available",
            "speech", "available"
        ));

        return ResponseEntity.ok(result);
    }
}
