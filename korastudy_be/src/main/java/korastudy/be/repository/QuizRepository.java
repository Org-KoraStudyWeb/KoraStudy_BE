package korastudy.be.repository;

import korastudy.be.entity.Course.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // ==================== QUAN HỆ VỚI SECTION (1-N) ====================

    /**
     * Tìm tất cả quiz theo section ID
     */
    List<Quiz> findBySectionId(Long sectionId);

    /**
     * Tìm quiz theo section ID và quiz ID
     */
    Optional<Quiz> findBySectionIdAndId(Long sectionId, Long quizId);

    /**
     * Kiểm tra section có quiz nào không
     */
    boolean existsBySectionId(Long sectionId);

    /**
     * Đếm số quiz trong section
     */
    long countBySectionId(Long sectionId);

    // ==================== QUERY PHỨC TẠP ====================

    /**
     * Tìm tất cả quiz theo course ID
     */
    @Query("SELECT q FROM Quiz q WHERE q.section.course.id = :courseId")
    List<Quiz> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Tìm quiz theo danh sách section IDs
     */
    @Query("SELECT q FROM Quiz q WHERE q.section.id IN :sectionIds")
    List<Quiz> findBySectionIds(@Param("sectionIds") List<Long> sectionIds);

    /**
     * Tìm quiz chứa question
     */
    @Query("SELECT q FROM Quiz q JOIN q.questions ques WHERE ques.id = :questionId")
    Optional<Quiz> findByQuestionId(@Param("questionId") Long questionId);

    /**
     * Tìm quiz cùng với section và course info
     */
    @Query("SELECT q FROM Quiz q JOIN FETCH q.section s JOIN FETCH s.course WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithSectionAndCourse(@Param("quizId") Long quizId);

    /**
     * Tìm quiz theo section với phân trang
     */
    Page<Quiz> findBySectionId(Long sectionId, Pageable pageable);
}