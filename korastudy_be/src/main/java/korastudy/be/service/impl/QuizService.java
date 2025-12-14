package korastudy.be.service.impl;

import korastudy.be.dto.request.quiz.*;
import korastudy.be.dto.response.quiz.*;
import korastudy.be.entity.Course.*;
import korastudy.be.entity.Enum.QuestionType;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.mapper.*;
import korastudy.be.repository.*;
import korastudy.be.service.IQuizService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService {

    private final QuizRepository quizRepository;
    private final SectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final TestResultRepository testResultRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final UserRepository userRepository;

    // ==================== QUIZ CRUD (ADMIN/TEACHER) ====================

    @Override
    @Transactional
    public QuizDTO createQuiz(QuizCreateRequest request) {
        log.info("Creating quiz for section ID: {}", request.getSectionId());

        Section section = sectionRepository.findById(request.getSectionId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy section với ID: " + request.getSectionId()));

        Quiz quiz = Quiz.builder().title(request.getTitle()).description(request.getDescription()).timeLimit(request.getTimeLimit()).passingScore(request.getPassingScore()).isPublished(request.getIsPublished()).isActive(request.getIsActive()).section(section).build();

        Quiz savedQuiz = quizRepository.save(quiz);

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<Question> questions = createQuestionsForQuiz(savedQuiz, request.getQuestions());
            savedQuiz.setQuestions(questions);
        }

        log.info("Quiz created successfully for section {} with ID: {}", request.getSectionId(), savedQuiz.getId());
        return QuizAdminMapper.toQuizDTO(savedQuiz);
    }

    @Override
    @Transactional
    public QuizDTO updateQuiz(Long quizId, QuizUpdateRequest request) {
        log.info("Updating quiz ID: {}", quizId);

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setIsPublished(request.getIsPublished());
        quiz.setIsActive(request.getIsActive() != null ? request.getIsActive() : quiz.getIsActive());

        Quiz updatedQuiz = quizRepository.save(quiz);
        log.info("Quiz updated successfully: {}", quizId);

        return QuizAdminMapper.toQuizDTO(updatedQuiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        questionRepository.deleteByQuizId(quizId);
        quizRepository.delete(quiz);
        log.info("Quiz deleted successfully: {}", quizId);
    }

    @Override
    @Transactional
    public void publishQuiz(Long quizId, boolean publish) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        quiz.setIsPublished(publish);
        quizRepository.save(quiz);
        log.info("Quiz {} {}", quizId, publish ? "published" : "unpublished");
    }

    // ==================== QUIZ VIEW (ADMIN/TEACHER) ====================

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizForTeacher(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));
        return QuizAdminMapper.toQuizDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizBasicInfoDTO getQuizBasicInfo(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));
        return QuizAdminMapper.toBasicInfoDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizSummaryDTO getQuizSummary(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));
        return QuizAdminMapper.toSummaryDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDTO> getQuizzesBySectionId(Long sectionId) {
        List<Quiz> quizzes = quizRepository.findBySectionId(sectionId);
        return QuizAdminMapper.toSummaryDTOs(quizzes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDTO> searchQuizzes(QuizSearchRequest request) {
        List<Quiz> quizzes = quizRepository.findAll();

        List<Quiz> filteredQuizzes = quizzes.stream().filter(quiz -> request.getSectionId() == null || quiz.getSection().getId().equals(request.getSectionId())).filter(quiz -> request.getTitle() == null || request.getTitle().isEmpty() || quiz.getTitle().toLowerCase().contains(request.getTitle().toLowerCase())).filter(quiz -> request.getIsPublished() == null || quiz.getIsPublished().equals(request.getIsPublished())).filter(quiz -> request.getIsActive() == null || quiz.getIsActive().equals(request.getIsActive())).skip((long) request.getPage() * request.getSize()).limit(request.getSize()).collect(Collectors.toList());

        return QuizAdminMapper.toSummaryDTOs(filteredQuizzes);
    }

    // ==================== QUIZ VIEW (STUDENT) ====================

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizForStudent(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        if (!quiz.getIsPublished() || !quiz.getIsActive()) {
            throw new IllegalArgumentException("Quiz không khả dụng để làm bài");
        }

        return QuizStudentMapper.toQuizDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDTO> getAvailableQuizzesForStudent(Long sectionId, Long userId) {
        log.info(" [QUIZ] Finding quizzes for sectionId: {}, userId: {}", sectionId, userId);

        List<Quiz> quizzes = quizRepository.findBySectionId(sectionId);
        log.info(" [QUIZ] Found {} quizzes from database", quizzes.size());

        // Log chi tiết từng quiz
        for (Quiz quiz : quizzes) {
            log.info(" Quiz ID: {}, Title: {}, isPublished: {}, isActive: {}", quiz.getId(), quiz.getTitle(), quiz.getIsPublished(), quiz.getIsActive());
        }

        //  DÙNG method helper để tránh NullPointerException
        List<QuizSummaryDTO> result = quizzes.stream().filter(quiz -> {
            boolean pass = isQuizAvailableForStudent(quiz);  // Dùng method helper
            if (!pass) {
                log.warn(" Quiz {} filtered out - isPublished: {}, isActive: {}", quiz.getId(), quiz.getIsPublished(), quiz.getIsActive());
            }
            return pass;
        }).map(QuizStudentMapper::toSummaryDTO).collect(Collectors.toList());

        log.info(" [QUIZ] Returning {} available quizzes after filtering", result.size());
        return result;
    }

    // Method helper xử lý null safety
    private boolean isQuizAvailableForStudent(Quiz quiz) {
        if (quiz == null) {
            log.warn(" Quiz is null");
            return false;
        }

        Boolean isPublished = quiz.getIsPublished();
        Boolean isActive = quiz.getIsActive();

        // Handle null values - treat null as false
        boolean published = Boolean.TRUE.equals(isPublished);
        boolean active = Boolean.TRUE.equals(isActive);

        log.debug(" Quiz {} - published: {}, active: {}", quiz.getId(), published, active);

        return published && active;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizStatusDTO getQuizStatusForStudent(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        List<TestResult> userResults = testResultRepository.findByUserIdAndQuizIdOrderByTakenDateDesc(userId, quizId);

        QuizStatusDTO status = QuizStudentMapper.toStatusDTO(quiz);

        if (!userResults.isEmpty()) {
            TestResult latestResult = userResults.get(0);
            status.setIsCompleted(true);
            status.setAttemptCount(userResults.size());

            Optional<TestResult> bestResult = userResults.stream().max(Comparator.comparing(TestResult::getScore));
            status.setBestScore(bestResult.map(TestResult::getScore).orElse(0.0));

            status.setIsPassed(latestResult.getIsPassed());
            status.setLastAttemptDate(latestResult.getTakenDate());
        } else {
            status.setIsCompleted(false);
            status.setAttemptCount(0);
            status.setBestScore(0.0);
            status.setIsPassed(false);
            status.setLastAttemptDate(null);
        }

        return status;
    }

    // ==================== QUESTION MANAGEMENT ====================

    @Override
    @Transactional
    public QuestionDTO addQuestionToQuiz(Long quizId, QuestionCreateRequest request) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        Integer orderIndex = request.getOrderIndex();
        if (orderIndex == null) {
            Integer maxOrder = questionRepository.findMaxOrderIndexByQuizId(quizId);
            orderIndex = maxOrder != null ? maxOrder + 1 : 1;
        }

        Question question = Question.builder().questionText(request.getQuestionText()).questionType(request.getQuestionType()).score(request.getScore()).orderIndex(orderIndex).imageUrl(request.getImageUrl()).explanation(request.getExplanation()).quiz(quiz).build();

        Question savedQuestion = questionRepository.save(question);

        // Với FILL_IN_BLANK, vẫn tạo options (tất cả đều isCorrect = true)
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            List<Option> options = createOptionsForQuestion(savedQuestion, request.getOptions());
            savedQuestion.setOptions(options);
        }

        log.info("Question added to quiz {}: {}", quizId, savedQuestion.getId());
        return QuestionMapper.toDTOForTeacher(savedQuestion);
    }

    @Override
    @Transactional
    public QuestionDTO updateQuestion(Long questionId, QuestionUpdateRequest request) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + questionId));

        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setScore(request.getScore());
        question.setOrderIndex(request.getOrderIndex());
        question.setImageUrl(request.getImageUrl());
        question.setExplanation(request.getExplanation());

        // Xóa options cũ và tạo mới
        optionRepository.deleteByQuestionId(questionId);

        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            //  Validate options trước khi tạo
            validateQuestionOptionsBasedOnType(request.getQuestionType(), request.getOptions().stream().map(opt -> {
                OptionCreateRequest createRequest = new OptionCreateRequest();
                createRequest.setOptionText(opt.getOptionText());
                createRequest.setIsCorrect(opt.getIsCorrect());
                createRequest.setImageUrl(opt.getImageUrl());
                return createRequest;
            }).collect(Collectors.toList()));

            // Tự động set isCorrect = true cho FILL_IN_BLANK
            if (request.getQuestionType() == QuestionType.FILL_IN_BLANK) {
                request.getOptions().forEach(opt -> opt.setIsCorrect(true));
            }

            List<Option> newOptions = createOptionsForQuestion(question, request.getOptions());
            question.setOptions(newOptions);
        }

        Question updatedQuestion = questionRepository.save(question);
        log.info("Question updated: {}", questionId);
        return QuestionMapper.toDTOForTeacher(updatedQuestion);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + questionId);
        }
        questionRepository.deleteById(questionId);
        log.info("Question deleted: {}", questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsForTeacher(Long quizId) {
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndex(quizId);
        return QuestionMapper.toDTOsForTeacher(questions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsForStudent(Long quizId) {
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndex(quizId);
        return QuestionMapper.toDTOsForStudent(questions);
    }

    // ==================== OPTION MANAGEMENT ====================

    @Override
    @Transactional
    public OptionDTO addOptionToQuestion(Long questionId, OptionCreateRequest request) {
        log.info("Adding option to question ID: {}", questionId);

        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + questionId));

        // Cho phép FILL_IN_BLANK thêm option
        // Chỉ không cho phép ESSAY
        if (question.getQuestionType() == QuestionType.ESSAY) {
            throw new IllegalArgumentException("Câu hỏi loại ESSAY không thể thêm option");
        }

        // Với FILL_IN_BLANK, option phải luôn isCorrect = true
        if (question.getQuestionType() == QuestionType.FILL_IN_BLANK && !request.getIsCorrect()) {
            throw new IllegalArgumentException("Câu hỏi FILL_IN_BLANK chỉ có đáp án đúng, không có đáp án sai");
        }

        // Kiểm tra số lượng option hiện có
        List<Option> existingOptions = optionRepository.findByQuestionId(questionId);
        if (question.getQuestionType() == QuestionType.TRUE_FALSE && existingOptions.size() >= 2) {
            throw new IllegalArgumentException("Câu hỏi TRUE/FALSE chỉ được có tối đa 2 option");
        }

        Integer maxOrder = optionRepository.findMaxOrderIndexByQuestionId(questionId);
        Integer orderIndex = maxOrder != null ? maxOrder + 1 : 1;

        Option option = Option.builder().optionText(request.getOptionText()).isCorrect(request.getIsCorrect()).imageUrl(request.getImageUrl()).orderIndex(orderIndex).question(question).build();

        Option savedOption = optionRepository.save(option);
        log.info("Option added successfully to question {}: {}", questionId, savedOption.getId());

        return OptionMapper.toDTO(savedOption);
    }

    @Override
    @Transactional
    public OptionDTO updateOption(Long optionId, OptionUpdateRequest request) {
        log.info("Updating option ID: {}", optionId);

        Option option = optionRepository.findById(optionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy option với ID: " + optionId));

        option.setOptionText(request.getOptionText());
        option.setIsCorrect(request.getIsCorrect());
        option.setImageUrl(request.getImageUrl());

        Option updatedOption = optionRepository.save(option);
        log.info("Option updated successfully: {}", optionId);

        return OptionMapper.toDTO(updatedOption);
    }

    @Override
    @Transactional
    public void deleteOption(Long optionId) {
        if (!optionRepository.existsById(optionId)) {
            throw new ResourceNotFoundException("Không tìm thấy option với ID: " + optionId);
        }

        Option option = optionRepository.findById(optionId).get();

        // Với FILL_IN_BLANK, vẫn cho phép xóa option
        // Chỉ kiểm tra TRUE_FALSE
        if (option.getQuestion().getQuestionType() == QuestionType.TRUE_FALSE) {
            long remainingOptions = optionRepository.countByQuestionId(option.getQuestion().getId()) - 1;
            if (remainingOptions < 2) {
                throw new IllegalArgumentException("Câu hỏi TRUE/FALSE phải có ít nhất 2 option");
            }
        }

        optionRepository.deleteById(optionId);
        log.info("Option deleted successfully: {}", optionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionDTO> getOptionsByQuestionId(Long questionId) {
        List<Option> options = optionRepository.findByQuestionId(questionId);
        return OptionMapper.toDTOs(options);
    }

    // Thêm phương thức helper để validate option đúng
    private void validateQuestionOptions(Question question, List<?> optionRequests) {
        if (optionRequests == null) return;

        long correctCount = optionRequests.stream().filter(req -> {
            if (req instanceof OptionCreateRequest) {
                return ((OptionCreateRequest) req).getIsCorrect();
            } else if (req instanceof OptionUpdateRequest) {
                return ((OptionUpdateRequest) req).getIsCorrect();
            }
            return false;
        }).count();

        // Validate SINGLE_CHOICE phải có đúng 1 đáp án đúng
        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE && correctCount != 1) {
            throw new IllegalArgumentException("Câu hỏi SINGLE_CHOICE phải có đúng 1 đáp án đúng");
        }

        // Validate MULTIPLE_CHOICE phải có ít nhất 1 đáp án đúng
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE && correctCount < 1) {
            throw new IllegalArgumentException("Câu hỏi MULTIPLE_CHOICE phải có ít nhất 1 đáp án đúng");
        }
    }
    // ==================== QUIZ TAKING & SUBMISSION ====================

    @Override
    @Transactional
    public TestResultDTO startQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));

        if (!quiz.getIsPublished() || !quiz.getIsActive()) {
            throw new IllegalArgumentException("Quiz không khả dụng");
        }

        TestResult testResult = TestResult.builder().quiz(quiz).user(user).takenDate(LocalDateTime.now()).build();

        TestResult savedResult = testResultRepository.save(testResult);
        log.info("Quiz started for user {}: result ID {}", userId, savedResult.getId());

        return TestResultMapper.toDTO(savedResult);
    }

    @Override
    @Transactional
    public TestResultDTO submitQuiz(Long quizId, QuizSubmissionRequest request, Long userId) {
        log.info("Submitting quiz {} for user: {}", quizId, userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        if (!quiz.getIsPublished() || !quiz.getIsActive()) {
            throw new IllegalArgumentException("Quiz không khả dụng");
        }

        QuizGradingResult gradingResult = calculateScore(quiz, request);
        TestResult testResult = saveTestResult(quiz, user, gradingResult, request.getTimeSpentInSeconds());
        saveQuizAnswers(testResult, request.getAnswers(), gradingResult);

        log.info("Quiz submitted successfully. Score: {}/{}", gradingResult.getEarnedPoints(), gradingResult.getTotalPoints());

        return TestResultMapper.toDTO(testResult);
    }

    @Override
    @Transactional
    public void saveAnswer(Long quizId, AnswerRequest request, Long userId) {
        log.debug("Saving temporary answer for user {} to question {}", userId, request.getQuestionId());
    }

    // ==================== RESULTS & ANALYTICS ====================

    @Override
    @Transactional(readOnly = true)
    public QuizResultDetailDTO getQuizResultForStudent(Long resultId, Long userId) {
        TestResult testResult = testResultRepository.findById(resultId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả với ID: " + resultId));

        if (!testResult.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xem kết quả này");
        }

        List<QuizAnswer> quizAnswers = quizAnswerRepository.findByTestResultId(resultId);

        TestResultDTO testResultDTO = TestResultMapper.toDTO(testResult);

        List<AnswerResultDTO> answerDetails = quizAnswers.stream().map(QuizAnswerMapper::toAnswerResultDTOForStudent).collect(Collectors.toList());

        testResultDTO.setAnswerDetails(answerDetails);

        QuizBasicInfoDTO quizInfo = QuizStudentMapper.toBasicInfoDTO(testResult.getQuiz());

        return QuizResultDetailDTO.builder().summary(testResultDTO).answerDetails(answerDetails).quizInfo(quizInfo).build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResultDetailDTO getQuizResultForTeacher(Long resultId) {
        TestResult testResult = testResultRepository.findById(resultId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả với ID: " + resultId));

        List<QuizAnswer> quizAnswers = quizAnswerRepository.findByTestResultId(resultId);

        TestResultDTO testResultDTO = TestResultMapper.toDTO(testResult);

        List<AnswerResultDTO> answerDetails = quizAnswers.stream().map(QuizAnswerMapper::toAnswerResultDTO).collect(Collectors.toList());

        testResultDTO.setAnswerDetails(answerDetails);

        QuizBasicInfoDTO quizInfo = QuizAdminMapper.toBasicInfoDTO(testResult.getQuiz());

        return QuizResultDetailDTO.builder().summary(testResultDTO).answerDetails(answerDetails).quizInfo(quizInfo).build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResultDTO> getUserQuizHistory(Long userId) {
        List<TestResult> results = testResultRepository.findByUserIdOrderByTakenDateDesc(userId);
        return results.stream().map(TestResultMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAllResultsDTO getAllQuizResults(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        List<TestResult> results = testResultRepository.findByQuizIdOrderByScoreDesc(quizId);

        List<QuizAllResultsDTO.UserResultDTO> userResults = results.stream().map(result -> QuizAllResultsDTO.UserResultDTO.builder().userId(result.getUser().getId()).username(result.getUser().getAccount().getUsername()).score(result.getScore()).isPassed(result.getIsPassed()).takenDate(result.getTakenDate()).timeSpent(result.getTimeSpent()).build()).collect(Collectors.toList());

        return QuizAllResultsDTO.builder().quizId(quizId).quizTitle(quiz.getTitle()).userResults(userResults).build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizStatisticsDTO getQuizStatistics(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        List<TestResult> results = testResultRepository.findByQuizIdOrderByScoreDesc(quizId);
        return calculateQuizStatistics(quiz, results);
    }

    // ==================== UTILITY METHODS ====================

    @Override
    public boolean existsQuiz(Long quizId) {
        return quizRepository.existsById(quizId);
    }

    @Override
    public boolean canUserAccessQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return false;

        return quiz.getIsPublished() && quiz.getIsActive();
    }

    @Override
    public long countQuizzesBySectionId(Long sectionId) {
        return quizRepository.countBySectionId(sectionId);
    }

    @Override
    public void validateQuizForTaking(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        if (!quiz.getIsPublished() || !quiz.getIsActive()) {
            throw new IllegalArgumentException("Quiz không khả dụng");
        }

        List<TestResult> previousAttempts = testResultRepository.findByUserIdAndQuizIdOrderByTakenDateDesc(userId, quizId);
        if (!previousAttempts.isEmpty()) {
            log.info("User {} has {} previous attempts for quiz {}", userId, previousAttempts.size(), quizId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TestResultDTO getQuizResult(Long resultId) {
        TestResult testResult = testResultRepository.findById(resultId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả với ID: " + resultId));

        List<QuizAnswer> quizAnswers = quizAnswerRepository.findByTestResultId(resultId);
        return TestResultMapper.toDTOWithDetails(testResult, quizAnswers);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResultDetailDTO getQuizResultDetail(Long resultId) {
        TestResultDTO testResult = getQuizResult(resultId);

        Quiz quiz = quizRepository.findById(testResult.getQuizId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz"));

        QuizBasicInfoDTO quizInfo = QuizAdminMapper.toBasicInfoDTO(quiz);

        return QuizResultDetailDTO.builder().summary(testResult).quizInfo(quizInfo).build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResultDTO> getQuizResults(Long quizId) {
        List<TestResult> results = testResultRepository.findByQuizIdOrderByScoreDesc(quizId);
        return results.stream().map(result -> {
            List<QuizAnswer> quizAnswers = quizAnswerRepository.findByTestResultIdOrderByQuestion(result.getId());
            return TestResultMapper.toDTOWithDetails(result, quizAnswers);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizStatisticsDTO> getQuizStatisticsBySectionId(Long sectionId) {
        List<Quiz> quizzes = quizRepository.findBySectionId(sectionId);
        return quizzes.stream().map(quiz -> {
            List<TestResult> results = testResultRepository.findByQuizIdOrderByScoreDesc(quiz.getId());
            return calculateQuizStatistics(quiz, results);
        }).collect(Collectors.toList());
    }

    @Override
    public boolean existsAnyQuizBySectionId(Long sectionId) {
        return quizRepository.existsBySectionId(sectionId);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private List<Question> createQuestionsForQuiz(Quiz quiz, List<QuestionCreateRequest> questionRequests) {
        List<Question> questions = new ArrayList<>();

        for (QuestionCreateRequest questionRequest : questionRequests) {
            // Validate trước khi tạo
            if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
                validateQuestionOptionsBasedOnType(questionRequest.getQuestionType(), questionRequest.getOptions());

                // ⭐ THÊM: Tự động set isCorrect = true cho FILL_IN_BLANK
                if (questionRequest.getQuestionType() == QuestionType.FILL_IN_BLANK) {
                    questionRequest.getOptions().forEach(option -> option.setIsCorrect(true));
                }
            }

            Question question = Question.builder().questionText(questionRequest.getQuestionText()).questionType(questionRequest.getQuestionType()).score(questionRequest.getScore()).orderIndex(questionRequest.getOrderIndex()).imageUrl(questionRequest.getImageUrl()).explanation(questionRequest.getExplanation())
                    // ⭐ XÓA: .correctAnswer(questionRequest.getCorrectAnswer())
                    .quiz(quiz).build();

            Question savedQuestion = questionRepository.save(question);

            if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
                List<Option> options = createOptionsForQuestion(savedQuestion, questionRequest.getOptions());
                savedQuestion.setOptions(options);
            }

            questions.add(savedQuestion);
        }
        return questions;
    }

    private void validateQuestionOptionsBasedOnType(QuestionType questionType, List<OptionCreateRequest> options) {
        long correctCount = options.stream().filter(OptionCreateRequest::getIsCorrect).count();

        switch (questionType) {
            case SINGLE_CHOICE:
                if (correctCount != 1) {
                    throw new IllegalArgumentException("Câu hỏi SINGLE_CHOICE phải có đúng 1 đáp án đúng");
                }
                break;
            case MULTIPLE_CHOICE:
                if (correctCount < 1) {
                    throw new IllegalArgumentException("Câu hỏi MULTIPLE_CHOICE phải có ít nhất 1 đáp án đúng");
                }
                break;
            case TRUE_FALSE:
                if (options.size() != 2) {
                    throw new IllegalArgumentException("Câu hỏi TRUE_FALSE phải có đúng 2 option");
                }
                if (correctCount != 1) {
                    throw new IllegalArgumentException("Câu hỏi TRUE_FALSE phải có đúng 1 đáp án đúng");
                }
                break;
            case FILL_IN_BLANK:
                for (OptionCreateRequest option : options) {
                    if (!option.getIsCorrect()) {
                        throw new IllegalArgumentException("Câu hỏi FILL_IN_BLANK chỉ có đáp án đúng, không có đáp án sai");
                    }
                }
                break;
            case ESSAY:
                if (!options.isEmpty()) {
                    throw new IllegalArgumentException("Câu hỏi ESSAY không có lựa chọn, chỉ có câu trả lời tự luận");
                }
                break;
        }
    }

    private List<Option> createOptionsForQuestion(Question question, List<?> optionRequests) {
        List<Option> options = new ArrayList<>();

        if (optionRequests != null && !optionRequests.isEmpty()) {
            for (Object request : optionRequests) {
                Option.OptionBuilder builder = Option.builder().question(question);

                if (request instanceof OptionCreateRequest) {
                    OptionCreateRequest createRequest = (OptionCreateRequest) request;
                    builder.optionText(createRequest.getOptionText()).isCorrect(createRequest.getIsCorrect()).imageUrl(createRequest.getImageUrl());
                } else if (request instanceof OptionUpdateRequest) {
                    OptionUpdateRequest updateRequest = (OptionUpdateRequest) request;
                    builder.optionText(updateRequest.getOptionText()).isCorrect(updateRequest.getIsCorrect()).imageUrl(updateRequest.getImageUrl());
                }

                options.add(builder.build());
            }
            return optionRepository.saveAll(options);
        }

        return options;
    }

    private QuizGradingResult calculateScore(Quiz quiz, QuizSubmissionRequest request) {
        QuizGradingResult result = new QuizGradingResult();
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());

        result.setTotalQuestions(questions.size());
        result.setTotalPoints(questions.stream().mapToDouble(Question::getScore).sum());

        double totalScore = 0;
        int correctAnswers = 0;
        List<AnswerResult> answerResults = new ArrayList<>();

        // Tạo map để tìm câu hỏi nhanh hơn
        Map<Long, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        for (AnswerRequest answerRequest : request.getAnswers()) {
            AnswerResult answerResult = new AnswerResult();

            Question question = questionMap.get(answerRequest.getQuestionId());
            if (question == null) {
                throw new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + answerRequest.getQuestionId());
            }

            answerResult.setQuestion(question);
            answerResult.setUserAnswer(serializeUserAnswer(answerRequest));

            boolean isCorrect = checkAnswerCorrect(question, answerRequest);
            answerResult.setCorrect(isCorrect);

            double earnedScore;
            if (question.getQuestionType() == QuestionType.ESSAY) {
                // Essay cần xử lý đặc biệt
                earnedScore = calculateEssayScore(question, answerRequest.getEssayAnswer());
            } else {
                earnedScore = isCorrect ? question.getScore() : 0;
            }

            answerResult.setEarnedScore(earnedScore);
            totalScore += earnedScore;
            if (isCorrect) correctAnswers++;

            answerResults.add(answerResult);
        }

        result.setEarnedPoints(totalScore);
        result.setCorrectAnswers(correctAnswers);
        result.setAnswerResults(answerResults);
        return result;
    }

    private boolean checkAnswerCorrect(Question question, AnswerRequest answerRequest) {
        return switch (question.getQuestionType()) {
            case SINGLE_CHOICE -> checkSingleChoice(question, answerRequest.getSelectedOptionId());
            case MULTIPLE_CHOICE -> checkMultipleChoice(question, answerRequest.getSelectedOptionIds());
            case TRUE_FALSE -> checkTrueFalse(question, answerRequest.getSelectedOptionId());
            case FILL_IN_BLANK -> checkFillInBlank(question, answerRequest.getFillInBlankAnswer());
            case ESSAY -> checkEssay(question, answerRequest.getEssayAnswer());
        };
    }

    private boolean checkSingleChoice(Question question, Long selectedOptionId) {
        if (selectedOptionId == null || question.getOptions() == null) return false;

        Optional<Option> selectedOption = question.getOptions().stream().filter(option -> option.getId().equals(selectedOptionId)).findFirst();

        return selectedOption.isPresent() && Boolean.TRUE.equals(selectedOption.get().getIsCorrect());
    }

    private boolean checkMultipleChoice(Question question, List<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty() || question.getOptions() == null) return false;

        Set<Long> correctOptionIds = question.getOptions().stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).map(Option::getId).collect(Collectors.toSet());

        Set<Long> selectedIds = new HashSet<>(selectedOptionIds);

        Set<Long> incorrectOptionIds = question.getOptions().stream().filter(option -> !Boolean.TRUE.equals(option.getIsCorrect())).map(Option::getId).collect(Collectors.toSet());

        boolean hasAllCorrect = selectedIds.containsAll(correctOptionIds);
        boolean hasNoIncorrect = Collections.disjoint(selectedIds, incorrectOptionIds);

        return hasAllCorrect && hasNoIncorrect;
    }

    private boolean checkTrueFalse(Question question, Long selectedOptionId) {
        if (selectedOptionId == null || question.getOptions() == null) return false;

        Optional<Option> selectedOption = question.getOptions().stream().filter(option -> option.getId().equals(selectedOptionId)).findFirst();

        return selectedOption.isPresent() && Boolean.TRUE.equals(selectedOption.get().getIsCorrect());
    }

    private boolean checkFillInBlank(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) return false;

        // Không dùng correctAnswer nữa, check trong options
        if (question.getOptions() == null || question.getOptions().isEmpty()) return false;

        String normalizedUserAnswer = userAnswer.trim().toLowerCase();

        // Check xem user answer có trùng với bất kỳ option nào không
        return question.getOptions().stream().map(Option::getOptionText).filter(Objects::nonNull).map(String::trim).map(String::toLowerCase).anyMatch(correctAnswer -> correctAnswer.equals(normalizedUserAnswer));
    }

    private boolean checkEssay(Question question, String userAnswer) {
        // Essay luôn được coi là đã trả lời (điểm sẽ được tính sau khi giáo viên chấm)
        // Hoặc có thể để điểm mặc định là 0 và chấm sau
        return userAnswer != null && !userAnswer.trim().isEmpty();
    }

    // Thêm logic xử lý điểm cho ESSAY
    private double calculateEssayScore(Question question, String userAnswer) {
        // Mặc định ESSAY cho 0 điểm, cần giáo viên chấm sau
        // Hoặc có thể implement AI scoring nếu cần
        return 0.0;
    }

    private String serializeUserAnswer(AnswerRequest answerRequest) {
        if (answerRequest.getSelectedOptionIds() != null && !answerRequest.getSelectedOptionIds().isEmpty()) {
            return String.join(",", answerRequest.getSelectedOptionIds().stream().map(String::valueOf).collect(Collectors.toList()));
        } else if (answerRequest.getSelectedOptionId() != null) {
            return answerRequest.getSelectedOptionId().toString();
        } else if (answerRequest.getEssayAnswer() != null) {
            return answerRequest.getEssayAnswer();
        } else if (answerRequest.getFillInBlankAnswer() != null) {
            return answerRequest.getFillInBlankAnswer();
        }
        return "";
    }

    private TestResult saveTestResult(Quiz quiz, User user, QuizGradingResult gradingResult, Long timeSpent) {
        double scorePercentage = gradingResult.getTotalPoints() > 0 ? (gradingResult.getEarnedPoints() / gradingResult.getTotalPoints()) * 100 : 0;

        TestResult testResult = TestResult.builder().quiz(quiz).user(user).score(scorePercentage).totalPoints(gradingResult.getTotalPoints()).earnedPoints(gradingResult.getEarnedPoints()).correctAnswers(gradingResult.getCorrectAnswers()).totalQuestions(gradingResult.getTotalQuestions()).timeSpent(timeSpent != null ? timeSpent : 0L).takenDate(LocalDateTime.now()).isPassed(scorePercentage >= quiz.getPassingScore()).build();

        return testResultRepository.save(testResult);
    }

    private void saveQuizAnswers(TestResult testResult, List<AnswerRequest> answers, QuizGradingResult gradingResult) {
        List<QuizAnswer> quizAnswers = new ArrayList<>();

        Map<Long, AnswerResult> answerResultMap = gradingResult.getAnswerResults().stream().collect(Collectors.toMap(ar -> ar.getQuestion().getId(), ar -> ar));

        for (AnswerRequest answerRequest : answers) {
            AnswerResult answerResult = answerResultMap.get(answerRequest.getQuestionId());
            if (answerResult == null) continue;

            QuizAnswer quizAnswer = QuizAnswer.builder().testResult(testResult).question(answerResult.getQuestion()).userAnswer(answerResult.getUserAnswer()).isCorrect(answerResult.isCorrect()).earnedScore(answerResult.getEarnedScore()).createdAt(LocalDateTime.now()).build();

            quizAnswers.add(quizAnswer);
        }

        quizAnswerRepository.saveAll(quizAnswers);
    }

    private QuizStatisticsDTO calculateQuizStatistics(Quiz quiz, List<TestResult> results) {
        if (results.isEmpty()) {
            return QuizStatisticsDTO.builder().quizId(quiz.getId()).quizTitle(quiz.getTitle()).totalAttempts(0).passedAttempts(0).averageScore(0.0).highestScore(0.0).lowestScore(0.0).averageTimeSpent(0.0).firstAttemptDate(null).lastAttemptDate(null).questionStats(Collections.emptyList()).build();
        }

        long passedCount = results.stream().filter(result -> result.getScore() >= quiz.getPassingScore()).count();

        double averageScore = results.stream().mapToDouble(TestResult::getScore).average().orElse(0.0);

        double highestScore = results.stream().mapToDouble(TestResult::getScore).max().orElse(0.0);

        double lowestScore = results.stream().mapToDouble(TestResult::getScore).min().orElse(0.0);

        double averageTimeSpent = results.stream().mapToLong(TestResult::getTimeSpent).average().orElse(0.0);

        LocalDateTime firstAttempt = results.stream().map(TestResult::getTakenDate).min(LocalDateTime::compareTo).orElse(null);

        LocalDateTime lastAttempt = results.stream().map(TestResult::getTakenDate).max(LocalDateTime::compareTo).orElse(null);

        return QuizStatisticsDTO.builder().quizId(quiz.getId()).quizTitle(quiz.getTitle()).totalAttempts(results.size()).passedAttempts((int) passedCount).averageScore(Math.round(averageScore * 100.0) / 100.0).highestScore(Math.round(highestScore * 100.0) / 100.0).lowestScore(Math.round(lowestScore * 100.0) / 100.0).averageTimeSpent(Math.round(averageTimeSpent * 100.0) / 100.0).firstAttemptDate(firstAttempt).lastAttemptDate(lastAttempt).questionStats(Collections.emptyList()).build();
    }

    // ==================== INNER CLASSES ====================

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class QuizGradingResult {
        private double totalPoints;
        private double earnedPoints;
        private int totalQuestions;
        private int correctAnswers;
        private List<AnswerResult> answerResults = new ArrayList<>();
    }

    @Getter
    @Setter
    private static class AnswerResult {
        private Question question;
        private boolean isCorrect;
        private double earnedScore;
        private String userAnswer;
    }
}