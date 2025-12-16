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
    private final CloudinaryService cloudinaryService;

    // ==================== QUIZ QUẢN LÝ (ADMIN) ====================

    @Override
    @Transactional
    public QuizDTO createQuiz(QuizCreateRequest request) {
        log.info("Tạo quiz mới cho section ID: {}", request.getSectionId());

        // Tìm section
        Section section = sectionRepository.findById(request.getSectionId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy section với ID: " + request.getSectionId()));

        // Tạo quiz mới
        Quiz quiz = Quiz.builder().title(request.getTitle()).description(request.getDescription()).timeLimit(request.getTimeLimit()).passingScore(request.getPassingScore()).isPublished(request.getIsPublished()).isActive(true) // Mặc định là active
                .section(section).build();

        Quiz savedQuiz = quizRepository.save(quiz);

        // Tạo câu hỏi nếu có trong request
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<Question> questions = createQuestionsForQuiz(savedQuiz, request.getQuestions());
            savedQuiz.setQuestions(questions);
        }

        log.info("Tạo quiz thành công cho section {} với ID: {}", request.getSectionId(), savedQuiz.getId());
        return QuizAdminMapper.toQuizDTO(savedQuiz);
    }

    @Override
    @Transactional
    public QuizDTO updateQuiz(Long quizId, QuizUpdateRequest request) {
        log.info("Cập nhật quiz ID: {}", quizId);

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        // Cập nhật thông tin
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setIsPublished(request.getIsPublished());

        // Giữ isActive nếu không cung cấp
        if (request.getIsActive() != null) {
            quiz.setIsActive(request.getIsActive());
        }

        Quiz updatedQuiz = quizRepository.save(quiz);
        log.info("Cập nhật quiz thành công: {}", quizId);

        return QuizAdminMapper.toQuizDTO(updatedQuiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        // ============ THÊM PHẦN XÓA ẢNH CỦA TẤT CẢ CÂU HỎI ============
        // Lấy tất cả câu hỏi của quiz
        List<Question> questions = questionRepository.findByQuizId(quizId);

        for (Question question : questions) {
            // Xóa ảnh câu hỏi
            if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteFile(question.getImageUrl());
                    log.info("Đã xóa ảnh câu hỏi: {}", question.getImageUrl());
                } catch (Exception e) {
                    log.warn("Không thể xóa ảnh câu hỏi: {}", question.getImageUrl(), e);
                }
            }

            // Xóa ảnh của các options
            if (question.getOptions() != null) {
                for (Option option : question.getOptions()) {
                    if (option.getImageUrl() != null && !option.getImageUrl().isEmpty()) {
                        try {
                            cloudinaryService.deleteFile(option.getImageUrl());
                            log.info("Đã xóa ảnh option: {}", option.getImageUrl());
                        } catch (Exception e) {
                            log.warn("Không thể xóa ảnh option: {}", option.getImageUrl(), e);
                        }
                    }
                }
            }
        }
        // ============================================================

        // Xóa tất cả câu hỏi trước
        questionRepository.deleteByQuizId(quizId);

        // Xóa quiz
        quizRepository.delete(quiz);

        log.info("Xóa quiz thành công: {}", quizId);
    }

    @Override
    @Transactional
    public void publishQuiz(Long quizId, boolean publish) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        quiz.setIsPublished(publish);
        quizRepository.save(quiz);

        log.info("Quiz {} {}", quizId, publish ? "đã bật hiển thị" : "đã tắt hiển thị");
    }

    // ==================== XEM QUIZ ====================

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizForTeacher(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        return QuizAdminMapper.toQuizDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizForStudent(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        // Kiểm tra quiz có khả dụng không
        if (!quiz.getIsPublished() || !quiz.getIsActive()) {
            throw new IllegalArgumentException("Quiz không khả dụng để làm bài");
        }

        return QuizStudentMapper.toQuizDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDTO> getAvailableQuizzesForStudent(Long sectionId, Long userId) {
        log.info("Tìm quiz cho học sinh - sectionId: {}, userId: {}", sectionId, userId);

        // Lấy tất cả quiz trong section
        List<Quiz> quizzes = quizRepository.findBySectionId(sectionId);
        log.info("Tìm thấy {} quiz trong database", quizzes.size());

        // Lọc chỉ lấy quiz đã publish và active
        List<QuizSummaryDTO> result = quizzes.stream().filter(quiz -> Boolean.TRUE.equals(quiz.getIsPublished()) && Boolean.TRUE.equals(quiz.getIsActive())).map(QuizStudentMapper::toSummaryDTO).collect(Collectors.toList());

        log.info("Trả về {} quiz khả dụng sau khi lọc", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizStatusDTO getQuizStatusForStudent(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        // Lấy lịch sử làm bài của user
        List<TestResult> userResults = testResultRepository.findByUserIdAndQuizIdOrderByTakenDateDesc(userId, quizId);

        // Tạo DTO trạng thái
        QuizStatusDTO status = QuizStudentMapper.toStatusDTO(quiz);

        if (!userResults.isEmpty()) {
            TestResult latestResult = userResults.get(0);
            status.setIsCompleted(true);
            status.setAttemptCount(userResults.size());
            status.setBestScore(latestResult.getScore());
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

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDTO> getQuizzesBySectionId(Long sectionId) {
        List<Quiz> quizzes = quizRepository.findBySectionId(sectionId);
        return QuizAdminMapper.toSummaryDTOs(quizzes);
    }

    // ==================== QUẢN LÝ CÂU HỎI ====================

    @Override
    @Transactional
    public QuestionDTO addQuestionToQuiz(Long quizId, QuestionCreateRequest request) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        // Xác định thứ tự câu hỏi
        Integer orderIndex = request.getOrderIndex();
        if (orderIndex == null) {
            Integer maxOrder = questionRepository.findMaxOrderIndexByQuizId(quizId);
            orderIndex = maxOrder != null ? maxOrder + 1 : 1;
        }

        // ============ THÊM VALIDATION CHO ẢNH ============
        // Kiểm tra URL ảnh hợp lệ (nếu có)
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            validateImageUrl(request.getImageUrl(), "imageUrl của câu hỏi");
        }
        // ===============================================

        // Tạo câu hỏi
        Question question = Question.builder().questionText(request.getQuestionText()).questionType(request.getQuestionType()).score(request.getScore()).orderIndex(orderIndex).imageUrl(request.getImageUrl()) // URL từ frontend upload
                .explanation(request.getExplanation()).quiz(quiz).build();

        Question savedQuestion = questionRepository.save(question);

        // Tạo options nếu có
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            // ============ THÊM VALIDATION CHO ẢNH OPTIONS ============
            for (OptionCreateRequest optionRequest : request.getOptions()) {
                if (optionRequest.getImageUrl() != null && !optionRequest.getImageUrl().isEmpty()) {
                    validateImageUrl(optionRequest.getImageUrl(), "imageUrl của đáp án");
                }
            }
            // ========================================================

            List<Option> options = createOptionsForQuestion(savedQuestion, request.getOptions());
            savedQuestion.setOptions(options);
        }

        log.info("Đã thêm câu hỏi vào quiz {}: {}", quizId, savedQuestion.getId());
        return QuestionMapper.toDTOForTeacher(savedQuestion);
    }

    @Override
    @Transactional
    public QuestionDTO updateQuestion(Long questionId, QuestionUpdateRequest request) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + questionId));

        // ============ THÊM PHẦN XỬ LÝ ẢNH ============
        String oldImageUrl = question.getImageUrl();
        String newImageUrl = request.getImageUrl();

        // Xóa ảnh cũ nếu có thay đổi và ảnh cũ tồn tại
        // BUG FIX: Chỉ xóa nếu newImageUrl null HOẶC khác oldImageUrl, VÀ oldImageUrl không null
        if (oldImageUrl != null && (newImageUrl == null || !oldImageUrl.equals(newImageUrl))) {
            try {
                cloudinaryService.deleteFile(oldImageUrl);
                log.info("Đã xóa ảnh cũ của câu hỏi: {}", oldImageUrl);
            } catch (Exception e) {
                log.warn("Không thể xóa ảnh cũ: {}", oldImageUrl, e);
                // Không throw exception để tránh ảnh hưởng đến update
            }
        }
        // ============================================

        // ============ THÊM VALIDATION CHO ẢNH MỚI ============
        if (newImageUrl != null && !newImageUrl.isEmpty()) {
            validateImageUrl(newImageUrl, "imageUrl của câu hỏi");
        }
        // ===================================================

        // Cập nhật thông tin cơ bản
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setScore(request.getScore());
        question.setOrderIndex(request.getOrderIndex());
        question.setImageUrl(request.getImageUrl()); // URL từ upload service
        question.setExplanation(request.getExplanation());

        // Lưu câu hỏi đã được cập nhật trước
        Question updatedQuestion = questionRepository.save(question);

        // ============ THÊM XỬ LÝ XÓA ẢNH CỦA OPTIONS CŨ ============
        // Lấy options cũ trước khi xóa (cần lấy trước khi deleteByQuestionId)
        List<Option> oldOptions = optionRepository.findByQuestionId(questionId);

        // Xóa ảnh của tất cả options cũ
        for (Option oldOption : oldOptions) {
            if (oldOption.getImageUrl() != null && !oldOption.getImageUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteFile(oldOption.getImageUrl());
                    log.info("Đã xóa ảnh cũ của option: {}", oldOption.getImageUrl());
                } catch (Exception e) {
                    log.warn("Không thể xóa ảnh option cũ: {}", oldOption.getImageUrl(), e);
                }
            }
        }
        // =========================================================

        // Xóa options cũ từ database
        optionRepository.deleteByQuestionId(questionId);

        // Tạo options mới nếu có
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            // Validate options theo loại câu hỏi
            validateQuestionOptionsBasedOnType(request.getQuestionType(), request.getOptions().stream().map(opt -> {
                OptionCreateRequest createRequest = new OptionCreateRequest();
                createRequest.setOptionText(opt.getOptionText());
                createRequest.setIsCorrect(opt.getIsCorrect());
                createRequest.setImageUrl(opt.getImageUrl());
                return createRequest;
            }).collect(Collectors.toList()));

            // Với FILL_IN_BLANK, tất cả option đều là đáp án đúng
            if (request.getQuestionType() == QuestionType.FILL_IN_BLANK) {
                request.getOptions().forEach(opt -> opt.setIsCorrect(true));
            }

            // ============ THÊM VALIDATION CHO ẢNH OPTIONS MỚI ============
            for (OptionUpdateRequest optionRequest : request.getOptions()) {
                if (optionRequest.getImageUrl() != null && !optionRequest.getImageUrl().isEmpty()) {
                    validateImageUrl(optionRequest.getImageUrl(), "imageUrl của đáp án");
                }
            }
            // ============================================================

            List<Option> newOptions = createOptionsForQuestion(updatedQuestion, request.getOptions());
            updatedQuestion.setOptions(newOptions);
        }

        log.info("Cập nhật câu hỏi thành công: {}", questionId);

        return QuestionMapper.toDTOForTeacher(updatedQuestion);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        // Tìm câu hỏi trước
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + questionId));

        // ============ THÊM PHẦN XÓA ẢNH ============
        // Xóa ảnh câu hỏi nếu có
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            try {
                cloudinaryService.deleteFile(question.getImageUrl());
                log.info("Đã xóa ảnh câu hỏi: {}", question.getImageUrl());
            } catch (Exception e) {
                log.warn("Không thể xóa ảnh câu hỏi: {}", question.getImageUrl(), e);
            }
        }

        // Xóa ảnh của tất cả options
        if (question.getOptions() != null) {
            for (Option option : question.getOptions()) {
                if (option.getImageUrl() != null && !option.getImageUrl().isEmpty()) {
                    try {
                        cloudinaryService.deleteFile(option.getImageUrl());
                        log.info("Đã xóa ảnh option: {}", option.getImageUrl());
                    } catch (Exception e) {
                        log.warn("Không thể xóa ảnh option: {}", option.getImageUrl(), e);
                    }
                }
            }
        }
        // ============================================

        // Xóa câu hỏi từ database
        questionRepository.deleteById(questionId);
        log.info("Xóa câu hỏi thành công: {}", questionId);
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

    @Override
    @Transactional
    public OptionDTO addOptionToQuestion(Long questionId, OptionCreateRequest request) {
        log.info("Thêm đáp án vào câu hỏi ID: {}", questionId);

        Question question = questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + questionId));

        // ============ THÊM VALIDATION CHO ẢNH ============
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            validateImageUrl(request.getImageUrl(), "imageUrl của đáp án");
        }
        // ===============================================

        // Với FILL_IN_BLANK, đáp án phải luôn là đúng
        if (question.getQuestionType() == QuestionType.FILL_IN_BLANK && !request.getIsCorrect()) {
            throw new IllegalArgumentException("Câu hỏi FILL_IN_BLANK chỉ có đáp án đúng, không có đáp án sai");
        }

        // Kiểm tra số lượng option hiện có
        List<Option> existingOptions = optionRepository.findByQuestionId(questionId);
        if (question.getQuestionType() == QuestionType.TRUE_FALSE && existingOptions.size() >= 2) {
            throw new IllegalArgumentException("Câu hỏi TRUE/FALSE chỉ được có tối đa 2 đáp án");
        }

        // Xác định thứ tự
        Integer maxOrder = optionRepository.findMaxOrderIndexByQuestionId(questionId);
        Integer orderIndex = maxOrder != null ? maxOrder + 1 : 1;

        Option option = Option.builder().optionText(request.getOptionText()).isCorrect(request.getIsCorrect()).imageUrl(request.getImageUrl()) // URL từ frontend upload
                .orderIndex(orderIndex).question(question).build();

        Option savedOption = optionRepository.save(option);
        log.info("Thêm đáp án thành công vào câu hỏi {}: {}", questionId, savedOption.getId());

        return OptionMapper.toDTO(savedOption);
    }

    @Override
    @Transactional
    public OptionDTO updateOption(Long optionId, OptionUpdateRequest request) {
        log.info("Cập nhật đáp án ID: {}", optionId);

        Option option = optionRepository.findById(optionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đáp án với ID: " + optionId));

        // ============ THÊM PHẦN XỬ LÝ ẢNH ============
        String oldImageUrl = option.getImageUrl();
        String newImageUrl = request.getImageUrl();

        // Xóa ảnh cũ nếu có thay đổi và ảnh cũ tồn tại
        if (oldImageUrl != null && (newImageUrl == null || !oldImageUrl.equals(newImageUrl))) {
            try {
                cloudinaryService.deleteFile(oldImageUrl);
                log.info("Đã xóa ảnh cũ của option: {}", oldImageUrl);
            } catch (Exception e) {
                log.warn("Không thể xóa ảnh option cũ: {}", oldImageUrl, e);
            }
        }
        // ============================================

        // ============ THÊM VALIDATION CHO ẢNH MỚI ============
        if (newImageUrl != null && !newImageUrl.isEmpty()) {
            validateImageUrl(newImageUrl, "imageUrl của đáp án");
        }
        // ===================================================

        // Với FILL_IN_BLANK, không cho phép chuyển thành đáp án sai
        if (option.getQuestion().getQuestionType() == QuestionType.FILL_IN_BLANK && !request.getIsCorrect()) {
            throw new IllegalArgumentException("Câu hỏi FILL_IN_BLANK chỉ có đáp án đúng, không có đáp án sai");
        }

        option.setOptionText(request.getOptionText());
        option.setIsCorrect(request.getIsCorrect());
        option.setImageUrl(request.getImageUrl());

        Option updatedOption = optionRepository.save(option);
        log.info("Cập nhật đáp án thành công: {}", optionId);

        return OptionMapper.toDTO(updatedOption);
    }

    @Override
    @Transactional
    public void deleteOption(Long optionId) {
        // Tìm option trước
        Option option = optionRepository.findById(optionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đáp án với ID: " + optionId));

        // ============ THÊM PHẦN XÓA ẢNH ============
        // Xóa ảnh option nếu có
        if (option.getImageUrl() != null && !option.getImageUrl().isEmpty()) {
            try {
                cloudinaryService.deleteFile(option.getImageUrl());
                log.info("Đã xóa ảnh option: {}", option.getImageUrl());
            } catch (Exception e) {
                log.warn("Không thể xóa ảnh option: {}", option.getImageUrl(), e);
            }
        }
        // ============================================

        // Với TRUE_FALSE, đảm bảo còn ít nhất 2 đáp án
        if (option.getQuestion().getQuestionType() == QuestionType.TRUE_FALSE) {
            long remainingOptions = optionRepository.countByQuestionId(option.getQuestion().getId()) - 1;
            if (remainingOptions < 2) {
                throw new IllegalArgumentException("Câu hỏi TRUE/FALSE phải có ít nhất 2 đáp án");
            }
        }

        optionRepository.deleteById(optionId);
        log.info("Xóa đáp án thành công: {}", optionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionDTO> getOptionsByQuestionId(Long questionId) {
        List<Option> options = optionRepository.findByQuestionId(questionId);
        return OptionMapper.toDTOs(options);
    }

    // ==================== LÀM BÀI THI ====================

    @Override
    @Transactional
    public TestResultDTO startQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));

        // Kiểm tra quiz có khả dụng không
        if (!quiz.getIsPublished() || !quiz.getIsActive()) {
            throw new IllegalArgumentException("Quiz không khả dụng");
        }

        // ============ VẤN ĐỀ: KHÔNG KIỂM TRA XEM ĐÃ CÓ TEST RESULT CHƯA HOÀN THÀNH ============
        List<TestResult> existingResults = testResultRepository.findByUserIdAndQuizIdOrderByTakenDateDesc(userId, quizId);

        // Nếu đã có test result chưa hoàn thành (chưa có score), trả về result đó
        for (TestResult result : existingResults) {
            if (result.getScore() == null || result.getScore() == 0) {
                log.info("Tìm thấy test result đang làm dở: {}", result.getId());
                return TestResultMapper.toDTO(result);
            }
        }

        // Tạo TestResult mới
        TestResult testResult = TestResult.builder().quiz(quiz).user(user).takenDate(LocalDateTime.now()).build();

        TestResult savedResult = testResultRepository.save(testResult);
        log.info("Bắt đầu quiz cho user {}: result ID {}", userId, savedResult.getId());

        return TestResultMapper.toDTO(savedResult);
    }

    @Override
    @Transactional
    public TestResultDTO submitQuiz(Long quizId, QuizSubmissionRequest request, Long userId) {
        log.info("Nộp bài quiz {} cho user: {}", quizId, userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        // Kiểm tra quiz có khả dụng không
        if (!quiz.getIsPublished() || !quiz.getIsActive()) {
            throw new IllegalArgumentException("Quiz không khả dụng");
        }

        // ============ VẤN ĐỀ 1: KHÔNG KIỂM TRA TEST RESULT ĐANG TỒN TẠI ============
        // Nên kiểm tra xem user đã có test result nào đang IN_PROGRESS cho quiz này không
        List<TestResult> existingResults = testResultRepository.findByUserIdAndQuizId(userId, quizId);

        // Nếu có test result đang IN_PROGRESS, sử dụng nó thay vì tạo mới
        TestResult testResultToUse = null;

        for (TestResult result : existingResults) {
            // Kiểm tra nếu result chưa có score (đang làm dở)
            if (result.getScore() == null || result.getScore() == 0) {
                testResultToUse = result;
                log.info("Tìm thấy test result đang làm dở: {}", result.getId());
                break;
            }
        }

        // Tính điểm
        QuizGradingResult gradingResult = calculateScore(quiz, request);

        // Lưu kết quả
        TestResult testResult = null;
        if (testResultToUse != null) {
            // Cập nhật test result hiện có
            testResult = updateTestResult(testResultToUse, gradingResult, request.getTimeSpentInSeconds());
            log.info("Cập nhật test result hiện có: {}", testResult.getId());
        } else {
            // Tạo test result mới
            testResult = saveTestResult(quiz, user, gradingResult, request.getTimeSpentInSeconds());
            log.info("Tạo test result mới: {}", testResult.getId());
        }

        // Lưu chi tiết các câu trả lời
        saveQuizAnswers(testResult, request.getAnswers(), gradingResult);

        log.info("Nộp bài thành công. Điểm: {}/{} ({}%)", gradingResult.getEarnedPoints(), gradingResult.getTotalPoints(), gradingResult.getTotalPoints() > 0 ? (gradingResult.getEarnedPoints() / gradingResult.getTotalPoints()) * 100 : 0);

        return TestResultMapper.toDTO(testResult);
    }

    private TestResult updateTestResult(TestResult testResult, QuizGradingResult gradingResult, Long timeSpent) {
        double totalPoints = gradingResult.getTotalPoints();
        double earnedPoints = gradingResult.getEarnedPoints();
        double scorePercentage = totalPoints > 0 ? (earnedPoints / totalPoints) * 100 : 0;

        // Đảm bảo timeSpent không null
        Long actualTimeSpent = timeSpent != null ? timeSpent : 0L;

        // Cập nhật thông tin
        testResult.setScore(scorePercentage);
        testResult.setTotalPoints(totalPoints);
        testResult.setEarnedPoints(earnedPoints);
        testResult.setCorrectAnswers(gradingResult.getCorrectAnswers());
        testResult.setTotalQuestions(gradingResult.getTotalQuestions());
        testResult.setTimeSpent(actualTimeSpent);
        testResult.setTakenDate(LocalDateTime.now()); // Cập nhật thời gian nộp
        testResult.setIsPassed(scorePercentage >= testResult.getQuiz().getPassingScore());

        return testResultRepository.save(testResult);
    }

    // ==================== KẾT QUẢ ====================

    @Override
    @Transactional(readOnly = true)
    public TestResultDTO getQuizResult(Long resultId) {
        TestResult testResult = testResultRepository.findById(resultId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả với ID: " + resultId));

        return TestResultMapper.toDTO(testResult);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResultDetailDTO getQuizResultForStudent(Long resultId, Long userId) {
        TestResult testResult = testResultRepository.findById(resultId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả với ID: " + resultId));

        // Kiểm tra quyền xem
        if (!testResult.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xem kết quả này");
        }

        // Lấy chi tiết các câu trả lời
        List<QuizAnswer> quizAnswers = quizAnswerRepository.findByTestResultId(resultId);

        TestResultDTO testResultDTO = TestResultMapper.toDTO(testResult);

        List<AnswerResultDTO> answerDetails = quizAnswers.stream().map(QuizAnswerMapper::toAnswerResultDTOForStudent).collect(Collectors.toList());

        testResultDTO.setAnswerDetails(answerDetails);

        // Thông tin quiz
        QuizBasicInfoDTO quizInfo = QuizStudentMapper.toBasicInfoDTO(testResult.getQuiz());

        return QuizResultDetailDTO.builder().summary(testResultDTO).answerDetails(answerDetails).quizInfo(quizInfo).build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResultDetailDTO getQuizResultForTeacher(Long resultId) {
        TestResult testResult = testResultRepository.findById(resultId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả với ID: " + resultId));

        // Lấy chi tiết các câu trả lời
        List<QuizAnswer> quizAnswers = quizAnswerRepository.findByTestResultId(resultId);

        TestResultDTO testResultDTO = TestResultMapper.toDTO(testResult);

        List<AnswerResultDTO> answerDetails = quizAnswers.stream().map(QuizAnswerMapper::toAnswerResultDTO).collect(Collectors.toList());

        testResultDTO.setAnswerDetails(answerDetails);

        // Thông tin quiz
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
    public List<TestResultDTO> getQuizResults(Long quizId) {
        List<TestResult> results = testResultRepository.findByQuizIdOrderByScoreDesc(quizId);
        return results.stream().map(TestResultMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuizStatisticsDTO getQuizStatistics(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quiz với ID: " + quizId));

        List<TestResult> results = testResultRepository.findByQuizIdOrderByScoreDesc(quizId);

        // Nếu không có kết quả
        if (results.isEmpty()) {
            return QuizStatisticsDTO.builder().quizId(quiz.getId()).quizTitle(quiz.getTitle()).totalAttempts(0).passedAttempts(0).averageScore(0.0).highestScore(0.0).lowestScore(0.0).averageTimeSpent(0.0).build();
        }

        // Tính toán thống kê
        long passedCount = results.stream().filter(result -> result.getScore() >= quiz.getPassingScore()).count();

        double averageScore = results.stream().mapToDouble(TestResult::getScore).average().orElse(0.0);

        double highestScore = results.stream().mapToDouble(TestResult::getScore).max().orElse(0.0);

        double lowestScore = results.stream().mapToDouble(TestResult::getScore).min().orElse(0.0);

        double averageTimeSpent = results.stream().mapToLong(TestResult::getTimeSpent).average().orElse(0.0);

        return QuizStatisticsDTO.builder().quizId(quiz.getId()).quizTitle(quiz.getTitle()).totalAttempts(results.size()).passedAttempts((int) passedCount).averageScore(Math.round(averageScore * 100.0) / 100.0).highestScore(Math.round(highestScore * 100.0) / 100.0).lowestScore(Math.round(lowestScore * 100.0) / 100.0).averageTimeSpent(Math.round(averageTimeSpent * 100.0) / 100.0).build();
    }

    // ==================== KIỂM TRA ====================

    @Override
    public boolean existsQuiz(Long quizId) {
        return quizRepository.existsById(quizId);
    }

    @Override
    public boolean existsAnyQuizBySectionId(Long sectionId) {
        return quizRepository.existsBySectionId(sectionId);
    }

    @Override
    public boolean canUserAccessQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return false;

        return quiz.getIsPublished() && quiz.getIsActive();
    }

    // ==================== PHƯƠNG THỨC HỖ TRỢ RIÊNG ====================

    /**
     * Tạo câu hỏi cho quiz
     */
    private List<Question> createQuestionsForQuiz(Quiz quiz, List<QuestionCreateRequest> questionRequests) {
        List<Question> questions = new ArrayList<>();

        for (QuestionCreateRequest questionRequest : questionRequests) {
            // ============ THÊM VALIDATION CHO ẢNH ============
            if (questionRequest.getImageUrl() != null && !questionRequest.getImageUrl().isEmpty()) {
                validateImageUrl(questionRequest.getImageUrl(), "imageUrl của câu hỏi");
            }
            // ===============================================

            // Validate options trước khi tạo
            if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
                // ============ THÊM VALIDATION CHO ẢNH OPTIONS ============
                for (OptionCreateRequest option : questionRequest.getOptions()) {
                    if (option.getImageUrl() != null && !option.getImageUrl().isEmpty()) {
                        validateImageUrl(option.getImageUrl(), "imageUrl của đáp án");
                    }
                }
                // ========================================================

                validateQuestionOptionsBasedOnType(questionRequest.getQuestionType(), questionRequest.getOptions());

                // Với FILL_IN_BLANK, tất cả option đều là đáp án đúng
                if (questionRequest.getQuestionType() == QuestionType.FILL_IN_BLANK) {
                    questionRequest.getOptions().forEach(option -> option.setIsCorrect(true));
                }
            }

            // Tạo câu hỏi
            Question question = Question.builder().questionText(questionRequest.getQuestionText()).questionType(questionRequest.getQuestionType()).score(questionRequest.getScore()).orderIndex(questionRequest.getOrderIndex()).imageUrl(questionRequest.getImageUrl()).explanation(questionRequest.getExplanation()).quiz(quiz).build();

            Question savedQuestion = questionRepository.save(question);

            // Tạo options nếu có
            if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
                List<Option> options = createOptionsForQuestion(savedQuestion, questionRequest.getOptions());
                savedQuestion.setOptions(options);
            }

            questions.add(savedQuestion);
        }
        return questions;
    }

    /**
     * Validate options theo loại câu hỏi
     */
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
                // FILL_IN_BLANK phải có ít nhất 1 option
                if (options.isEmpty()) {
                    throw new IllegalArgumentException("Câu hỏi FILL_IN_BLANK phải có ít nhất 1 đáp án");
                }
                // Tất cả option phải là đáp án đúng
                for (OptionCreateRequest option : options) {
                    if (!option.getIsCorrect()) {
                        throw new IllegalArgumentException("Câu hỏi FILL_IN_BLANK chỉ có đáp án đúng, không có đáp án sai");
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Loại câu hỏi không được hỗ trợ: " + questionType);
        }
    }

    /**
     * Tạo options cho câu hỏi
     */
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

    /**
     * Validate image URL format
     */
    private void validateImageUrl(String imageUrl, String fieldName) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                throw new IllegalArgumentException(fieldName + " phải là URL hợp lệ (bắt đầu với http:// hoặc https://)");
            }

            // Kiểm tra định dạng ảnh
            String lowerUrl = imageUrl.toLowerCase();
            if (!lowerUrl.matches(".*\\.(jpg|jpeg|png|gif|webp|bmp)(\\?.*)?$")) {
                throw new IllegalArgumentException(fieldName + " chỉ chấp nhận các định dạng: JPG, JPEG, PNG, GIF, WEBP, BMP");
            }
        }
    }

    /**
     * Tính điểm cho bài làm
     */
    private QuizGradingResult calculateScore(Quiz quiz, QuizSubmissionRequest request) {
        QuizGradingResult result = new QuizGradingResult();

        // Lấy tất cả câu hỏi của quiz
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());

        // Tổng điểm và số câu hỏi
        result.setTotalQuestions(questions.size());
        result.setTotalPoints(questions.stream().mapToDouble(Question::getScore).sum());

        double totalScore = 0;
        int correctAnswers = 0;
        List<AnswerResult> answerResults = new ArrayList<>();

        // Tạo map để tìm câu hỏi nhanh
        Map<Long, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        // Xử lý từng câu trả lời
        for (AnswerRequest answerRequest : request.getAnswers()) {
            AnswerResult answerResult = new AnswerResult();

            Question question = questionMap.get(answerRequest.getQuestionId());
            if (question == null) {
                log.warn("Không tìm thấy câu hỏi với ID: {}", answerRequest.getQuestionId());
                continue;
            }

            // Lưu câu trả lời của user
            answerResult.setQuestion(question);
            answerResult.setUserAnswer(serializeUserAnswer(answerRequest));

            // Kiểm tra đáp án đúng/sai
            boolean isCorrect = checkAnswerCorrect(question, answerRequest);
            answerResult.setCorrect(isCorrect);

            // Tính điểm
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

    /**
     * Kiểm tra đáp án có đúng không
     */
    private boolean checkAnswerCorrect(Question question, AnswerRequest answerRequest) {
        switch (question.getQuestionType()) {
            case SINGLE_CHOICE:
                return checkSingleChoice(question, answerRequest.getSelectedOptionId());
            case MULTIPLE_CHOICE:
                return checkMultipleChoice(question, answerRequest.getSelectedOptionIds());
            case TRUE_FALSE:
                return checkTrueFalse(question, answerRequest.getSelectedOptionId());
            case FILL_IN_BLANK:
                return checkFillInBlank(question, answerRequest.getFillInBlankAnswer());
            default:
                return false;
        }
    }

    /**
     * Kiểm tra SINGLE_CHOICE
     */
    private boolean checkSingleChoice(Question question, Long selectedOptionId) {
        if (selectedOptionId == null || question.getOptions() == null || question.getOptions().isEmpty()) {
            return false;
        }

        return question.getOptions().stream().filter(option -> option.getId().equals(selectedOptionId)).findFirst().map(Option::getIsCorrect).orElse(false);
    }

    /**
     * Kiểm tra MULTIPLE_CHOICE
     */
    private boolean checkMultipleChoice(Question question, List<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty() || question.getOptions() == null || question.getOptions().isEmpty()) {
            return false;
        }

        // Lấy ID của tất cả đáp án đúng
        Set<Long> correctOptionIds = question.getOptions().stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).map(Option::getId).collect(Collectors.toSet());

        // Lấy ID của tất cả đáp án sai
        Set<Long> incorrectOptionIds = question.getOptions().stream().filter(opt -> !Boolean.TRUE.equals(opt.getIsCorrect())).map(Option::getId).collect(Collectors.toSet());

        // Chuyển selected IDs sang Set
        Set<Long> selectedIds = new HashSet<>(selectedOptionIds);

        // Điều kiện: chọn tất cả đáp án đúng và không chọn đáp án sai nào
        boolean hasAllCorrect = selectedIds.containsAll(correctOptionIds);
        boolean hasNoIncorrect = Collections.disjoint(selectedIds, incorrectOptionIds);

        return hasAllCorrect && hasNoIncorrect;
    }

    /**
     * Kiểm tra TRUE_FALSE
     */
    private boolean checkTrueFalse(Question question, Long selectedOptionId) {
        if (selectedOptionId == null || question.getOptions() == null || question.getOptions().isEmpty()) {
            return false;
        }

        return question.getOptions().stream().filter(option -> option.getId().equals(selectedOptionId)).findFirst().map(Option::getIsCorrect).orElse(false);
    }

    /**
     * Kiểm tra FILL_IN_BLANK
     */
    private boolean checkFillInBlank(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty() || question.getOptions() == null || question.getOptions().isEmpty()) {
            return false;
        }

        String normalizedUserAnswer = userAnswer.trim().toLowerCase();

        // Kiểm tra xem câu trả lời có khớp với bất kỳ đáp án đúng nào không
        return question.getOptions().stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).map(Option::getOptionText).filter(Objects::nonNull).map(String::trim).map(String::toLowerCase).anyMatch(correctAnswer -> correctAnswer.equals(normalizedUserAnswer));
    }

    /**
     * Chuyển câu trả lời thành string để lưu
     */
    private String serializeUserAnswer(AnswerRequest answerRequest) {
        if (answerRequest.getSelectedOptionIds() != null && !answerRequest.getSelectedOptionIds().isEmpty()) {
            return "Multiple Choice: " + String.join(",", answerRequest.getSelectedOptionIds().stream().map(String::valueOf).collect(Collectors.toList()));
        } else if (answerRequest.getSelectedOptionId() != null) {
            return "Single Choice: " + answerRequest.getSelectedOptionId();
        } else if (answerRequest.getFillInBlankAnswer() != null) {
            return "Fill in blank: " + answerRequest.getFillInBlankAnswer();
        }
        return "No answer";
    }

    /**
     * Lưu kết quả bài làm
     */
    private TestResult saveTestResult(Quiz quiz, User user, QuizGradingResult gradingResult, Long timeSpent) {
        double totalPoints = gradingResult.getTotalPoints();
        double earnedPoints = gradingResult.getEarnedPoints();
        double scorePercentage = totalPoints > 0 ? (earnedPoints / totalPoints) * 100 : 0;

        // Đảm bảo timeSpent không null
        Long actualTimeSpent = timeSpent != null ? timeSpent : 0L;

        TestResult testResult = TestResult.builder().quiz(quiz).user(user).score(scorePercentage).totalPoints(totalPoints).earnedPoints(earnedPoints).correctAnswers(gradingResult.getCorrectAnswers()).totalQuestions(gradingResult.getTotalQuestions()).timeSpent(actualTimeSpent).takenDate(LocalDateTime.now()).isPassed(scorePercentage >= quiz.getPassingScore()).build();

        return testResultRepository.save(testResult);
    }

    /**
     * Lưu chi tiết các câu trả lời
     */
    private void saveQuizAnswers(TestResult testResult, List<AnswerRequest> answers, QuizGradingResult gradingResult) {
        List<QuizAnswer> quizAnswers = new ArrayList<>();

        // Tạo map để tra cứu nhanh
        Map<Long, AnswerResult> answerResultMap = gradingResult.getAnswerResults().stream().collect(Collectors.toMap(ar -> ar.getQuestion().getId(), ar -> ar));

        for (AnswerRequest answerRequest : answers) {
            AnswerResult answerResult = answerResultMap.get(answerRequest.getQuestionId());
            if (answerResult == null) {
                log.warn("Không tìm thấy kết quả cho câu hỏi: {}", answerRequest.getQuestionId());
                continue;
            }

            QuizAnswer quizAnswer = QuizAnswer.builder().testResult(testResult).question(answerResult.getQuestion()).userAnswer(answerResult.getUserAnswer()).isCorrect(answerResult.isCorrect()).earnedScore(answerResult.getEarnedScore()).createdAt(LocalDateTime.now()).build();

            quizAnswers.add(quizAnswer);
        }

        if (!quizAnswers.isEmpty()) {
            quizAnswerRepository.saveAll(quizAnswers);
            log.info("Đã lưu {} câu trả lời cho kết quả test {}", quizAnswers.size(), testResult.getId());
        }
    }

    // ==================== LỚP HỖ TRỢ NỘI BỘ ====================

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