package korastudy.be.repository;

import korastudy.be.entity.MockTest.MockTestAnswers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockTestAnswersRepository extends JpaRepository<MockTestAnswers, Long> {
    List<MockTestAnswers> findByQuestionAnswer_Id(Long questionId);

    List<MockTestAnswers> findByAnswerPartId(Long answerPartId);
}