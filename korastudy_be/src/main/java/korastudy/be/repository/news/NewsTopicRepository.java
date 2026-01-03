package korastudy.be.repository.news;

import korastudy.be.entity.news.NewsTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsTopicRepository extends JpaRepository<NewsTopic, Long> {
    Optional<NewsTopic> findByTitle(String title);
}
