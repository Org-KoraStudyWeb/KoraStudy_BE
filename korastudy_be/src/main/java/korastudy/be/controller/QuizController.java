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

    // ==================== HELPER METHOD ====================

    private Long getUserIdFromPrincipal(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User chưa đăng nhập");
        }

        String username = userDetails.getUsername();
        Account account = accountRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với username: " + username));

        return account.getUser().getId();
    }

    // ==================== QUIZ MANAGEMENT ====================

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

    @GetMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long quizId) {
        QuizDTO quizDTO = quizService.getQuizForTeacher(quizId);
        return ResponseEntity.ok(quizDTO);
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<QuizSummaryDTO>> getQuizzesBySectionId(@PathVariable Long sectionId) {
        List<QuizSummaryDTO> quizzes = quizService.getQuizzesBySectionId(sectionId);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/section/{sectionId}/take")
    public ResponseEntity<List<QuizSummaryDTO>> getQuizzesForTakingBySectionId(@PathVariable Long sectionId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        List<QuizSummaryDTO> quizzes = quizService.getAvailableQuizzesForStudent(sectionId, userId);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/section/{sectionId}/default")
    public ResponseEntity<QuizDTO> getDefaultQuizForTaking(@PathVariable Long sectionId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        List<QuizSummaryDTO> quizzes = quizService.getAvailableQuizzesForStudent(sectionId, userId);

        if (quizzes.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy quiz nào trong section này");
        }

        QuizDTO quiz = quizService.getQuizForStudent(quizzes.get(0).getId());
        return ResponseEntity.ok(quiz);
    }

    @PutMapping("/{quizId}/publish/{publish}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> publishQuiz(@PathVariable Long quizId, @PathVariable Boolean publish) {
        quizService.publishQuiz(quizId, publish);
        return ResponseEntity.ok().build();
    }

    // ==================== QUESTION MANAGEMENT ====================

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

    @GetMapping("/{quizId}/questions")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByQuizId(@PathVariable Long quizId) {
        List<QuestionDTO> questions = quizService.getQuestionsForTeacher(quizId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{quizId}/questions/student")
    public ResponseEntity<List<QuestionDTO>> getQuestionsForStudent(@PathVariable Long quizId) {
        List<QuestionDTO> questions = quizService.getQuestionsForStudent(quizId);
        return ResponseEntity.ok(questions);
    }

    // ==================== OPTION MANAGEMENT ====================

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

    // ==================== QUIZ TAKING ====================

    @GetMapping("/{quizId}/take")
    public ResponseEntity<QuizDTO> getQuizForTaking(@PathVariable Long quizId) {
        QuizDTO quizDTO = quizService.getQuizForStudent(quizId);
        return ResponseEntity.ok(quizDTO);
    }

    @PostMapping("/{quizId}/start")
    public ResponseEntity<TestResultDTO> startQuiz(@PathVariable Long quizId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        TestResultDTO result = quizService.startQuiz(quizId, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<TestResultDTO> submitQuiz(@PathVariable Long quizId, @Valid @RequestBody QuizSubmissionRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        TestResultDTO result = quizService.submitQuiz(quizId, request, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{quizId}/save-answer")
    public ResponseEntity<Void> saveAnswer(@PathVariable Long quizId, @Valid @RequestBody AnswerRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        quizService.saveAnswer(quizId, request, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{quizId}/status")
    public ResponseEntity<QuizStatusDTO> getQuizStatus(@PathVariable Long quizId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        QuizStatusDTO status = quizService.getQuizStatusForStudent(quizId, userId);
        return ResponseEntity.ok(status);
    }

    // ==================== RESULTS & ANALYTICS ====================

    @GetMapping("/results/{resultId}")
    public ResponseEntity<TestResultDTO> getQuizResult(@PathVariable Long resultId) {
        TestResultDTO result = quizService.getQuizResult(resultId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/results/{resultId}/details")
    public ResponseEntity<QuizResultDetailDTO> getQuizResultDetail(@PathVariable Long resultId) {
        QuizResultDetailDTO resultDetail = quizService.getQuizResultDetail(resultId);
        return ResponseEntity.ok(resultDetail);
    }

    @GetMapping("/my-results")
    public ResponseEntity<List<TestResultDTO>> getUserQuizResults(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        List<TestResultDTO> results = quizService.getUserQuizHistory(userId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{quizId}/results")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<TestResultDTO>> getQuizResults(@PathVariable Long quizId) {
        List<TestResultDTO> results = quizService.getQuizResults(quizId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{quizId}/results/all")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizAllResultsDTO> getAllQuizResults(@PathVariable Long quizId) {
        QuizAllResultsDTO results = quizService.getAllQuizResults(quizId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{quizId}/statistics")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizStatisticsDTO> getQuizStatistics(@PathVariable Long quizId) {
        QuizStatisticsDTO statistics = quizService.getQuizStatistics(quizId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/section/{sectionId}/statistics")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<QuizStatisticsDTO>> getQuizStatisticsBySectionId(@PathVariable Long sectionId) {
        List<QuizStatisticsDTO> statistics = quizService.getQuizStatisticsBySectionId(sectionId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/results/{resultId}/teacher")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<QuizResultDetailDTO> getQuizResultForTeacher(@PathVariable Long resultId) {
        QuizResultDetailDTO result = quizService.getQuizResultForTeacher(resultId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/results/{resultId}/student")
    public ResponseEntity<QuizResultDetailDTO> getQuizResultForStudent(@PathVariable Long resultId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        QuizResultDetailDTO result = quizService.getQuizResultForStudent(resultId, userId);
        return ResponseEntity.ok(result);
    }

    // ==================== VALIDATION ====================

    @GetMapping("/section/{sectionId}/exists")
    public ResponseEntity<Boolean> checkQuizExistsBySectionId(@PathVariable Long sectionId) {
        boolean exists = quizService.existsAnyQuizBySectionId(sectionId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/section/{sectionId}/count")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Long> countQuizzesBySectionId(@PathVariable Long sectionId) {
        long count = quizService.countQuizzesBySectionId(sectionId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{quizId}/exists")
    public ResponseEntity<Boolean> checkQuizExists(@PathVariable Long quizId) {
        boolean exists = quizService.existsQuiz(quizId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{quizId}/can-access")
    public ResponseEntity<Boolean> canUserAccessQuiz(@PathVariable Long quizId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromPrincipal(userDetails);
        boolean canAccess = quizService.canUserAccessQuiz(quizId, userId);
        return ResponseEntity.ok(canAccess);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<QuizSummaryDTO>> searchQuizzes(@Valid @ModelAttribute QuizSearchRequest request) {
        List<QuizSummaryDTO> quizzes = quizService.searchQuizzes(request);
        return ResponseEntity.ok(quizzes);
    }
}