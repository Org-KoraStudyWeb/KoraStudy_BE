package korastudy.be.service;

import korastudy.be.dto.request.course.LessonCreateRequest;
import korastudy.be.dto.request.course.LessonUpdateRequest;
import korastudy.be.dto.response.course.LessonDTO;
import korastudy.be.entity.Course.Lesson;

import java.util.List;

public interface ILessonService {
    
    LessonDTO createLesson(LessonCreateRequest request);
    
    LessonDTO updateLesson(Long lessonId, LessonUpdateRequest request);
    
    LessonDTO getLessonById(Long lessonId);
    
    List<LessonDTO> getLessonsBySectionId(Long sectionId);
    
    void deleteLesson(Long lessonId);
    
    LessonDTO mapToDTO(Lesson lesson);
}
