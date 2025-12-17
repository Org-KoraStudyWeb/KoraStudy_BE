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

    List<TestResult> findByUserIdAndQuizIdOrderByTakenDateDesc(Long userId, Long quizId);

    List<TestResult> findByUserIdAndQuizId(Long userId, Long quizId);
}