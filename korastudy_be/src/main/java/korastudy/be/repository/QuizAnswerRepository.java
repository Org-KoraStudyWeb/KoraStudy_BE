package korastudy.be.repository;

import korastudy.be.entity.Course.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    List<QuizAnswer> findByTestResultId(Long testResultId);

    List<QuizAnswer> findByQuestionId(Long questionId);

    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.testResult.quiz.id = :quizId")
    List<QuizAnswer> findByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.testResult.user.id = :userId AND qa.question.id = :questionId")
    List<QuizAnswer> findByUserIdAndQuestionId(@Param("userId") Long userId, @Param("questionId") Long questionId);

    void deleteByTestResultId(Long testResultId);

    @Query("SELECT COUNT(qa) FROM QuizAnswer qa WHERE qa.question.id = :questionId AND qa.isCorrect = true")
    long countCorrectAnswersByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.testResult.id = :testResultId ORDER BY qa.question.id")
    List<QuizAnswer> findByTestResultIdOrderByQuestion(@Param("testResultId") Long testResultId);
}