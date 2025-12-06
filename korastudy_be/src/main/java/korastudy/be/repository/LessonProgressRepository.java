package korastudy.be.repository;

import korastudy.be.entity.Course.LessonProgress;
import korastudy.be.entity.Enum.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    // Tìm progress cụ thể của user cho 1 lesson
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    // Tìm tất cả progress của user trong 1 khóa học
    List<LessonProgress> findByUserIdAndLessonSectionCourseId(Long userId, Long courseId);

    // Lịch sử học tập của user (mới nhất trước)
    List<LessonProgress> findByUserIdOrderByCompletedDateDesc(Long userId);

    // Đếm số lesson đã hoàn thành trong khóa học
    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.user.id = :userId AND lp.status = 'COMPLETED' AND lp.lesson.section.course.id = :courseId")
    long countCompletedLessonsByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    // Tìm progress (tương tự method đầu nhưng dùng @Query)
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.user.id = :userId AND lp.lesson.id = :lessonId")
    Optional<LessonProgress> findProgress(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    // Kiểm tra trạng thái cụ thể
    boolean existsByUserIdAndLessonIdAndStatus(Long userId, Long lessonId, ProgressStatus status);

    @Query("SELECT lp FROM LessonProgress lp JOIN lp.lesson l WHERE l.section.course.id = :courseId")
    List<LessonProgress> findByCourseId(@Param("courseId") Long courseId);

    // Cho getUserProgressByCourseForAdmin - lấy progress của user cụ thể
    @Query("SELECT lp FROM LessonProgress lp JOIN lp.lesson l WHERE l.section.course.id = :courseId AND lp.user.id = :userId")
    List<LessonProgress> findByCourseIdAndUserId(@Param("courseId") Long courseId, @Param("userId") Long userId);

    // Cho logic mapping - tìm progress theo lessonId và userId
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.lesson.id = :lessonId AND lp.user.id = :userId")
    Optional<LessonProgress> findByLessonIdAndUserId(@Param("lessonId") Long lessonId, @Param("userId") Long userId);

}