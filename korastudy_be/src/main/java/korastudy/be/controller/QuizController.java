package korastudy.be.controller;

import korastudy.be.dto.request.quiz.*;
import korastudy.be.dto.response.quiz.*;
import korastudy.be.service.IQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final IQuizService quizService;

    // ==================== QUIZ MANAGEMENT (ADMIN & CONTENT_MANAGER) ====================

    @PostMapping
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizDTO> createQuiz(@Valid @RequestBody QuizCreateRequest request) {
        // Tạo quiz mới cho section
        QuizDTO quizDTO = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizDTO);
    }

    @PutMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable Long quizId, @Valid @RequestBody QuizUpdateRequest request) {
        // Cập nhật thông tin quiz
        QuizDTO quizDTO = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(quizDTO);
    }

    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        // Xóa quiz và các questions liên quan
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long quizId) {
        // Lấy chi tiết quiz (có đáp án) - cho giáo viên
        QuizDTO quizDTO = quizService.getQuizById(quizId);
        return ResponseEntity.ok(quizDTO);
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<QuizDTO>> getQuizzesBySectionId(@PathVariable Long sectionId) {
        // Lấy danh sách quiz theo section
        List<QuizDTO> quizzes = quizService.getQuizzesBySectionId(sectionId);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/section/{sectionId}/take")
    public ResponseEntity<List<QuizDTO>> getQuizzesForTakingBySectionId(@PathVariable Long sectionId) {
        // Lấy danh sách quiz để làm bài (không có đáp án)
        List<QuizDTO> quizzes = quizService.getQuizzesForTakingBySectionId(sectionId);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/section/{sectionId}/default")
    public ResponseEntity<QuizDTO> getDefaultQuizForTaking(@PathVariable Long sectionId) {
        // Lấy quiz mặc định của section để làm bài
        QuizDTO quizDTO = quizService.getDefaultQuizForTakingBySectionId(sectionId);
        return ResponseEntity.ok(quizDTO);
    }

    // ==================== QUESTION MANAGEMENT ====================

    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuestionDTO> addQuestionToQuiz(@PathVariable Long quizId, @Valid @RequestBody QuestionCreateRequest request) {
        // Thêm câu hỏi mới vào quiz
        QuestionDTO questionDTO = quizService.addQuestionToQuiz(quizId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(questionDTO);
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuestionDTO> updateQuestion(@PathVariable Long questionId, @Valid @RequestBody QuestionUpdateRequest request) {
        // Cập nhật câu hỏi và options
        QuestionDTO questionDTO = quizService.updateQuestion(questionId, request);
        return ResponseEntity.ok(questionDTO);
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        // Xóa câu hỏi khỏi quiz
        quizService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{quizId}/questions")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByQuizId(@PathVariable Long quizId) {
        // Lấy danh sách câu hỏi của quiz (có đáp án) - cho giáo viên
        List<QuestionDTO> questions = quizService.getQuestionsByQuizId(quizId);
        return ResponseEntity.ok(questions);
    }

    // ==================== QUIZ TAKING (ALL USERS) ====================

    @GetMapping("/{quizId}/take")
    public ResponseEntity<QuizDTO> getQuizForTaking(@PathVariable Long quizId) {
        // Lấy quiz để làm bài (không có đáp án)
        QuizDTO quizDTO = quizService.getQuizForTaking(quizId);
        return ResponseEntity.ok(quizDTO);
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<TestResultDTO> submitQuiz(@PathVariable Long quizId, @Valid @RequestBody QuizSubmissionRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        // Nộp bài quiz và chấm điểm
        String username = userDetails.getUsername();
        TestResultDTO result = quizService.submitQuiz(quizId, request, username);
        return ResponseEntity.ok(result);
    }

    // ==================== RESULTS & ANALYTICS ====================

    @GetMapping("/results/{resultId}")
    public ResponseEntity<TestResultDTO> getQuizResult(@PathVariable Long resultId) {
        // Lấy kết quả bài test theo ID
        TestResultDTO result = quizService.getQuizResult(resultId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/results/{resultId}/details")
    public ResponseEntity<QuizResultDetailDTO> getQuizResultDetail(@PathVariable Long resultId) {
        // Lấy chi tiết kết quả bài test (câu hỏi, đáp án user chọn, đáp án đúng)
        QuizResultDetailDTO resultDetail = quizService.getQuizResultDetail(resultId);
        return ResponseEntity.ok(resultDetail);
    }

    @GetMapping("/my-results")
    public ResponseEntity<List<TestResultDTO>> getUserQuizResults(@AuthenticationPrincipal UserDetails userDetails) {
        // Lấy lịch sử làm bài của user
        String username = userDetails.getUsername();
        List<TestResultDTO> results = quizService.getUserQuizResults(username);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{quizId}/results")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<TestResultDTO>> getQuizResults(@PathVariable Long quizId) {
        // Lấy kết quả của tất cả user cho một quiz - cho giáo viên
        List<TestResultDTO> results = quizService.getQuizResults(quizId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{quizId}/statistics")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizStatisticsDTO> getQuizStatistics(@PathVariable Long quizId) {
        // Lấy thống kê chi tiết của quiz
        QuizStatisticsDTO statistics = quizService.getQuizStatistics(quizId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/section/{sectionId}/statistics")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<QuizStatisticsDTO>> getQuizStatisticsBySectionId(@PathVariable Long sectionId) {
        // Lấy thống kê của tất cả quiz trong section
        List<QuizStatisticsDTO> statistics = quizService.getQuizStatisticsBySectionId(sectionId);
        return ResponseEntity.ok(statistics);
    }

    // ==================== VALIDATION ====================

    @GetMapping("/section/{sectionId}/exists")
    public ResponseEntity<Boolean> checkQuizExistsBySectionId(@PathVariable Long sectionId) {
        // Kiểm tra section đã có quiz chưa
        boolean exists = quizService.existsAnyQuizBySectionId(sectionId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/section/{sectionId}/count")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Long> countQuizzesBySectionId(@PathVariable Long sectionId) {
        // Đếm số quiz trong section
        long count = quizService.countQuizzesBySectionId(sectionId);
        return ResponseEntity.ok(count);
    }
}