package korastudy.be.controller;

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
        ExamResultResponse result = examService.submitExam(id, request, userId);
        return ResponseEntity.ok(result);
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
     * Lấy lịch sử làm bài của user
     */
    @GetMapping("/history")
    public ResponseEntity<List<ExamResultResponse>> getExamHistory(@RequestParam Long userId) {
        List<ExamResultResponse> history = examService.getExamHistory(userId);
        return ResponseEntity.ok(history);
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
     * Lấy kết quả chi tiết một lần thi cụ thể
     */
    @GetMapping("/result/{resultId}")
    public ResponseEntity<ExamResultResponse> getExamResultDetail(@PathVariable Long resultId) {
        ExamResultResponse result = examService.getExamResultDetail(resultId);
        return ResponseEntity.ok(result);
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