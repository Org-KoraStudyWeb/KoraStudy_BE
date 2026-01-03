package korastudy.be.repository.news;

import korastudy.be.entity.news.ArticleComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
    
    Page<ArticleComment> findByArticleIdAndParentCommentIsNull(Long articleId, Pageable pageable);
    
    Page<ArticleComment> findByUserId(Long userId, Pageable pageable);
}
