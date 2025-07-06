package korastudy.be.controller;

import korastudy.be.dto.request.Exam.*;
import korastudy.be.dto.response.Exam.*;
import korastudy.be.service.impl.AdminExamService;
import korastudy.be.service.impl.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminExamController {
    
    private final AdminExamService adminExamService;
    private final CloudinaryService cloudinaryService;

    /**
     * Lấy danh sách tất cả bài thi (bao gồm cả inactive)
     */
    @GetMapping
    public ResponseEntity<List<ExamListItemResponse>> getAllExamsForAdmin() {
        List<ExamListItemResponse> exams = adminExamService.getAllExamsForAdmin();
        return ResponseEntity.ok(exams);
    }

    /**
     * Lấy chi tiết bài thi để chỉnh sửa
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminExamDetailResponse> getExamForEdit(@PathVariable Long id) {
        AdminExamDetailResponse exam = adminExamService.getExamForEdit(id);
        return ResponseEntity.ok(exam);
    }

    /**
     * Tạo bài thi mới
     */
    @PostMapping
    public ResponseEntity<AdminExamDetailResponse> createExam(@RequestBody CreateExamRequest request) {
        AdminExamDetailResponse exam = adminExamService.createExam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(exam);
    }

    /**
     * Cập nhật thông tin bài thi
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminExamDetailResponse> updateExam(
            @PathVariable Long id,
            @RequestBody UpdateExamRequest request) {
        AdminExamDetailResponse exam = adminExamService.updateExam(id, request);
        return ResponseEntity.ok(exam);
    }

    /**
     * Xóa bài thi
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        adminExamService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Thêm phần mới vào bài thi
     */
    @PostMapping("/{examId}/parts")
    public ResponseEntity<AdminExamPartResponse> addPart(
            @PathVariable Long examId,
            @RequestBody CreateExamPartRequest request) {
        AdminExamPartResponse part = adminExamService.addPart(examId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(part);
    }

    /**
     * Cập nhật phần thi
     */
    @PutMapping("/parts/{partId}")
    public ResponseEntity<AdminExamPartResponse> updatePart(
            @PathVariable Long partId,
            @RequestBody UpdateExamPartRequest request) {
        AdminExamPartResponse part = adminExamService.updatePart(partId, request);
        return ResponseEntity.ok(part);
    }

    /**
     * Xóa phần thi
     */
    @DeleteMapping("/parts/{partId}")
    public ResponseEntity<Void> deletePart(@PathVariable Long partId) {
        adminExamService.deletePart(partId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Thêm câu hỏi vào phần thi
     */
    @PostMapping("/parts/{partId}/questions")
    public ResponseEntity<AdminExamQuestionResponse> addQuestion(
            @PathVariable Long partId,
            @RequestBody CreateExamQuestionRequest request) {
        AdminExamQuestionResponse question = adminExamService.addQuestion(partId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }

    /**
     * Cập nhật câu hỏi
     */
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<AdminExamQuestionResponse> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody UpdateExamQuestionRequest request) {
        AdminExamQuestionResponse question = adminExamService.updateQuestion(questionId, request);
        return ResponseEntity.ok(question);
    }

    /**
     * Xóa câu hỏi
     */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        adminExamService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload ảnh cho câu hỏi
     */
    @PostMapping("/questions/{questionId}/upload-image")
    public ResponseEntity<Map<String, String>> uploadQuestionImage(
            @PathVariable Long questionId,
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            adminExamService.updateQuestionImage(questionId, imageUrl);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Upload audio cho câu hỏi
     */
    @PostMapping("/questions/{questionId}/upload-audio")
    public ResponseEntity<Map<String, String>> uploadQuestionAudio(
            @PathVariable Long questionId,
            @RequestParam("file") MultipartFile file) {
        try {
            String audioUrl = cloudinaryService.uploadAudio(file);
            adminExamService.updateQuestionAudio(questionId, audioUrl);
            return ResponseEntity.ok(Map.of("audioUrl", audioUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa bài thi
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Map<String, Boolean>> toggleExamActive(@PathVariable Long id) {
        boolean isActive = adminExamService.toggleExamActive(id);
        return ResponseEntity.ok(Map.of("isActive", isActive));
    }

    /**
     * Sao chép bài thi
     */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<AdminExamDetailResponse> duplicateExam(@PathVariable Long id) {
        AdminExamDetailResponse exam = adminExamService.duplicateExam(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(exam);
    }
}
