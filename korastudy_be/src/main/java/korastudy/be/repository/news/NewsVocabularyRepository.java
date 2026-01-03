package korastudy.be.repository.news;

import korastudy.be.entity.news.NewsVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsVocabularyRepository extends JpaRepository<NewsVocabulary, Long> {
    List<NewsVocabulary> findByArticleId(Long articleId);
}
