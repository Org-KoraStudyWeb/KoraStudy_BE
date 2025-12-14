package korastudy.be.repository;

import korastudy.be.entity.Course.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {

    List<Option> findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);

    List<Option> findByQuestionQuizId(Long quizId);

    Integer findMaxOrderIndexByQuestionId(Long questionId);

    long countByQuestionId(Long id);
}