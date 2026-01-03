package korastudy.be.repository.news;

import korastudy.be.entity.news.NewsFlashcardMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsFlashcardMappingRepository extends JpaRepository<NewsFlashcardMapping, Long> {
    
    boolean existsByUserIdAndNewsVocabularyId(Long userId, Long newsVocabId);
    
    Optional<NewsFlashcardMapping> findByUserIdAndNewsVocabularyId(Long userId, Long newsVocabId);
    
    List<NewsFlashcardMapping> findByUserId(Long userId);
}
