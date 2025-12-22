package korastudy.be.repository;

import korastudy.be.entity.Course.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {

    List<TestResult> findByUserIdOrderByTakenDateDesc(Long userId);

    List<TestResult> findByQuizIdOrderByScoreDesc(Long quizId);

    boolean existsByUserIdAndQuizId(Long userId, Long quizId);


    List<TestResult> findByUserIdAndQuizId(Long userId, Long quizId);

    @Query("SELECT tr FROM TestResult tr " + "WHERE tr.user.id = :userId AND tr.quiz.id = :quizId " + "ORDER BY tr.score DESC")
    List<TestResult> findByUserIdAndQuizIdOrderByScoreDesc(@Param("userId") Long userId, @Param("quizId") Long quizId);

    @Query("SELECT tr FROM TestResult tr " + "WHERE tr.user.id = :userId AND tr.quiz.id = :quizId " + "ORDER BY tr.takenDate DESC")
    List<TestResult> findByUserIdAndQuizIdOrderByTakenDateDesc(@Param("userId") Long userId, @Param("quizId") Long quizId);

    default Optional<TestResult> findFirstByUserIdAndQuizIdOrderByScoreDesc(Long userId, Long quizId) {
        List<TestResult> results = findByUserIdAndQuizIdOrderByScoreDesc(userId, quizId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    default Optional<TestResult> findFirstByUserIdAndQuizIdOrderByTakenDateDesc(Long userId, Long quizId) {
        List<TestResult> results = findByUserIdAndQuizIdOrderByTakenDateDesc(userId, quizId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Query("SELECT DISTINCT tr.user.id FROM TestResult tr " + "WHERE tr.quiz.section.course.id = :courseId")
    List<Long> findDistinctUserIdsByCourseId(@Param("courseId") Long courseId);
}