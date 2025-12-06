package korastudy.be.service;

import korastudy.be.dto.request.course.LessonCreateRequest;
import korastudy.be.dto.request.course.LessonUpdateRequest;
import korastudy.be.dto.request.course.LessonProgressRequest;
import korastudy.be.dto.response.course.LessonDTO;
import korastudy.be.dto.response.course.LessonProgressDTO;
import korastudy.be.entity.Course.Lesson;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ILessonService {

    // Lesson CRUD
    LessonDTO createLesson(LessonCreateRequest request);

    LessonDTO updateLesson(Long id, LessonUpdateRequest request);

    LessonDTO getLessonById(Long id);

    void deleteLesson(Long id);

    List<LessonDTO> getLessonsBySectionId(Long sectionId);

    List<LessonDTO> getLessonsByCourseId(Long courseId);

    // Upload files
    String uploadVideo(MultipartFile file, String title);

    String uploadDocument(MultipartFile file, String title);

    void deleteFile(String fileUrl);

    // Progress tracking
    LessonProgressDTO updateLessonProgress(LessonProgressRequest request, String username);

    LessonProgressDTO getLessonProgress(Long lessonId, String username);

    // Cho user thường - lấy progress của chính họ
    List<LessonProgressDTO> getUserProgressByCourse(Long courseId, String username);

    // Cho admin - lấy progress của user cụ thể
    List<LessonProgressDTO> getUserProgressByCourseForAdmin(Long courseId, Long userId);

    // Cho admin - lấy progress của tất cả users trong khóa học
    List<LessonProgressDTO> getAllUsersProgressByCourse(Long courseId);

    // Utils
    LessonDTO mapToDTO(Lesson lesson);
}