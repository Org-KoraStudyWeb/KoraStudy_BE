package korastudy.be.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

/**
 * Service tích hợp Azure Text-to-Speech API
 * Hỗ trợ tạo audio phát âm tiếng Hàn
 */
@Service
@Slf4j
public class AzureSpeechService {

    @Value("${azure.speech.key}")
    private String speechKey;

    @Value("${azure.speech.region:eastasia}")
    private String speechRegion;

    private final RestTemplate restTemplate;

    public AzureSpeechService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Tạo audio phát âm tiếng Hàn từ văn bản
     * @param text Văn bản tiếng Hàn cần phát âm
     * @param voiceName Giọng đọc (mặc định: ko-KR-SunHiNeural - nữ)
     * @return Base64 encoded audio data (MP3)
     */
    public String generateKoreanSpeech(String text, String voiceName) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return null;
            }

            // Default voice: ko-KR-SunHiNeural (female) or ko-KR-InJoonNeural (male)
            if (voiceName == null || voiceName.isEmpty()) {
                voiceName = "ko-KR-SunHiNeural";
            }

            String url = "https://" + speechRegion + ".tts.speech.microsoft.com/cognitiveservices/v1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Ocp-Apim-Subscription-Key", speechKey);
            headers.setContentType(MediaType.valueOf("application/ssml+xml"));
            headers.set("X-Microsoft-OutputFormat", "audio-16khz-128kbitrate-mono-mp3");
            headers.set("User-Agent", "KoraStudy");

            // SSML format for better pronunciation control
            String ssml = String.format(
                "<speak version='1.0' xml:lang='ko-KR'>" +
                "<voice xml:lang='ko-KR' name='%s'>" +
                "%s" +
                "</voice>" +
                "</speak>",
                voiceName,
                escapeXml(text)
            );

            HttpEntity<String> entity = new HttpEntity<>(ssml, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                byte[] audioData = response.getBody();
                String base64Audio = Base64.getEncoder().encodeToString(audioData);
                log.info("Generated speech for text: '{}' with voice: {}", text, voiceName);
                return base64Audio;
            }

            log.warn("Speech generation failed for text: {}", text);
            return null;

        } catch (Exception e) {
            log.error("Error generating speech for text: {}", text, e);
            return null;
        }
    }

    /**
     * Tạo audio với giọng nữ (SunHiNeural)
     */
    public String generateKoreanSpeechFemale(String text) {
        return generateKoreanSpeech(text, "ko-KR-SunHiNeural");
    }

    /**
     * Tạo audio với giọng nam (InJoonNeural)
     */
    public String generateKoreanSpeechMale(String text) {
        return generateKoreanSpeech(text, "ko-KR-InJoonNeural");
    }

    /**
     * Lấy danh sách giọng đọc tiếng Hàn có sẵn
     */
    public String[] getAvailableKoreanVoices() {
        return new String[]{
            "ko-KR-SunHiNeural",    // Nữ (mặc định)
            "ko-KR-InJoonNeural",   // Nam
            "ko-KR-BongJinNeural",  // Nam
            "ko-KR-GookMinNeural",  // Nam
            "ko-KR-JiMinNeural",    // Nữ
            "ko-KR-SeoHyeonNeural", // Nữ
            "ko-KR-SoonBokNeural",  // Nữ
            "ko-KR-YuJinNeural"     // Nữ
        };
    }

    /**
     * Escape XML special characters
     */
    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
