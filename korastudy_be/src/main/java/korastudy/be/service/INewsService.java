package korastudy.be.service;

import korastudy.be.dto.request.news.CommentRequest;
import korastudy.be.dto.request.news.ReadingProgressRequest;
import korastudy.be.dto.request.news.SaveVocabToFlashcardRequest;
import korastudy.be.dto.response.news.CommentResponse;
import korastudy.be.dto.response.news.NewsArticleResponse;
import korastudy.be.dto.response.news.NewsTopicResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface INewsService {
    
    // Topics
    List<NewsTopicResponse> getAllTopics();
    NewsTopicResponse getTopicById(Long topicId);
    
    // Articles
    Page<NewsArticleResponse> getArticles(
        Long newsTopicId,
        String level,
        String keyword,
        Pageable pageable,
        String username
    );
    
    NewsArticleResponse getArticleById(Long articleId, String username);
    
    // Reading History
    void trackReadingProgress(ReadingProgressRequest request, String username);
    
    // Comments
    Page<CommentResponse> getArticleComments(Long articleId, Pageable pageable);
    CommentResponse createComment(CommentRequest request, String username);
    CommentResponse updateComment(Long commentId, CommentRequest request, String username);
    void deleteComment(Long commentId, String username);
    
    // Flashcard Integration
    Long saveVocabularyToFlashcard(SaveVocabToFlashcardRequest request, String username);
}
