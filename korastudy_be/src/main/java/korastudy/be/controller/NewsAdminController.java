package korastudy.be.controller;

import korastudy.be.dto.request.news.ImportArticleRequest;
import korastudy.be.dto.response.ApiSuccess;
import korastudy.be.dto.response.news.NewsTopicResponse;
import korastudy.be.entity.Enum.ArticleStatus;
import korastudy.be.entity.news.NewsArticle;
import korastudy.be.entity.news.NewsTopic;
import korastudy.be.repository.news.NewsArticleRepository;
import korastudy.be.repository.news.NewsTopicRepository;
import korastudy.be.service.impl.NewsWebCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/news")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NewsAdminController {

    private final NewsWebCrawlerService crawlerService;
    private final NewsArticleRepository articleRepository;
    private final NewsTopicRepository topicRepository;

    /**
     * Import article from URL - Admin nhập URL để crawl
     */
    @PostMapping("/import-from-url")
    public ResponseEntity<Map<String, Object>> importArticleFromUrl(
            @RequestBody ImportArticleRequest request
    ) {
        try {
            log.info("📥 Admin importing article from URL: {}", request.getUrl());
            
            NewsArticle article = crawlerService.crawlSingleArticle(request.getUrl());
            
            if (article == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Không thể crawl bài báo từ URL này");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Set status = DRAFT để admin review trước khi publish
            article.setStatus(ArticleStatus.DRAFT);
            
            // Gán topic nếu có
            if (request.getTopicId() != null) {
                NewsTopic topic = topicRepository.findById(request.getTopicId()).orElse(null);
                article.setNewsTopic(topic);
            }
            
            // Lưu draft article
            NewsArticle saved = articleRepository.save(article);
            
            // Trả về data để fill vào form
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Crawl thành công! Vui lòng kiểm tra và chỉnh sửa trước khi publish");
            response.put("data", toArticleMap(saved));
            
            log.info("✅ Article imported successfully as DRAFT - ID: {}", saved.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Import failed: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi crawl bài báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all articles (with filters) - Danh sách bài báo trong admin
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllArticles(
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<NewsArticle> articles = articleRepository.findAllWithFiltersForAdmin(
                topicId, status, level, keyword, pageable
            );
            
            List<Map<String, Object>> content = articles.getContent().stream()
                .map(this::toArticleMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", content);
            response.put("currentPage", articles.getNumber());
            response.put("totalItems", articles.getTotalElements());
            response.put("totalPages", articles.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching articles: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Lỗi khi lấy danh sách bài báo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get article by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getArticleById(@PathVariable Long id) {
        try {
            NewsArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
            
            return ResponseEntity.ok(toArticleMap(article));
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Update article - Admin chỉnh sửa article
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateArticle(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updateData
    ) {
        try {
            NewsArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
            
            // Update fields từ request
            if (updateData.containsKey("title")) {
                article.setTitle((String) updateData.get("title"));
            }
            if (updateData.containsKey("titleVietnamese")) {
                article.setTitleVietnamese((String) updateData.get("titleVietnamese"));
            }
            if (updateData.containsKey("content")) {
                article.setContent((String) updateData.get("content"));
            }
            if (updateData.containsKey("contentVietnamese")) {
                article.setContentVietnamese((String) updateData.get("contentVietnamese"));
            }
            if (updateData.containsKey("summary")) {
                article.setSummary((String) updateData.get("summary"));
            }
            if (updateData.containsKey("difficultyLevel")) {
                article.setDifficultyLevel((String) updateData.get("difficultyLevel"));
            }
            if (updateData.containsKey("thumbnailUrl")) {
                article.setThumbnailUrl((String) updateData.get("thumbnailUrl"));
            }
            if (updateData.containsKey("audioUrl")) {
                article.setAudioUrl((String) updateData.get("audioUrl"));
            }
            if (updateData.containsKey("topicId")) {
                Object topicIdObj = updateData.get("topicId");
                if (topicIdObj != null) {
                    Long topicId = Long.valueOf(topicIdObj.toString());
                    NewsTopic topic = topicRepository.findById(topicId).orElse(null);
                    article.setNewsTopic(topic);
                }
            }
            
            NewsArticle updated = articleRepository.save(article);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật thành công");
            response.put("data", toArticleMap(updated));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Update failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi cập nhật bài báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Change article status (DRAFT → PUBLISHED)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiSuccess> changeArticleStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        try {
            NewsArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
            
            ArticleStatus newStatus = ArticleStatus.valueOf(status.toUpperCase());
            article.setStatus(newStatus);
            
            if (newStatus == ArticleStatus.PUBLISHED && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
            
            articleRepository.save(article);
            
            return ResponseEntity.ok(ApiSuccess.of("Đã chuyển trạng thái sang " + status));
            
        } catch (Exception e) {
            log.error("Status change failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiSuccess.of("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * Delete article
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiSuccess> deleteArticle(@PathVariable Long id) {
        try {
            articleRepository.deleteById(id);
            return ResponseEntity.ok(ApiSuccess.of("Đã xóa bài báo"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiSuccess.of("Lỗi khi xóa bài báo"));
        }
    }

    // ============ TOPIC MANAGEMENT ============

    /**
     * Get all topics
     */
    @GetMapping("/topics")
    public ResponseEntity<List<NewsTopicResponse>> getAllTopics() {
        List<NewsTopicResponse> topics = topicRepository.findAll().stream()
            .map(this::toTopicResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(topics);
    }

    /**
     * Create topic
     */
    @PostMapping("/topics")
    public ResponseEntity<NewsTopicResponse> createTopic(@RequestBody Map<String, String> topicRequest) {
        // Frontend gửi: name, nameKorean, description, iconUrl
        String name = topicRequest.get("name");
        String nameKorean = topicRequest.get("nameKorean");
        String description = topicRequest.get("description");
        String iconUrl = topicRequest.get("iconUrl");
        
        NewsTopic topic = NewsTopic.builder()
            .title(name != null ? name : "")
            .description(description != null ? description : "")
            .icon(iconUrl != null ? iconUrl : "📰")
            .build();
        
        NewsTopic saved = topicRepository.save(topic);
        
        return ResponseEntity.ok(toTopicResponse(saved));
    }

    /**
     * Update topic
     */
    @PutMapping("/topics/{id}")
    public ResponseEntity<NewsTopicResponse> updateTopic(
            @PathVariable Long id,
            @RequestBody Map<String, String> topicUpdate
    ) {
        NewsTopic topic = topicRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Topic not found"));
        
        if (topicUpdate.containsKey("name")) {
            topic.setTitle(topicUpdate.get("name"));
        }
        if (topicUpdate.containsKey("description")) {
            topic.setDescription(topicUpdate.get("description"));
        }
        if (topicUpdate.containsKey("iconUrl")) {
            topic.setIcon(topicUpdate.get("iconUrl"));
        }
        
        NewsTopic updated = topicRepository.save(topic);
        
        return ResponseEntity.ok(toTopicResponse(updated));
    }

    /**
     * Delete topic
     */
    @DeleteMapping("/topics/{id}")
    public ResponseEntity<ApiSuccess> deleteTopic(@PathVariable Long id) {
        topicRepository.deleteById(id);
        return ResponseEntity.ok(ApiSuccess.of("Đã xóa chủ đề"));
    }

    // ============ Helper methods ============
    
    private Map<String, Object> toArticleMap(NewsArticle article) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", article.getId());
        map.put("title", article.getTitle());
        map.put("titleVietnamese", article.getTitleVietnamese());
        map.put("content", article.getContent());
        map.put("contentVietnamese", article.getContentVietnamese());
        map.put("summary", article.getSummary());
        map.put("source", article.getSource());
        map.put("sourceUrl", article.getSourceUrl());
        map.put("thumbnailUrl", article.getThumbnailUrl());
        map.put("audioUrl", article.getAudioUrl());
        map.put("difficultyLevel", article.getDifficultyLevel());
        map.put("status", article.getStatus());
        map.put("readCount", article.getReadCount());
        map.put("publishedAt", article.getPublishedAt());
        map.put("createdAt", article.getCreatedAt());
        map.put("topic", article.getNewsTopic() != null ? toTopicResponse(article.getNewsTopic()) : null);
        map.put("topicId", article.getNewsTopic() != null ? article.getNewsTopic().getId() : null);
        return map;
    }

    private NewsTopicResponse toTopicResponse(NewsTopic topic) {
        return NewsTopicResponse.builder()
            .id(topic.getId())
            .name(topic.getTitle())  // Frontend expects 'name'
            .title(topic.getTitle()) // Keep for backward compatibility
            .description(topic.getDescription())
            .icon(topic.getIcon())
            .articleCount(topic.getArticles() != null ? topic.getArticles().size() : 0)
            .build();
    }
}
