package korastudy.be.controller;

import korastudy.be.dto.request.course.LessonCreateRequest;
import korastudy.be.dto.request.course.LessonUpdateRequest;
import korastudy.be.dto.request.course.LessonProgressRequest;
import korastudy.be.dto.response.course.LessonDTO;
import korastudy.be.dto.response.course.LessonProgressDTO;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final ILessonService lessonService;

    // ==================== ADMIN & CONTENT_MANAGER ====================

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
    public ResponseEntity<ApiSuccess> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.ok(ApiSuccess.of("Xóa bài học thành công"));
    }

    // ==================== PUBLIC & AUTHENTICATED ====================

    @GetMapping("/{id}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable Long id) {
        LessonDTO lessonDTO = lessonService.getLessonById(id);
        return ResponseEntity.ok(lessonDTO);
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<LessonDTO>> getLessonsBySectionId(@PathVariable Long sectionId) {
        List<LessonDTO> lessons = lessonService.getLessonsBySectionId(sectionId);
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonDTO>> getLessonsByCourseId(@PathVariable Long courseId) {
        List<LessonDTO> lessons = lessonService.getLessonsByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }

    // ==================== UPLOAD FILES ====================

    @PostMapping("/upload/video")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file, @RequestParam(value = "title", required = false) String title) {
        String videoUrl = lessonService.uploadVideo(file, title);
        return ResponseEntity.ok(videoUrl);
    }

    @PostMapping("/upload/document")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam(value = "title", required = false) String title) {
        String documentUrl = lessonService.uploadDocument(file, title);
        return ResponseEntity.ok(documentUrl);
    }

    @DeleteMapping("/files")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteFile(@RequestParam String fileUrl) {
        lessonService.deleteFile(fileUrl);
        return ResponseEntity.noContent().build();
    }

    // ==================== PROGRESS TRACKING ====================

    @PostMapping("/progress")
    public ResponseEntity<LessonProgressDTO> updateLessonProgress(@Valid @RequestBody LessonProgressRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        LessonProgressDTO progress = lessonService.updateLessonProgress(request, username);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{lessonId}/progress")
    public ResponseEntity<LessonProgressDTO> getLessonProgress(@PathVariable Long lessonId, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        LessonProgressDTO progress = lessonService.getLessonProgress(lessonId, username);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/course/{courseId}/progress")
    public ResponseEntity<List<LessonProgressDTO>> getUserProgressByCourse(@PathVariable Long courseId, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        List<LessonProgressDTO> progresses = lessonService.getUserProgressByCourse(courseId, username);
        return ResponseEntity.ok(progresses);
    }
}