package korastudy.be.service.impl;

import korastudy.be.dto.request.news.CommentRequest;
import korastudy.be.dto.request.news.ReadingProgressRequest;
import korastudy.be.dto.request.news.SaveVocabToFlashcardRequest;
import korastudy.be.dto.response.news.*;
import korastudy.be.entity.FlashCard.Card;
import korastudy.be.entity.FlashCard.SetCard;
import korastudy.be.entity.news.*;
import korastudy.be.entity.User.User;
import korastudy.be.exception.BadRequestException;
import korastudy.be.exception.NotFoundException;
import korastudy.be.repository.UserRepository;
import korastudy.be.repository.flashcard.CardRepository;
import korastudy.be.repository.flashcard.SetCardRepository;
import korastudy.be.repository.news.*;
import korastudy.be.service.INewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService implements INewsService {

    private final NewsTopicRepository newsTopicRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final NewsVocabularyRepository newsVocabularyRepository;
    private final UserReadingHistoryRepository readingHistoryRepository;
    private final ArticleCommentRepository commentRepository;
    private final NewsFlashcardMappingRepository mappingRepository;
    private final UserRepository userRepository;
    private final SetCardRepository setCardRepository;
    private final CardRepository cardRepository;

    @Override
    public List<NewsTopicResponse> getAllTopics() {
        return newsTopicRepository.findAll().stream()
            .map(this::toTopicResponse)
            .collect(Collectors.toList());
    }

    @Override
    public NewsTopicResponse getTopicById(Long topicId) {
        NewsTopic topic = newsTopicRepository.findById(topicId)
            .orElseThrow(() -> new NotFoundException("Topic not found"));
        return toTopicResponse(topic);
    }

    @Override
    public Page<NewsArticleResponse> getArticles(
        Long newsTopicId,
        String level,
        String keyword,
        Pageable pageable,
        String username
    ) {
        User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        
        Page<NewsArticle> articles = newsArticleRepository.findWithFilters(
            newsTopicId, level, keyword, pageable
        );
        
        return articles.map(article -> toArticleResponse(article, user));
    }

    @Override
    @Transactional
    public NewsArticleResponse getArticleById(Long articleId, String username) {
        NewsArticle article = newsArticleRepository.findById(articleId)
            .orElseThrow(() -> new NotFoundException("Article not found"));
        
        User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        
        // Increment read count
        article.incrementReadCount();
        newsArticleRepository.save(article);
        
        // Track reading if user is logged in
        if (user != null) {
            trackInitialRead(article, user);
        }
        
        return toArticleResponse(article, user);
    }

    @Override
    @Transactional
    public void trackReadingProgress(ReadingProgressRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        NewsArticle article = newsArticleRepository.findById(request.getArticleId())
            .orElseThrow(() -> new NotFoundException("Article not found"));
        
        UserReadingHistory history = readingHistoryRepository
            .findByUserIdAndArticleId(user.getId(), article.getId())
            .orElseGet(() -> {
                UserReadingHistory newHistory = new UserReadingHistory();
                newHistory.setUser(user);
                newHistory.setArticle(article);
                return newHistory;
            });
        
        history.updateProgress(
            request.getProgressPercentage(),
            request.getTimeSpentSeconds(),
            request.getIsCompleted()
        );
        
        readingHistoryRepository.save(history);
    }

    @Override
    public Page<CommentResponse> getArticleComments(Long articleId, Pageable pageable) {
        Page<ArticleComment> comments = commentRepository
            .findByArticleIdAndParentCommentIsNull(articleId, pageable);
        
        return comments.map(this::toCommentResponse);
    }

    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        NewsArticle article = newsArticleRepository.findById(request.getArticleId())
            .orElseThrow(() -> new NotFoundException("Article not found"));
        
        ArticleComment comment = new ArticleComment();
        comment.setArticle(article);
        comment.setUser(user);
        comment.setContent(request.getContent());
        
        if (request.getParentCommentId() != null) {
            ArticleComment parentComment = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new NotFoundException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }
        
        ArticleComment saved = commentRepository.save(comment);
        return toCommentResponse(saved);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String username) {
        ArticleComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only edit your own comments");
        }
        
        comment.setContent(request.getContent());
        ArticleComment updated = commentRepository.save(comment);
        
        return toCommentResponse(updated);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        ArticleComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only delete your own comments");
        }
        
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public Long saveVocabularyToFlashcard(SaveVocabToFlashcardRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        NewsArticle article = newsArticleRepository.findById(request.getArticleId())
            .orElseThrow(() -> new NotFoundException("Article not found"));
        
        // Get or create flashcard set
        SetCard setCard;
        if (request.getSetCardId() != null) {
            setCard = setCardRepository.findById(request.getSetCardId())
                .orElseThrow(() -> new NotFoundException("Flashcard set not found"));
            
            // Verify ownership
            if (setCard.getUser() != null && !setCard.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You don't have permission to add to this set");
            }
        } else {
            // Create default "Words from News" set
            setCard = setCardRepository.findAll().stream()
                .filter(set -> set.getUser() != null && 
                             set.getUser().getId().equals(user.getId()) &&
                             set.getTitle().equals("Từ vựng từ Tin tức"))
                .findFirst()
                .orElseGet(() -> {
                    SetCard newSet = SetCard.builder()
                        .title("Từ vựng từ Tin tức")
                        .description("Các từ vựng đã lưu từ bài báo")
                        .category("Tin tức")
                        .user(user)
                        .build();
                    return setCardRepository.save(newSet);
                });
        }
        
        // Create flashcard
        Card card = Card.builder()
            .term(request.getWord())
            .definition(request.getMeaning())
            .example(request.getExample())
            .setCard(setCard)
            .build();
        
        Card savedCard = cardRepository.save(card);
        
        // Create mapping
        NewsFlashcardMapping mapping = NewsFlashcardMapping.builder()
            .article(article)
            .newsVocabulary(request.getNewsVocabId() != null ? 
                newsVocabularyRepository.findById(request.getNewsVocabId()).orElse(null) : null)
            .card(savedCard)
            .user(user)
            .build();
        
        mappingRepository.save(mapping);
        
        log.info("Saved vocabulary '{}' to flashcard for user: {}", request.getWord(), username);
        
        return savedCard.getId();
    }

    // Helper methods
    private void trackInitialRead(NewsArticle article, User user) {
        readingHistoryRepository.findByUserIdAndArticleId(user.getId(), article.getId())
            .orElseGet(() -> {
                UserReadingHistory history = UserReadingHistory.builder()
                    .user(user)
                    .article(article)
                    .lastReadAt(LocalDateTime.now())
                    .build();
                return readingHistoryRepository.save(history);
            });
    }

    private NewsTopicResponse toTopicResponse(NewsTopic topic) {
        return NewsTopicResponse.builder()
            .id(topic.getId())
            .title(topic.getTitle())
            .description(topic.getDescription())
            .icon(topic.getIcon())
            .articleCount(topic.getArticles() != null ? topic.getArticles().size() : 0)
            .build();
    }

    private NewsArticleResponse toArticleResponse(NewsArticle article, User user) {
        List<VocabularyHighlight> vocabularies = article.getVocabularies() != null ?
            article.getVocabularies().stream()
                .map(vocab -> toVocabularyHighlight(vocab, user))
                .collect(Collectors.toList()) : List.of();
        
        return NewsArticleResponse.builder()
            .id(article.getId())
            .title(article.getTitle())
            .titleVietnamese(article.getTitleVietnamese())
            .content(article.getContent())
            .contentVietnamese(article.getContentVietnamese())
            .htmlContent(article.getHtmlContent())
            .summary(article.getSummary())
            .source(article.getSource())
            .sourceUrl(article.getSourceUrl())
            .difficultyLevel(article.getDifficultyLevel())
            .thumbnailUrl(article.getThumbnailUrl())
            .audioUrl(article.getAudioUrl())
            .readCount(article.getReadCount())
            .publishedAt(article.getPublishedAt())
            .createdAt(article.getCreatedAt())
            .newsTopic(article.getNewsTopic() != null ? toTopicResponse(article.getNewsTopic()) : null)
            .vocabularies(vocabularies)
            .build();
    }

    private VocabularyHighlight toVocabularyHighlight(NewsVocabulary vocab, User user) {
        boolean isInFlashcard = user != null &&
            mappingRepository.existsByUserIdAndNewsVocabularyId(user.getId(), vocab.getId());
        
        return VocabularyHighlight.builder()
            .id(vocab.getId())
            .koreanWord(vocab.getKoreanWord())
            .vietnameseMeaning(vocab.getVietnameseMeaning())
            .romanization(vocab.getRomanization())
            .wordType(vocab.getWordType())
            .topikLevel(vocab.getTopikLevel())
            .positionStart(vocab.getPositionStart())
            .positionEnd(vocab.getPositionEnd())
            .contextSentence(vocab.getContextSentence())
            .isInFlashcard(isInFlashcard)
            .build();
    }

    private CommentResponse toCommentResponse(ArticleComment comment) {
        User user = comment.getUser();
        String username = user.getAccount() != null ? user.getAccount().getUsername() : "Unknown";
        String fullName = (user.getFirstName() != null && user.getLastName() != null) 
            ? user.getFirstName() + " " + user.getLastName() 
            : username;
        
        return CommentResponse.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .articleId(comment.getArticle().getId())
            .parentCommentId(comment.getParentComment() != null ? 
                comment.getParentComment().getId() : null)
            .user(CommentResponse.UserInfo.builder()
                .id(user.getId())
                .username(username)
                .fullName(fullName)
                .build())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getLastModified())
            .build();
    }
}
