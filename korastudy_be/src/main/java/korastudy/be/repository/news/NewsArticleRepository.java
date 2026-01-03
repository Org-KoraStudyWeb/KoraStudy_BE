package korastudy.be.repository.news;

import korastudy.be.entity.news.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    
    // Check duplicate by source URL
    boolean existsBySourceUrl(String sourceUrl);
    
    // Find by topic
    Page<NewsArticle> findByNewsTopicId(Long topicId, Pageable pageable);
    
    // Find by difficulty level
    Page<NewsArticle> findByDifficultyLevel(String level, Pageable pageable);
    
    // Search by keyword
    @Query("SELECT a FROM NewsArticle a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<NewsArticle> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Find with filters
    @Query("SELECT a FROM NewsArticle a WHERE " +
           "(:topicId IS NULL OR a.newsTopic.id = :topicId) AND " +
           "(:level IS NULL OR a.difficultyLevel = :level) AND " +
           "(:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<NewsArticle> findWithFilters(
        @Param("topicId") Long topicId,
        @Param("level") String level,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
