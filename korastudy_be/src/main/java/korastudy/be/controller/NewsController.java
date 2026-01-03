package korastudy.be.controller;

import korastudy.be.dto.request.news.CommentRequest;
import korastudy.be.dto.request.news.ReadingProgressRequest;
import korastudy.be.dto.request.news.SaveVocabToFlashcardRequest;
import korastudy.be.dto.response.news.CommentResponse;
import korastudy.be.dto.response.news.NewsArticleResponse;
import korastudy.be.dto.response.news.NewsTopicResponse;
import korastudy.be.service.INewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NewsController {

    private final INewsService newsService;

    // ========== NEWS TOPICS ==========
    
    @GetMapping("/news-topics")
    public ResponseEntity<Map<String, Object>> getAllTopics() {
        List<NewsTopicResponse> topics = newsService.getAllTopics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", topics);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/news-topics/{topicId}")
    public ResponseEntity<Map<String, Object>> getTopicById(@PathVariable Long topicId) {
        NewsTopicResponse topic = newsService.getTopicById(topicId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", topic);
        
        return ResponseEntity.ok(response);
    }

    // ========== NEWS ARTICLES ==========
    
    @GetMapping("/articles")
    public ResponseEntity<Map<String, Object>> getArticles(
            @RequestParam(required = false) Long newsTopicId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<NewsArticleResponse> articles = newsService.getArticles(
            newsTopicId, level, keyword, pageable, username
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of(
            "content", articles.getContent(),
            "currentPage", articles.getNumber(),
            "totalPages", articles.getTotalPages(),
            "totalElements", articles.getTotalElements(),
            "pageSize", articles.getSize()
        ));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/articles/{articleId}")
    public ResponseEntity<Map<String, Object>> getArticleById(
            @PathVariable Long articleId,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        NewsArticleResponse article = newsService.getArticleById(articleId, username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", article);
        
        return ResponseEntity.ok(response);
    }

    // ========== READING HISTORY ==========
    
    @PostMapping("/reading-history/track")
    public ResponseEntity<Map<String, String>> trackReadingProgress(
            @RequestBody ReadingProgressRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        newsService.trackReadingProgress(request, username);
        
        return ResponseEntity.ok(Map.of("message", "Progress tracked successfully"));
    }

    // ========== COMMENTS ==========
    
    @GetMapping("/article-comments/article/{articleId}")
    public ResponseEntity<Map<String, Object>> getArticleComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommentResponse> comments = newsService.getArticleComments(articleId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of(
            "content", comments.getContent(),
            "currentPage", comments.getNumber(),
            "totalPages", comments.getTotalPages(),
            "totalElements", comments.getTotalElements()
        ));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/article-comments")
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestBody CommentRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        CommentResponse comment = newsService.createComment(request, username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", comment);
        response.put("message", "Comment created successfully");
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/article-comments/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        CommentResponse comment = newsService.updateComment(commentId, request, username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", comment);
        response.put("message", "Comment updated successfully");
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/article-comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        newsService.deleteComment(commentId, username);
        
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
    }

    // ========== FLASHCARD INTEGRATION ==========
    
    @PostMapping("/articles/save-vocabulary")
    public ResponseEntity<Map<String, Object>> saveVocabularyToFlashcard(
            @RequestBody SaveVocabToFlashcardRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        Long cardId = newsService.saveVocabularyToFlashcard(request, username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of("cardId", cardId));
        response.put("message", "Vocabulary saved to flashcard successfully");
        
        return ResponseEntity.ok(response);
    }
}
