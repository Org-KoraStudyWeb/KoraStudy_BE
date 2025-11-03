package korastudy.be.service.impl;

import korastudy.be.dto.request.course.LessonCreateRequest;
import korastudy.be.dto.request.course.LessonUpdateRequest;
import korastudy.be.dto.response.course.LessonDTO;
import korastudy.be.entity.Course.Lesson;
import korastudy.be.entity.Course.Section;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.LessonRepository;
import korastudy.be.repository.SectionRepository;
import korastudy.be.service.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService implements ILessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;

    @Override
    public LessonDTO createLesson(LessonCreateRequest request) {
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương học với ID: " + request.getSectionId()));

        Lesson lesson = Lesson.builder()
                .lessonTitle(request.getLessonTitle())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .contentType(request.getContentType())
                .orderIndex(request.getDisplayOrder())
                .section(section)
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);
        return mapToDTO(savedLesson);
    }

    @Override
    public LessonDTO updateLesson(Long lessonId, LessonUpdateRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        lesson.setLessonTitle(request.getLessonTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setContentType(request.getContentType());
        lesson.setOrderIndex(request.getOrderIndex());

        Lesson updatedLesson = lessonRepository.save(lesson);
        return mapToDTO(updatedLesson);
    }

    @Override
    public LessonDTO getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId));
        return mapToDTO(lesson);
    }

    @Override
    public List<LessonDTO> getLessonsBySectionId(Long sectionId) {
        List<Lesson> lessons = lessonRepository.findBySectionIdOrderByOrderIndex(sectionId);
        return lessons.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId);
        }
        lessonRepository.deleteById(lessonId);
    }

    @Override
    public LessonDTO mapToDTO(Lesson lesson) {
        return LessonDTO.builder()
                .id(lesson.getId())
                .lessonTitle(lesson.getLessonTitle())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .contentType(lesson.getContentType())
                .orderIndex(lesson.getOrderIndex())
                .build();
    }
}
