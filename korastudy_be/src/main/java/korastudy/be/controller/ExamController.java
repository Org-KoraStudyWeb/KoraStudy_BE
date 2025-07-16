package korastudy.be.controller;

import korastudy.be.dto.request.Exam.SubmitAnswerRequest;
import korastudy.be.dto.request.Exam.SubmitExamRequest;
import korastudy.be.dto.response.Exam.ExamCommentResponse;
import korastudy.be.dto.response.Exam.ExamDetailResponse;
import korastudy.be.dto.response.Exam.ExamListItemResponse;
import korastudy.be.dto.response.Exam.ExamResultResponse;
import korastudy.be.service.impl.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;

    /**
     * Lấy danh sách tất cả bài thi
     */
    @GetMapping
    public ResponseEntity<List<ExamListItemResponse>> getAllExams() {
        List<ExamListItemResponse> exams = examService.getAllExams();
        return ResponseEntity.ok(exams);
    }

    /**
     * Lấy chi tiết bài thi theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExamDetailResponse> getExamDetail(@PathVariable Long id) {
        ExamDetailResponse exam = examService.getExamDetail(id);
        return ResponseEntity.ok(exam);
    }

    /**
     * Nộp bài thi và nhận kết quả
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ExamResultResponse> submitExam(
            @PathVariable Long id,
            @RequestBody SubmitExamRequest request,
            @RequestParam Long userId
    ) {
        try {
            System.out.println("=== CONTROLLER RECEIVED ===");
            System.out.println("Raw Path Variable 'id': " + id);
            System.out.println("Raw Query Param 'userId': " + userId);
            System.out.println("Request Body: " + request);
            
            // Log request details
            System.out.println("Request Method: POST");
            System.out.println("Request URL: /exams/" + id + "/submit?userId=" + userId);
            
            if (request != null && request.getAnswers() != null) {
                System.out.println("Answers received: " + request.getAnswers().size());
                for (int i = 0; i < Math.min(3, request.getAnswers().size()); i++) {
                    SubmitAnswerRequest answer = request.getAnswers().get(i);
                    System.out.println("Sample answer " + i + ": Q" + answer.getQuestionId() + " = " + answer.getSelectedAnswer());
                }
            }
            
            // Validate parameters
            if (id == null || id <= 0) {
                System.err.println("❌ Invalid exam ID: " + id);
                return ResponseEntity.badRequest().build();
            }
            
            if (userId == null || userId <= 0) {
                System.err.println("❌ Invalid user ID: " + userId);
                return ResponseEntity.badRequest().build();
            }
            
            if (request == null || request.getAnswers() == null || request.getAnswers().isEmpty()) {
                System.err.println("❌ Invalid request body");
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("✅ All parameters validated, calling service...");
            ExamResultResponse result = examService.submitExam(id, request, userId);
            
            System.out.println("✅ Service call completed successfully");
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error in controller: " + e.getMessage());
            e.printStackTrace();
            
            if (e.getMessage().contains("không tìm thấy người dùng") || 
                e.getMessage().contains("User ID")) {
                return ResponseEntity.status(404)
                    .body(null); // Or create an error response DTO
            } else if (e.getMessage().contains("không tìm thấy bài thi")) {
                return ResponseEntity.status(404)
                    .body(null);
            } else {
                return ResponseEntity.status(500)
                    .body(null);
            }
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Nộp bài practice test (một hoặc nhiều phần)
     */
    @PostMapping("/{id}/submit-practice")
    public ResponseEntity<ExamResultResponse> submitPracticeTest(
            @PathVariable Long id,
            @RequestParam List<Long> partIds,
            @RequestBody SubmitExamRequest request,
            @RequestParam Long userId
    ) {
        ExamResultResponse result = examService.submitPracticeTest(id, partIds, request, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy kết quả chi tiết một lần thi cụ thể
     */
    @GetMapping("/result/{resultId}")
    public ResponseEntity<ExamResultResponse> getExamResultDetail(@PathVariable Long resultId) {
        try {
            System.out.println("Getting exam result detail for resultId: " + resultId);
            ExamResultResponse result = examService.getExamResultDetail(resultId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error getting exam result detail: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(404).build();
        }
    }

    /**
     * Lấy lịch sử làm bài của user
     */
    @GetMapping("/history")
    public ResponseEntity<List<ExamResultResponse>> getExamHistory(@RequestParam Long userId) {
        try {
            System.out.println("Getting exam history for userId: " + userId);
            List<ExamResultResponse> history = examService.getExamHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            System.err.println("Error getting exam history: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Tìm kiếm bài thi theo tiêu chí
     */
    @GetMapping("/search")
    public ResponseEntity<List<ExamListItemResponse>> searchExams(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<ExamListItemResponse> exams = examService.searchExams(title, level, type, page, size);
        return ResponseEntity.ok(exams);
    }

    /**
     * Lấy danh sách bài thi theo level
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<List<ExamListItemResponse>> getExamsByLevel(@PathVariable String level) {
        List<ExamListItemResponse> exams = examService.getExamsByLevel(level);
        return ResponseEntity.ok(exams);
    }

    /**
     * Lấy thống kê kết quả thi của user
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getExamStatistics(@RequestParam Long userId) {
        Map<String, Object> statistics = examService.getExamStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Lấy comments của bài thi
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<ExamCommentResponse>> getExamComments(@PathVariable Long id) {
        List<ExamCommentResponse> comments = examService.getExamComments(id);
        return ResponseEntity.ok(comments);
    }

    /**
     * Thêm comment cho bài thi
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<ExamCommentResponse> addExamComment(
            @PathVariable Long id,
            @RequestParam String context,
            @RequestParam Long userId
    ) {
        ExamCommentResponse comment = examService.addExamComment(id, context, userId);
        return ResponseEntity.ok(comment);
    }
}