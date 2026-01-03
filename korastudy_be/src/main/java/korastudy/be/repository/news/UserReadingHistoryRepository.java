package korastudy.be.repository.news;

import korastudy.be.entity.news.UserReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReadingHistoryRepository extends JpaRepository<UserReadingHistory, Long> {
    
    Optional<UserReadingHistory> findByUserIdAndArticleId(Long userId, Long articleId);
    
    Page<UserReadingHistory> findByUserId(Long userId, Pageable pageable);
    
    Long countByArticleId(Long articleId);
}
