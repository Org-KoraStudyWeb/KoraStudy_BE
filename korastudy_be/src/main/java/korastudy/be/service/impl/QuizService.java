package korastudy.be.service.impl;

import korastudy.be.dto.request.quiz.*;
import korastudy.be.dto.response.quiz.*;
import korastudy.be.entity.Course.*;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
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

    // ==================== QUIZ CRUD ====================

    @Override
    @Transactional
    public QuizDTO createQuiz(QuizCreateRequest request) {
        // Tạo quiz mới cho section với các thông tin cơ bản và questions (nếu có)
        log.info("Creating quiz for section ID: {}", request.getSectionId());

        Section section = sectionRepository.findById(request.getSectionId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy section với ID: " + request.getSectionId()));

        Quiz quiz = Quiz.builder().title(request.getTitle()).description(request.getDescription()).timeLimit(request.getTimeLimit()).passingScore(request.getPassingScore()).section(section).build();

        Quiz savedQuiz = quizRepository.save(quiz);

        // Tạo questions và options nếu request có chứa questions
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<Question> questions = createQuestionsForQuiz(savedQuiz, request.getQuestions());
            savedQuiz.setQuestions(questions);
        }

        log.info("Quiz created successfully for section {} with ID: {}", request.getSectionId(), savedQuiz.getId());
        return mapToDTO(savedQuiz);
    }

    @Override
    @Transactional
    public QuizDTO updateQuiz(Long quizId, QuizUpdateRequest request) {
        // Cập nhật thông tin cơ bản của quiz (title, description, timeLimit, passingScore)
        log.info("Updating quiz ID: {}", quizId);

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setPassingScore(request.getPassingScore());

        Quiz updatedQuiz = quizRepository.save(quiz);
        log.info("Quiz updated successfully: {}", quizId);

        return mapToDTO(updatedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizById(Long id) {
        // Lấy chi tiết quiz theo ID (bao gồm questions và options) - dành cho giáo viên
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + id));
        return mapToDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> getQuizzesBySectionId(Long sectionId) {
        // Lấy danh sách tất cả quiz thuộc section
        List<Quiz> quizzes = quizRepository.findBySectionId(sectionId);
        return quizzes.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizBySectionAndId(Long sectionId, Long quizId) {
        // Lấy quiz cụ thể theo section ID và quiz ID (validation quiz thuộc section)
        Quiz quiz = quizRepository.findBySectionIdAndId(sectionId, quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId + " trong section: " + sectionId));
        return mapToDTO(quiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long id) {
        // Xóa quiz và tất cả questions, options liên quan (cascade delete)
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + id));

        questionRepository.deleteByQuizId(id);
        quizRepository.delete(quiz);

        log.info("Quiz deleted successfully: {}", id);
    }

    // ==================== QUESTION MANAGEMENT ====================

    @Override
    @Transactional
    public QuestionDTO addQuestionToQuiz(Long quizId, QuestionCreateRequest request) {
        // Thêm câu hỏi mới vào quiz với tự động sắp xếp thứ tự
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        // Tự động set orderIndex nếu không được cung cấp
        Integer orderIndex = request.getOrderIndex();
        if (orderIndex == null) {
            Integer maxOrder = questionRepository.findMaxOrderIndexByQuizId(quizId);
            orderIndex = maxOrder != null ? maxOrder + 1 : 1;
        }

        // Tạo entity Question với các thông tin từ request và orderIndex đã tính
        Question question = Question.builder()
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .score(request.getScore())
                .orderIndex(orderIndex) // SET ORDER INDEX đã được tính toán
                .quiz(quiz)
                .build();

        // Lưu question vào database
        Question savedQuestion = questionRepository.save(question);

        // Tạo và liên kết các options (lựa chọn) cho câu hỏi
        List<Option> options = createOptionsForQuestion(savedQuestion, request.getOptions());
        savedQuestion.setOptions(options);

        log.info("Question added to quiz {}: {}", quizId, savedQuestion.getId());

        // Trả về DTO của question đã tạo
        return mapToDTO(savedQuestion);
    }

    @Override
    @Transactional
    public QuestionDTO updateQuestion(Long questionId, QuestionUpdateRequest request) {
        // Cập nhật thông tin câu hỏi và thay thế toàn bộ options
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + questionId));

        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setScore(request.getScore());

        // Xóa options cũ và tạo mới (full replace)
        optionRepository.deleteByQuestionId(questionId);

        List<Option> newOptions = new ArrayList<>();
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            // Tạo options mới từ OptionUpdateRequest
            newOptions = createOptionsFromUpdateRequest(question, request.getOptions());
        }
        question.setOptions(newOptions);

        Question updatedQuestion = questionRepository.save(question);
        log.info("Question updated: {}", questionId);

        return mapToDTO(updatedQuestion);
    }

    private List<Option> createOptionsFromUpdateRequest(Question question, List<OptionUpdateRequest> optionRequests) {
        // Tạo danh sách Option entities từ OptionUpdateRequest DTOs
        List<Option> options = new ArrayList<>();

        for (OptionUpdateRequest optionRequest : optionRequests) {
            Option option = Option.builder()
                    .optionText(optionRequest.getOptionText())
                    .isCorrect(optionRequest.getIsCorrect())
                    .question(question)
                    .build();
            options.add(option);
        }
        return optionRepository.saveAll(options);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        // Xóa câu hỏi và các options liên quan (cascade delete)
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + questionId);
        }
        questionRepository.deleteById(questionId);
        log.info("Question deleted: {}", questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsByQuizId(Long quizId) {
        // Lấy câu hỏi theo thứ tự
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndex(quizId);
        return questions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ==================== QUIZ TAKING & SUBMISSION ====================

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizForTaking(Long quizId) {
        // Lấy quiz để học viên làm bài (ẩn đáp án đúng)
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        QuizDTO quizDTO = mapToDTO(quiz);
        hideCorrectAnswers(quizDTO);

        log.info("Quiz prepared for taking: {}", quizId);
        return quizDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> getQuizzesForTakingBySectionId(Long sectionId) {
        // Lấy tất cả quiz trong section để học viên làm bài (ẩn đáp án đúng)
        List<Quiz> quizzes = quizRepository.findBySectionId(sectionId);
        return quizzes.stream().map(this::mapToDTO).map(this::hideCorrectAnswers).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getDefaultQuizForTakingBySectionId(Long sectionId) {
        // Lấy quiz mặc định (đầu tiên) trong section để học viên làm bài (ẩn đáp án đúng)
        Quiz quiz = quizRepository.findBySectionId(sectionId).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz nào cho section: " + sectionId));

        QuizDTO quizDTO = mapToDTO(quiz);
        return hideCorrectAnswers(quizDTO);
    }

    @Override
    @Transactional
    public TestResultDTO submitQuiz(Long quizId, QuizSubmissionRequest request, String username) {
        // Nộp bài quiz và chấm điểm tự động
        log.info("Submitting quiz {} for user: {}", quizId, username);

        User user = userRepository.findByAccount_Username(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với username: " + username));

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        // Kiểm tra user đã làm quiz này chưa
        if (testResultRepository.existsByUserIdAndQuizId(user.getId(), quizId)) {
            throw new IllegalArgumentException("Bạn đã làm quiz này rồi");
        }

        QuizGradingResult gradingResult = calculateScore(quiz, request);
        TestResult testResult = saveTestResult(quiz, user, gradingResult, request.getTimeSpent());
        saveQuizAnswers(testResult, request.getAnswers(), gradingResult);

        log.info("Quiz submitted successfully. Score: {}/{}", gradingResult.getEarnedPoints(), gradingResult.getTotalPoints());
        return mapToTestResultDTO(testResult, gradingResult);
    }

    // ==================== RESULTS & ANALYTICS ====================

    @Override
    @Transactional(readOnly = true)
    public TestResultDTO getQuizResult(Long resultId) {
        // Lấy kết quả chi tiết của một bài quiz đã làm theo ID kết quả
        TestResult testResult = testResultRepository.findById(resultId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả với ID: " + resultId));

        List<QuizAnswer> quizAnswers = quizAnswerRepository.findByTestResultIdOrderByQuestion(resultId);
        return mapToTestResultDTO(testResult, quizAnswers);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResultDetailDTO getQuizResultDetail(Long resultId) {
        // Lấy chi tiết đầy đủ kết quả quiz (bao gồm thông tin quiz và kết quả)
        TestResultDTO testResult = getQuizResult(resultId);
        QuizDTO quizInfo = getQuizById(testResult.getQuizId());

        return QuizResultDetailDTO.builder().summary(testResult).quizInfo(quizInfo).build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResultDTO> getUserQuizResults(String username) {
        // Lấy lịch sử tất cả bài quiz mà user đã làm (mới nhất trước)
        User user = userRepository.findByAccount_Username(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với username: " + username));

        List<TestResult> results = testResultRepository.findByUserIdOrderByTakenDateDesc(user.getId());
        return results.stream().map(result -> {
            List<QuizAnswer> quizAnswers = quizAnswerRepository.findByTestResultIdOrderByQuestion(result.getId());
            return mapToTestResultDTO(result, quizAnswers);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResultDTO> getQuizResults(Long quizId) {
        // Lấy kết quả của tất cả user cho một quiz cụ thể (điểm cao nhất trước)
        List<TestResult> results = testResultRepository.findByQuizIdOrderByScoreDesc(quizId);
        return results.stream().map(result -> {
            List<QuizAnswer> quizAnswers = quizAnswerRepository.findByTestResultIdOrderByQuestion(result.getId());
            return mapToTestResultDTO(result, quizAnswers);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuizStatisticsDTO getQuizStatistics(Long quizId) {
        // Lấy thống kê chi tiết của một quiz (số người làm, điểm trung bình, tỷ lệ đỗ...)
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        List<TestResult> results = testResultRepository.findByQuizIdOrderByScoreDesc(quizId);
        return calculateQuizStatistics(quiz, results);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizStatisticsDTO> getQuizStatisticsBySectionId(Long sectionId) {
        // Lấy thống kê của tất cả quiz trong một section
        List<Quiz> quizzes = quizRepository.findBySectionId(sectionId);
        return quizzes.stream().map(quiz -> {
            List<TestResult> results = testResultRepository.findByQuizIdOrderByScoreDesc(quiz.getId());
            return calculateQuizStatistics(quiz, results);
        }).collect(Collectors.toList());
    }

    // ==================== VALIDATION METHODS ====================

    @Override
    public boolean existsAnyQuizBySectionId(Long sectionId) {
        return quizRepository.existsBySectionId(sectionId);
    }

    @Override
    public long countQuizzesBySectionId(Long sectionId) {
        return quizRepository.countBySectionId(sectionId);
    }

    // ==================== UTILITY METHODS ====================

    @Override
    public QuizDTO mapToDTO(Quiz quiz) {
        List<QuestionDTO> questionDTOs = quiz.getQuestions() != null ? quiz.getQuestions().stream().map(this::mapToDTO).collect(Collectors.toList()) : Collections.emptyList();

        return QuizDTO.builder().id(quiz.getId()).title(quiz.getTitle()).description(quiz.getDescription()).timeLimit(quiz.getTimeLimit()).passingScore(quiz.getPassingScore()).sectionId(quiz.getSection().getId()).sectionName(quiz.getSection().getSectionName()).questions(questionDTOs).createdAt(quiz.getCreatedAt()).updatedAt(quiz.getLastModified()).build();
    }

    @Override
    public QuestionDTO mapToDTO(Question question) {
        List<OptionDTO> optionDTOs = question.getOptions() != null ?
                question.getOptions().stream()
                        .map(this::mapToOptionDTO)
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return QuestionDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .score(question.getScore())
                .options(optionDTOs)
                .orderIndex(question.getOrderIndex())
                .build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private List<Question> createQuestionsForQuiz(Quiz quiz, List<QuestionCreateRequest> questionRequests) {
        // Tạo danh sách questions và options từ request khi tạo quiz
        List<Question> questions = new ArrayList<>();

        for (QuestionCreateRequest questionRequest : questionRequests) {
            // Tạo question entity - KHÔNG set createdAt thủ công vì đã extend BaseTimeEntity
            Question question = Question.builder()
                    .questionText(questionRequest.getQuestionText())
                    .questionType(questionRequest.getQuestionType())
                    .score(questionRequest.getScore())
                    .quiz(quiz)
                    .build();

            Question savedQuestion = questionRepository.save(question);

            // Tạo và liên kết options với question
            List<Option> options = createOptionsForQuestion(savedQuestion, questionRequest.getOptions());
            savedQuestion.setOptions(options);

            questions.add(savedQuestion);
        }
        return questions;
    }

    private List<Option> createOptionsForQuestion(Question question, List<OptionCreateRequest> optionRequests) {
        // Tạo danh sách options (lựa chọn) cho một câu hỏi từ request
        List<Option> options = new ArrayList<>();

        // Kiểm tra nếu có options trong request
        if (optionRequests != null && !optionRequests.isEmpty()) {
            for (OptionCreateRequest optionRequest : optionRequests) {
                // Tạo từng option entity với thông tin từ request
                Option option = Option.builder()
                        .optionText(optionRequest.getOptionText())    // Nội dung lựa chọn
                        .isCorrect(optionRequest.getIsCorrect())      // Đánh dấu đáp án đúng
                        .question(question)                           // Liên kết với câu hỏi
                        .build();
                options.add(option);
            }
            // Lưu tất cả options vào database và trả về
            return optionRepository.saveAll(options);
        }

        return options; // Trả về list rỗng nếu không có options trong request
    }

    private QuizDTO hideCorrectAnswers(QuizDTO quizDTO) {
        if (quizDTO.getQuestions() != null) {
            quizDTO.getQuestions().forEach(question -> {
                if (question.getOptions() != null) {
                    question.getOptions().forEach(option -> option.setIsCorrect(null));
                }
            });
        }
        return quizDTO;
    }

    private QuizGradingResult calculateScore(Quiz quiz, QuizSubmissionRequest request) {
        QuizGradingResult result = new QuizGradingResult();
        List<Question> questions = questionRepository.findWithOptionsByQuizId(quiz.getId());

        result.setTotalQuestions(questions.size());
        result.setTotalPoints(questions.stream().mapToDouble(Question::getScore).sum());

        double totalScore = 0;
        int correctAnswers = 0;
        List<AnswerResult> answerResults = new ArrayList<>();

        for (AnswerRequest answerRequest : request.getAnswers()) {
            AnswerResult answerResult = new AnswerResult();

            Question question = questions.stream().filter(q -> q.getId().equals(answerRequest.getQuestionId())).findFirst().orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + answerRequest.getQuestionId()));

            answerResult.setQuestion(question);
            answerResult.setUserAnswer(serializeUserAnswer(answerRequest));

            boolean isCorrect = checkAnswerCorrect(question, answerRequest);
            answerResult.setCorrect(isCorrect);

            double earnedScore = isCorrect ? question.getScore() : 0;
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
        if (question.getOptions() == null) return false;

        return switch (question.getQuestionType()) {
            case SINGLE_CHOICE -> checkSingleChoice(question, answerRequest.getSelectedOptionId());
            case MULTIPLE_CHOICE -> checkMultipleChoice(question, answerRequest.getSelectedOptionIds());
            case TRUE_FALSE -> checkTrueFalse(question, answerRequest.getTrueFalseAnswer());
            default -> false;
        };
    }

    private boolean checkSingleChoice(Question question, Long selectedOptionId) {
        if (selectedOptionId == null) return false;

        Optional<Option> selectedOption = question.getOptions().stream()
                .filter(option -> option.getId().equals(selectedOptionId))
                .findFirst();

        return selectedOption.isPresent() && selectedOption.get().isCorrect();
    }

    private boolean checkMultipleChoice(Question question, List<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) return false;

        Set<Long> correctOptionIds = question.getOptions().stream()
                .filter(Option::isCorrect)
                .map(Option::getId)
                .collect(Collectors.toSet());

        Set<Long> selectedIds = new HashSet<>(selectedOptionIds);
        return selectedIds.equals(correctOptionIds);
    }

    private boolean checkTrueFalse(Question question, Boolean userAnswer) {
        if (userAnswer == null) return false;

        List<Option> options = question.getOptions();
        if (options.size() >= 2) {
            boolean correctAnswer = options.getFirst().isCorrect();
            return userAnswer == correctAnswer;
        }
        return false;
    }

    private String serializeUserAnswer(AnswerRequest answerRequest) {
        if (answerRequest.getSelectedOptionIds() != null) {
            return "Selected options: " + answerRequest.getSelectedOptionIds();
        } else if (answerRequest.getSelectedOptionId() != null) {
            return "Selected option: " + answerRequest.getSelectedOptionId();
        } else if (answerRequest.getTrueFalseAnswer() != null) {
            return "True/False: " + answerRequest.getTrueFalseAnswer();
        } else if (answerRequest.getEssayAnswer() != null) {
            return "Essay: " + answerRequest.getEssayAnswer();
        }
        return "No answer";
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

            QuizAnswer quizAnswer = QuizAnswer.builder().testResult(testResult).question(answerResult.getQuestion()).userAnswer(answerResult.getUserAnswer()).isCorrect(answerResult.isCorrect()).earnedScore(answerResult.getEarnedScore()).createdAt(LocalDateTime.now()).build();

            quizAnswers.add(quizAnswer);
        }

        quizAnswerRepository.saveAll(quizAnswers);
    }

    private TestResultDTO mapToTestResultDTO(TestResult testResult, QuizGradingResult gradingResult) {
        return TestResultDTO.builder().id(testResult.getId()).score(testResult.getScore()).totalQuestions(gradingResult.getTotalQuestions()).correctAnswers(gradingResult.getCorrectAnswers()).passingScore(testResult.getQuiz().getPassingScore()).isPassed(testResult.getIsPassed()).takenDate(testResult.getTakenDate()).timeSpent(testResult.getTimeSpent()).quizId(testResult.getQuiz().getId()).quizTitle(testResult.getQuiz().getTitle()).userId(testResult.getUser().getId()).username(testResult.getUser().getAccount().getUsername()).build();
    }

    private TestResultDTO mapToTestResultDTO(TestResult testResult, List<QuizAnswer> quizAnswers) {
        int totalQuestions = quizAnswers.size();
        int correctAnswers = (int) quizAnswers.stream().filter(QuizAnswer::getIsCorrect).count();

        return TestResultDTO.builder().id(testResult.getId()).score(testResult.getScore()).totalQuestions(totalQuestions).correctAnswers(correctAnswers).passingScore(testResult.getQuiz().getPassingScore()).isPassed(testResult.getIsPassed()).takenDate(testResult.getTakenDate()).timeSpent(testResult.getTimeSpent()).quizId(testResult.getQuiz().getId()).quizTitle(testResult.getQuiz().getTitle()).userId(testResult.getUser().getId()).username(testResult.getUser().getAccount().getUsername()).answerDetails(quizAnswers.stream().map(this::mapToQuizAnswerDTO).collect(Collectors.toList())).build();
    }

    private QuizAnswerDTO mapToQuizAnswerDTO(QuizAnswer quizAnswer) {
        return QuizAnswerDTO.builder().id(quizAnswer.getId()).questionId(quizAnswer.getQuestion().getId()).questionText(quizAnswer.getQuestion().getQuestionText()).userAnswer(quizAnswer.getUserAnswer()).isCorrect(quizAnswer.getIsCorrect()).earnedScore(quizAnswer.getEarnedScore()).questionScore(quizAnswer.getQuestion().getScore()).correctAnswer(getCorrectAnswer(quizAnswer.getQuestion())).build();
    }

    private String getCorrectAnswer(Question question) {
        if (question.getOptions() == null) return "";

        return question.getOptions().stream()
                .filter(Option::isCorrect)
                .map(Option::getOptionText)
                .collect(Collectors.joining(", "));
    }

    private OptionDTO mapToOptionDTO(Option option) {
        return OptionDTO.builder().id(option.getId()).optionText(option.getOptionText()).isCorrect(option.isCorrect()).build();
    }

    private QuizStatisticsDTO calculateQuizStatistics(Quiz quiz, List<TestResult> results) {
        if (results.isEmpty()) {
            return QuizStatisticsDTO.builder().quizId(quiz.getId()).quizTitle(quiz.getTitle()).totalParticipants(0).passedParticipants(0).averageScore(0.0).passingRate(0.0).lastSubmissionDate(null).build();
        }

        long passedCount = results.stream().filter(result -> result.getScore() >= quiz.getPassingScore()).count();

        double averageScore = results.stream().mapToDouble(TestResult::getScore).average().orElse(0.0);

        LocalDateTime lastSubmission = results.stream().map(TestResult::getTakenDate).max(LocalDateTime::compareTo).orElse(null);

        return QuizStatisticsDTO.builder().quizId(quiz.getId()).quizTitle(quiz.getTitle()).totalParticipants(results.size()).passedParticipants((int) passedCount).averageScore(Math.round(averageScore * 100.0) / 100.0).passingRate(Math.round((passedCount * 100.0 / results.size()) * 100.0) / 100.0).lastSubmissionDate(lastSubmission != null ? lastSubmission.toString() : null).build();
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