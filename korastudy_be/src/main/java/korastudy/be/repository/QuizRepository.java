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
     * 🆕 Tìm quiz đã publish và active theo section ID (cho student)
     */
    @Query("SELECT q FROM Quiz q WHERE q.section.id = :sectionId " + "AND q.isPublished = true AND q.isActive = true")
    List<Quiz> findPublishedAndActiveBySectionId(@Param("sectionId") Long sectionId);

    /**
     * 🆕 Tìm tất cả quiz theo section ID với JOIN FETCH để tránh N+1
     */
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.section WHERE q.section.id = :sectionId")
    List<Quiz> findBySectionIdWithSection(@Param("sectionId") Long sectionId);

    /**
     * Tìm quiz theo section ID và quiz ID
     */
    Optional<Quiz> findBySectionIdAndId(Long sectionId, Long quizId);

    /**
     * Kiểm tra section có quiz nào không
     */
    boolean existsBySectionId(Long sectionId);

    /**
     * 🆕 Kiểm tra section có quiz published không
     */
    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END FROM Quiz q " + "WHERE q.section.id = :sectionId AND q.isPublished = true AND q.isActive = true")
    boolean existsPublishedQuizBySectionId(@Param("sectionId") Long sectionId);

    /**
     * Đếm số quiz trong section
     */
    long countBySectionId(Long sectionId);

    /**
     * 🆕 Đếm số quiz published trong section
     */
    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.section.id = :sectionId " + "AND q.isPublished = true AND q.isActive = true")
    long countPublishedQuizzesBySectionId(@Param("sectionId") Long sectionId);

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