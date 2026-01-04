package korastudy.be.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service để gọi Korean Dictionary API (krdict.korean.go.kr)
 * API Reference: https://krdict.korean.go.kr/kor/openApi/openApiInfo
 * Kết hợp với Azure Translator để dịch nghĩa sang tiếng Việt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KrDictService {

    private static final String API_KEY = "4C17A0A28A021E2C9AA6604CD48370A2";
    private static final String BASE_URL = "https://krdict.korean.go.kr/api/search";
    
    private final AzureTranslatorService translatorService;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Tra từ trong từ điển Hàn Quốc
     * @param word Từ cần tra
     * @return Map chứa thông tin từ điển
     */
    public Map<String, Object> lookupWord(String word) {
        Map<String, Object> result = new HashMap<>();
        result.put("word", word);
        
        try {
            // Build URL - chỉ cần key và query
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("key", API_KEY)
                .queryParam("q", word)
                .build()
                .toUriString();
            
            log.info("🔍 Calling KrDict API for word: {} | URL: {}", word, url);
            
            // Gọi API
            String xmlResponse = restTemplate.getForObject(url, String.class);
            
            if (xmlResponse == null || xmlResponse.isEmpty()) {
                result.put("meaning", "Không tìm thấy nghĩa");
                result.put("found", false);
                return result;
            }
            
            // Parse XML response và dịch sang tiếng Việt
            return parseXmlResponse(xmlResponse, word);
            
        } catch (Exception e) {
            log.error("❌ KrDict API error for word '{}': {}", word, e.getMessage());
            result.put("meaning", "Lỗi khi tra từ điển");
            result.put("found", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * Parse XML response từ KrDict API và dịch nghĩa sang tiếng Việt
     */
    private Map<String, Object> parseXmlResponse(String xml, String originalWord) {
        Map<String, Object> result = new HashMap<>();
        result.put("word", originalWord);
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            
            // Kiểm tra số lượng kết quả
            NodeList totalNodes = doc.getElementsByTagName("total");
            if (totalNodes.getLength() > 0) {
                int total = Integer.parseInt(totalNodes.item(0).getTextContent());
                if (total == 0) {
                    result.put("meaning", "Không tìm thấy nghĩa");
                    result.put("found", false);
                    return result;
                }
            }
            
            // Lấy danh sách item
            NodeList items = doc.getElementsByTagName("item");
            if (items.getLength() == 0) {
                result.put("meaning", "Không tìm thấy nghĩa");
                result.put("found", false);
                return result;
            }
            
            // Lấy item đầu tiên (phổ biến nhất)
            Element firstItem = (Element) items.item(0);
            
            // Lấy từ gốc
            String targetWord = getElementText(firstItem, "word");
            result.put("koreanWord", targetWord != null ? targetWord : originalWord);
            
            // Lấy phiên âm (pronunciation)
            String pronunciation = getElementText(firstItem, "pronunciation");
            if (pronunciation != null && !pronunciation.isEmpty()) {
                result.put("pronunciation", pronunciation);
            }
            
            // Lấy Hanja (origin - chữ Hán)
            String hanja = getElementText(firstItem, "origin");
            if (hanja != null && !hanja.isEmpty()) {
                result.put("hanja", hanja);
            }
            
            // Lấy cấp độ từ vựng
            String wordGrade = getElementText(firstItem, "word_grade");
            if (wordGrade != null && !wordGrade.isEmpty()) {
                result.put("level", wordGrade);
            }
            
            // Lấy từ loại (pos - part of speech)
            String pos = getElementText(firstItem, "pos");
            if (pos != null && !pos.isEmpty()) {
                result.put("partOfSpeech", translatePos(pos));
                result.put("partOfSpeechKorean", pos);
            }
            
            // Lấy định nghĩa tiếng Hàn và dịch sang tiếng Việt
            List<Map<String, String>> definitions = new ArrayList<>();
            NodeList senseNodes = firstItem.getElementsByTagName("sense");
            
            StringBuilder koreanMeaningBuilder = new StringBuilder();
            StringBuilder vietnameseMeaningBuilder = new StringBuilder();
            
            for (int i = 0; i < Math.min(senseNodes.getLength(), 3); i++) {
                Element sense = (Element) senseNodes.item(i);
                
                // Lấy nghĩa tiếng Hàn
                String koreanDef = getElementText(sense, "definition");
                
                if (koreanDef != null && !koreanDef.isEmpty()) {
                    if (koreanMeaningBuilder.length() > 0) {
                        koreanMeaningBuilder.append("; ");
                    }
                    koreanMeaningBuilder.append(koreanDef);
                    
                    Map<String, String> def = new HashMap<>();
                    def.put("korean", koreanDef);
                    definitions.add(def);
                }
            }
            
            // Dịch nghĩa tiếng Hàn sang tiếng Việt bằng Azure
            String koreanMeaning = koreanMeaningBuilder.toString();
            if (!koreanMeaning.isEmpty()) {
                try {
                    String vietnameseMeaning = translatorService.translateKoreanToVietnamese(koreanMeaning);
                    result.put("meaning", vietnameseMeaning);
                    result.put("koreanMeaning", koreanMeaning);
                    
                    // Cập nhật definitions với nghĩa tiếng Việt
                    if (!definitions.isEmpty()) {
                        // Dịch từng definition nếu cần
                        for (Map<String, String> def : definitions) {
                            String viDef = translatorService.translateKoreanToVietnamese(def.get("korean"));
                            def.put("vietnamese", viDef);
                        }
                    }
                    
                    log.info("✅ KrDict found & translated: {} -> {}", originalWord, vietnameseMeaning);
                } catch (Exception e) {
                    log.warn("Translation failed, using Korean meaning: {}", e.getMessage());
                    result.put("meaning", koreanMeaning);
                    result.put("koreanMeaning", koreanMeaning);
                }
            } else {
                result.put("meaning", "Không tìm thấy nghĩa");
            }
            
            result.put("definitions", definitions);
            result.put("found", !koreanMeaning.isEmpty());
            
            // Lấy từ liên quan từ các item khác
            List<String> relatedWords = new ArrayList<>();
            for (int i = 1; i < Math.min(items.getLength(), 4); i++) {
                Element item = (Element) items.item(i);
                String relatedWord = getElementText(item, "word");
                if (relatedWord != null && !relatedWord.equals(originalWord) && !relatedWords.contains(relatedWord)) {
                    relatedWords.add(relatedWord);
                }
            }
            
            if (!relatedWords.isEmpty()) {
                result.put("relatedWords", relatedWords);
            }
            
        } catch (Exception e) {
            log.error("Error parsing KrDict XML: {}", e.getMessage());
            result.put("meaning", "Lỗi phân tích dữ liệu từ điển");
            result.put("found", false);
        }
        
        return result;
    }

    /**
     * Helper để lấy text từ element
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

    /**
     * Dịch từ loại sang tiếng Việt
     */
    private String translatePos(String pos) {
        Map<String, String> posMap = new HashMap<>();
        posMap.put("명사", "Danh từ");
        posMap.put("동사", "Động từ");
        posMap.put("형용사", "Tính từ");
        posMap.put("부사", "Phó từ");
        posMap.put("관형사", "Định từ");
        posMap.put("감탄사", "Thán từ");
        posMap.put("조사", "Trợ từ");
        posMap.put("어미", "Vĩ tố");
        posMap.put("접사", "Tiếp tố");
        posMap.put("의존 명사", "Danh từ phụ thuộc");
        posMap.put("보조 동사", "Động từ bổ trợ");
        posMap.put("보조 형용사", "Tính từ bổ trợ");
        posMap.put("수사", "Số từ");
        posMap.put("대명사", "Đại từ");
        
        return posMap.getOrDefault(pos, pos);
    }
}
