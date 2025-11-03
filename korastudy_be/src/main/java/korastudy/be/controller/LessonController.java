package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.course.LessonCreateRequest;
import korastudy.be.dto.request.course.LessonUpdateRequest;
import korastudy.be.dto.response.course.LessonDTO;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final ILessonService lessonService;

    @GetMapping("/{id}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable Long id) {
        LessonDTO lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(lesson);
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<LessonDTO>> getLessonsBySectionId(@PathVariable Long sectionId) {
        List<LessonDTO> lessons = lessonService.getLessonsBySectionId(sectionId);
        return ResponseEntity.ok(lessons);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<LessonDTO> createLesson(@Valid @RequestBody LessonCreateRequest request) {
        LessonDTO lessonDTO = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<LessonDTO> updateLesson(@PathVariable Long id, @Valid @RequestBody LessonUpdateRequest request) {
        LessonDTO lessonDTO = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(lessonDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiSuccess> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.ok(ApiSuccess.of("Xóa bài học thành công"));
    }
}
