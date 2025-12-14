package korastudy.be.repository;

import korastudy.be.entity.Course.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Method cơ bản - chỉ lấy questions không có options
    List<Question> findByQuizIdOrderById(Long quizId);

    // Method tối ưu - lấy questions CÙNG options (tránh N+1 query)
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId ORDER BY q.id")
    List<Question> findWithOptionsByQuizId(@Param("quizId") Long quizId);

    // Method đếm - đã tốt
    @Query("SELECT COUNT(q) FROM Question q WHERE q.quiz.id = :quizId")
    long countByQuizId(@Param("quizId") Long quizId);

    // Method xóa - đã tốt
    void deleteByQuizId(Long quizId);

    // THÊM: Tìm question cùng options theo ID (cho chấm bài)
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id = :questionId")
    Optional<Question> findByIdWithOptions(@Param("questionId") Long questionId);

    // THÊM: Tìm questions với correct options (cho giáo viên)
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options o WHERE q.quiz.id = :quizId AND o.isCorrect = true ORDER BY q.id")
    List<Question> findWithCorrectOptionsByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT MAX(q.orderIndex) FROM Question q WHERE q.quiz.id = :quizId")
    Integer findMaxOrderIndexByQuizId(@Param("quizId") Long quizId);

    List<Question> findByQuizIdOrderByOrderIndex(Long quizId);

    List<Question> findByQuizId(Long id);
}