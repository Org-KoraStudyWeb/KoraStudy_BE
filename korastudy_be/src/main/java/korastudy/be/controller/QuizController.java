package korastudy.be.controller;

import korastudy.be.dto.request.quiz.*;
import korastudy.be.dto.response.quiz.*;
import korastudy.be.entity.User.Account;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.AccountRepository;
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
    private final AccountRepository accountRepository;

    // ==================== PHƯƠNG THỨC HỖ TRỢ ====================

    private Long getUserIdFromPrincipal(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User chưa đăng nhập");
        }

        String username = userDetails.getUsername();
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với username: " + username));

        return account.getUser().getId();
    }

    // ==================== ADMIN APIs ====================
    // (Chỉ ADMIN, CONTENT_MANAGER)

    // ==================== QUIZ QUẢN LÝ ====================

    @PostMapping
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizDTO> createQuiz(@Valid @RequestBody QuizCreateRequest request) {
        QuizDTO quizDTO = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizDTO);
    }

    @PutMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable Long quizId, @Valid @RequestBody QuizUpdateRequest request) {
        QuizDTO quizDTO = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(quizDTO);
    }

    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{quizId}/publish/{publish}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> publishQuiz(@PathVariable Long quizId, @PathVariable Boolean publish) {
        quizService.publishQuiz(quizId, publish);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{quizId}/admin")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizDTO> getQuizForAdmin(@PathVariable Long quizId) {
        QuizDTO quizDTO = quizService.getQuizForTeacher(quizId);
        return ResponseEntity.ok(quizDTO);
    }

    // ==================== QUẢN LÝ CÂU HỎI (ADMIN) ====================

    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuestionDTO> addQuestionToQuiz(@PathVariable Long quizId, @Valid @RequestBody QuestionCreateRequest request) {
        QuestionDTO questionDTO = quizService.addQuestionToQuiz(quizId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(questionDTO);
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuestionDTO> updateQuestion(@PathVariable Long questionId, @Valid @RequestBody QuestionUpdateRequest request) {
        QuestionDTO questionDTO = quizService.updateQuestion(questionId, request);
        return ResponseEntity.ok(questionDTO);
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        quizService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{quizId}/questions/admin")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsForAdmin(@PathVariable Long quizId) {
        List<QuestionDTO> questions = quizService.getQuestionsForTeacher(quizId);
        return ResponseEntity.ok(questions);
    }

    // ==================== QUẢN LÝ ĐÁP ÁN (ADMIN) ====================

    @PostMapping("/questions/{questionId}/options")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<OptionDTO> addOptionToQuestion(@PathVariable Long questionId, @Valid @RequestBody OptionCreateRequest request) {
        OptionDTO optionDTO = quizService.addOptionToQuestion(questionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(optionDTO);
    }

    @PutMapping("/options/{optionId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<OptionDTO> updateOption(@PathVariable Long optionId, @Valid @RequestBody OptionUpdateRequest request) {
        OptionDTO optionDTO = quizService.updateOption(optionId, request);
        return ResponseEntity.ok(optionDTO);
    }

    @DeleteMapping("/options/{optionId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteOption(@PathVariable Long optionId) {
        quizService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/questions/{questionId}/options")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<OptionDTO>> getOptionsByQuestionId(@PathVariable Long questionId) {
        List<OptionDTO> options = quizService.getOptionsByQuestionId(questionId);
        return ResponseEntity.ok(options);
    }

    // ==================== KẾT QUẢ & THỐNG KÊ (ADMIN) ====================

    @GetMapping("/results/{resultId}/admin")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizResultDetailDTO> getQuizResultForAdmin(@PathVariable Long resultId) {
        QuizResultDetailDTO result = quizService.getQuizResultForTeacher(resultId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{quizId}/user/{userId}/status")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizStatusDTO> getQuizStatusForUser(@PathVariable Long quizId, @PathVariable Long userId) {
        QuizStatusDTO status = quizService.getQuizStatusForStudent(quizId, userId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{quizId}/results")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<TestResultDTO>> getQuizResults(@PathVariable Long quizId) {
        List<TestResultDTO> results = quizService.getQuizResults(quizId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{quizId}/statistics")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizStatisticsDTO> getQuizStatistics(@PathVariable Long quizId) {
        QuizStatisticsDTO statistics = quizService.getQuizStatistics(quizId);
        return ResponseEntity.ok(statistics);
    }

    // ==================== TIẾN ĐỘ QUIZ THEO COURSE (ADMIN) ====================

    @GetMapping("/admin/course/{courseId}/user/{userId}/progress")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<UserQuizProgressInCourseDTO>> getUserQuizProgressInCourseForAdmin(
            @PathVariable Long courseId,
            @PathVariable Long userId) {
        List<UserQuizProgressInCourseDTO> progress = quizService.getUserQuizProgressInCourse(userId, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/admin/course/{courseId}/user/{userId}/progress/summary")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<UserQuizProgressSummaryDTO> getUserQuizProgressSummaryForAdmin(
            @PathVariable Long courseId,
            @PathVariable Long userId) {
        UserQuizProgressSummaryDTO summary = quizService.getUserQuizProgressSummary(userId, courseId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/admin/course/{courseId}/all-users-progress")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<UserQuizProgressSummaryDTO>> getAllUsersQuizProgressInCourse(
            @PathVariable Long courseId) {
        List<UserQuizProgressSummaryDTO> allProgress = quizService.getAllUsersQuizProgressInCourse(courseId);
        return ResponseEntity.ok(allProgress);
    }

    // ==================== USER APIs ====================
    // (Tất cả authenticated users)

    // ==================== XEM QUIZ ====================

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<QuizSummaryDTO>> getQuizzesBySectionId(@PathVariable Long sectionId) {
        List<QuizSummaryDTO> quizzes = quizService.getQuizzesBySectionId(sectionId);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/section/{sectionId}/take")
    public ResponseEntity<List<QuizSummaryDTO>> getQuizzesForTakingBySectionId(
            @PathVariable Long sectionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        List<QuizSummaryDTO> quizzes = quizService.getAvailableQuizzesForStudent(sectionId, userId);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDTO> getQuizForTaking(@PathVariable Long quizId) {
        QuizDTO quizDTO = quizService.getQuizForStudent(quizId);
        return ResponseEntity.ok(quizDTO);
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<QuestionDTO>> getQuestionsForStudent(@PathVariable Long quizId) {
        List<QuestionDTO> questions = quizService.getQuestionsForStudent(quizId);
        return ResponseEntity.ok(questions);
    }

    // ==================== LÀM BÀI THI ====================

    @PostMapping("/{quizId}/start")
    public ResponseEntity<TestResultDTO> startQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        TestResultDTO result = quizService.startQuiz(quizId, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<TestResultDTO> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        TestResultDTO result = quizService.submitQuiz(quizId, request, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{quizId}/status")
    public ResponseEntity<QuizStatusDTO> getQuizStatus(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        QuizStatusDTO status = quizService.getQuizStatusForStudent(quizId, userId);
        return ResponseEntity.ok(status);
    }

    // ==================== XEM KẾT QUẢ ====================

    @GetMapping("/results/{resultId}")
    public ResponseEntity<TestResultDTO> getQuizResult(@PathVariable Long resultId) {
        TestResultDTO result = quizService.getQuizResult(resultId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/results/{resultId}/student")
    public ResponseEntity<QuizResultDetailDTO> getQuizResultForStudent(
            @PathVariable Long resultId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        QuizResultDetailDTO result = quizService.getQuizResultForStudent(resultId, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-results")
    public ResponseEntity<List<TestResultDTO>> getUserQuizResults(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        List<TestResultDTO> results = quizService.getUserQuizHistory(userId);
        return ResponseEntity.ok(results);
    }

    // ==================== TIẾN ĐỘ QUIZ THEO COURSE (USER) ====================

    @GetMapping("/course/{courseId}/user-progress")
    public ResponseEntity<List<UserQuizProgressInCourseDTO>> getUserQuizProgressInCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        List<UserQuizProgressInCourseDTO> progress = quizService.getUserQuizProgressInCourse(userId, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/course/{courseId}/user-progress/summary")
    public ResponseEntity<UserQuizProgressSummaryDTO> getUserQuizProgressSummary(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        UserQuizProgressSummaryDTO summary = quizService.getUserQuizProgressSummary(userId, courseId);
        return ResponseEntity.ok(summary);
    }

    // ==================== KIỂM TRA ====================

    @GetMapping("/section/{sectionId}/exists")
    public ResponseEntity<Boolean> checkQuizExistsBySectionId(@PathVariable Long sectionId) {
        boolean exists = quizService.existsAnyQuizBySectionId(sectionId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{quizId}/exists")
    public ResponseEntity<Boolean> checkQuizExists(@PathVariable Long quizId) {
        boolean exists = quizService.existsQuiz(quizId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{quizId}/can-access")
    public ResponseEntity<Boolean> canUserAccessQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        boolean canAccess = quizService.canUserAccessQuiz(quizId, userId);
        return ResponseEntity.ok(canAccess);
    }
}