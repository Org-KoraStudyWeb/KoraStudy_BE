package korastudy.be.service.impl;

import korastudy.be.dto.request.Exam.SubmitAnswerRequest;
import korastudy.be.dto.request.Exam.SubmitExamRequest;
import korastudy.be.dto.response.Exam.*;
import korastudy.be.entity.MockTest.*;
import korastudy.be.entity.User.User;
import korastudy.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final MockTestRepository mockTestRepo;
    private final MockTestPartRepository partRepo;
    private final MockTestQuestionRepository questionRepo;
    private final MockTestAnswersRepository answersRepo;
    private final ComprehensiveTestResultRepository resultRepo;
    private final UserRepository userRepo;
    private final PracticeTestResultRepository practiceResultRepo;
    private final ExamCommentRepository commentRepo;
    private final UserStudyActivityService studyActivityService;
    private final ReviewRepository reviewRepo;

    public List<ExamListItemResponse> getAllExams() {
        // Chỉ lấy các bài thi đang active cho user
        List<MockTest> tests = mockTestRepo.findByIsActiveTrue();
        List<ExamListItemResponse> dtos = new ArrayList<>();
        for (MockTest t : tests) {
            ExamListItemResponse dto = new ExamListItemResponse();
            dto.setId(t.getId()); // MockTest có trường id
            dto.setTitle(t.getTitle());
            dto.setDescription(t.getDescription());
            dto.setLevel(t.getLevel());
            dto.setTotalQuestions(t.getTotalQuestions());
            dto.setTotalPart(t.getTotalParts()); // Cần thêm trường này vào MockTest entity
            dto.setDurationTimes(t.getDurationTimes()); // Cần thêm trường này vào MockTest entity
            // Đếm số người đã làm bài thi này
            Long totalTaken = resultRepo.countDistinctUsersByMockTestId(t.getId());
            dto.setTotalTaken(totalTaken != null ? totalTaken : 0L);
            // Lấy rating trung bình và số lượt đánh giá
            Double avgRating = reviewRepo.findAverageRatingByMockTestId(t.getId());
            dto.setAverageRating(avgRating != null ? avgRating : 0.0);
            long reviewCount = reviewRepo.countByMockTestIdAndStatus(t.getId(), korastudy.be.entity.Enum.ReviewStatus.ACTIVE);
            dto.setReviewCount(reviewCount);
            dtos.add(dto);
        }
        return dtos;
    }

    public ExamDetailResponse getExamDetail(Long id) {
        MockTest test = mockTestRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));
        ExamDetailResponse dto = new ExamDetailResponse();
        dto.setId(test.getId());
        dto.setTitle(test.getTitle());
        dto.setDescription(test.getDescription());
        dto.setLevel(test.getLevel());
        dto.setTotalQuestions(test.getTotalQuestions());
        dto.setTotalPart(test.getTotalParts());
        dto.setDurationTimes(test.getDurationTimes());

        // Hardcode instructions và requirements
        dto.setInstructions(Arrays.asList(
                "Đọc kỹ hướng dẫn trước khi bắt đầu",
                "Làm bài theo thứ tự từ phần nghe đến phần đọc",
                "Không được quay lại phần đã làm",
                "Nộp bài trước khi hết thời gian"
        ));
        dto.setRequirements(Arrays.asList(
                "Đã học xong bảng chữ cái Hangeul",
                "Có từ vựng cơ bản khoảng 800-1500 từ"
        ));

        // Lấy các phần của bài thi
        List<MockTestPart> parts = partRepo.findByMockTestId(id);
        List<ExamPartResponse> partDTOs = new ArrayList<>();
        for (MockTestPart part : parts) {
            ExamPartResponse partDTO = new ExamPartResponse();
            partDTO.setPartId(part.getId()); // MockTestPart có trường id
            partDTO.setPartNumber(part.getPartNumber());
            partDTO.setTitle(part.getTitle());
            partDTO.setDescription(part.getDescription());

            // Lấy câu hỏi trong phần này
            List<MockTestQuestion> questions = questionRepo.findByQuestionPart_Id(part.getId());
            List<ExamQuestionResponse> questionDTOs = new ArrayList<>();
            for (MockTestQuestion q : questions) {
                ExamQuestionResponse qdto = new ExamQuestionResponse();
                qdto.setQuestionId(q.getId()); // MockTestQuestion có trường id
                qdto.setQuestionText(q.getQuestionText());
                qdto.setOption(q.getOption());
                qdto.setImageUrl(q.getImageUrl());
                qdto.setAudioUrl(q.getAudioUrl());
                qdto.setExplanation(q.getExplanation());
                questionDTOs.add(qdto);
            }
            partDTO.setQuestions(questionDTOs);
            partDTOs.add(partDTO);
        }
        dto.setParts(partDTOs);
        return dto;
    }

    public ExamResultResponse submitExam(Long examId, SubmitExamRequest request, Long userId) {
        System.out.println("=== EXAM SUBMISSION DEBUG ===");
        System.out.println("Exam ID: " + examId + " (type: " + examId.getClass().getSimpleName() + ")");
        System.out.println("User ID: " + userId + " (type: " + userId.getClass().getSimpleName() + ")");
        System.out.println("Request: " + request);
        System.out.println("Request answers: " + (request != null ? request.getAnswers() : "null"));
        
        // Validate input parameters
        if (examId == null) {
            throw new RuntimeException("Exam ID không được null");
        }
        if (userId == null) {
            throw new RuntimeException("User ID không được null");
        }
        if (request == null || request.getAnswers() == null) {
            throw new RuntimeException("Request hoặc answers không được null");
        }
        
        // Tìm MockTest
        MockTest mockTest = null;
        try {
            mockTest = mockTestRepo.findById(examId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi với ID: " + examId));
            System.out.println("✅ Found exam: " + mockTest.getTitle());
        } catch (Exception e) {
            System.err.println("❌ Error finding exam: " + e.getMessage());
            throw e;
        }

        // Tìm User với nhiều phương pháp khác nhau
        User user = null;
        try {
            System.out.println("🔍 Searching for user with ID: " + userId);
            
            // Phương pháp 1: Tìm trực tiếp theo user.id
            Optional<User> userOptional = userRepo.findById(userId);
            System.out.println("findById result: " + userOptional.isPresent());
            
            if (userOptional.isPresent()) {
                user = userOptional.get();
                System.out.println("✅ Found user by findById: " + user.getEmail() + " (ID: " + user.getId() + ")");
            } else {
                System.out.println("❌ User not found by user.id, trying account_id...");
                
                // Phương pháp 2: Tìm theo account_id nếu userId thực chất là account_id
                try {
                    // Assuming you have a method to find user by account_id
                    // You might need to create this method in UserRepository
                    List<User> allUsers = userRepo.findAll();
                    for (User u : allUsers) {
                        System.out.println("Checking user: ID=" + u.getId() + ", Email=" + u.getEmail() + 
                                         ", AccountId=" + (u.getAccount() != null ? u.getAccount().getId() : "null"));
                        
                        // Try to match by account_id if user has account relation
                        if (u.getAccount() != null && u.getAccount().getId().equals(userId)) {
                            user = u;
                            System.out.println("✅ Found user by account_id: " + user.getEmail() + " (User ID: " + user.getId() + ", Account ID: " + userId + ")");
                            break;
                        }
                        
                        // Also try direct ID match
                        if (u.getId().equals(userId)) {
                            user = u;
                            System.out.println("✅ Found user by manual ID search: " + user.getEmail());
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error in alternative user search: " + e.getMessage());
                }
            }
            
            if (user == null) {
                // Log tất cả user IDs để debug
                List<User> allUsers = userRepo.findAll();
                StringBuilder userInfo = new StringBuilder("Available users: ");
                for (User u : allUsers) {
                    String accountId = (u.getAccount() != null) ? u.getAccount().getId().toString() : "null";
                    userInfo.append("User[id=").append(u.getId())
                           .append(", email=").append(u.getEmail())
                           .append(", accountId=").append(accountId)
                           .append("] ");
                }
                System.err.println(userInfo.toString());
                
                throw new RuntimeException("Không tìm thấy người dùng với ID: " + userId + ". " +
                    "Đã thử tìm theo user.id và account.id nhưng không thấy.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception while finding user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tìm người dùng: " + e.getMessage());
        }

        // Verify user object
        System.out.println("✅ Final user object: " + user.getEmail() + " (User ID: " + user.getId() + 
                          ", Account ID: " + (user.getAccount() != null ? user.getAccount().getId() : "null") + ")");

        // Lấy danh sách câu hỏi và đáp án đúng
        List<MockTestPart> parts = partRepo.findByMockTestId(examId);
        Map<Long, String> correctAnswers = new HashMap<>();
        Map<Long, String> questionTexts = new HashMap<>();
        int totalQuestions = 0;

        System.out.println("📝 Processing " + parts.size() + " parts");

        for (MockTestPart part : parts) {
            List<MockTestQuestion> questions = questionRepo.findByQuestionPart_Id(part.getId());
            System.out.println("Part " + part.getPartNumber() + " has " + questions.size() + " questions");
            
            for (MockTestQuestion q : questions) {
                correctAnswers.put(q.getId(), q.getCorrectAnswer());
                questionTexts.put(q.getId(), q.getQuestionText());
                totalQuestions++;
                System.out.println("Question " + q.getId() + " correct answer: " + q.getCorrectAnswer());
            }
        }

        System.out.println("📊 Total questions: " + totalQuestions);
        System.out.println("📊 Student answers count: " + request.getAnswers().size());

        // Chấm điểm và tạo chi tiết đáp án
        int correct = 0;
        int incorrect = 0;
        List<ExamAnswerDetailResponse> answerDetails = new ArrayList<>();
        
        for (SubmitAnswerRequest ans : request.getAnswers()) {
            System.out.println("Checking question " + ans.getQuestionId() + 
                             ", student answer: '" + ans.getSelectedAnswer() + "'");
            
            String correctAns = correctAnswers.get(ans.getQuestionId());
            String questionText = questionTexts.get(ans.getQuestionId());
            boolean isCorrect = correctAns != null && correctAns.trim().equals(ans.getSelectedAnswer().trim());
            
            if (isCorrect) {
                correct++;
                System.out.println("✅ Correct");
            } else {
                incorrect++;
                System.out.println("❌ Incorrect (correct: '" + correctAns + "')");
            }
            
            // Tạo chi tiết đáp án
            ExamAnswerDetailResponse detail = new ExamAnswerDetailResponse();
            detail.setQuestionId(ans.getQuestionId());
            detail.setQuestionText(questionText != null ? questionText : "");
            detail.setSelectedAnswer(ans.getSelectedAnswer());
            detail.setCorrectAnswer(correctAns != null ? correctAns : "");
            detail.setIsCorrect(isCorrect);
            detail.setPoints(isCorrect ? 1 : 0);
            
            answerDetails.add(detail);
        }
        
        // Thêm các câu chưa làm
        for (Map.Entry<Long, String> entry : correctAnswers.entrySet()) {
            Long questionId = entry.getKey();
            boolean answered = request.getAnswers().stream()
                .anyMatch(ans -> ans.getQuestionId().equals(questionId));
            
            if (!answered) {
                ExamAnswerDetailResponse detail = new ExamAnswerDetailResponse();
                detail.setQuestionId(questionId);
                detail.setQuestionText(questionTexts.get(questionId));
                detail.setSelectedAnswer("");
                detail.setCorrectAnswer(entry.getValue());
                detail.setIsCorrect(false);
                detail.setPoints(0);
                answerDetails.add(detail);
            }
        }
        
        // Sắp xếp theo questionId
        answerDetails.sort((a, b) -> Long.compare(a.getQuestionId(), b.getQuestionId()));
        
        // Tính điểm theo phần trăm
        double score = (totalQuestions > 0) ? (correct * 100.0 / totalQuestions) : 0;
        
        System.out.println("📊 Final score: " + correct + "/" + totalQuestions + " = " + score + "%");

        // Lưu kết quả
        ComprehensiveTestResult result = null;
        try {
            result = ComprehensiveTestResult.builder()
                    .testType("COMPREHENSIVE")
                    .testDate(LocalDateTime.now())
                    .noCorrect(correct)
                    .noIncorrect(incorrect)
                    .scores(score)
                    .mockTest(mockTest)
                    .user(user)
                    .build();
            
            System.out.println("💾 Saving result...");
            result = resultRepo.save(result);
            System.out.println("✅ Result saved with ID: " + result.getId());
            
            // Ghi nhận hoạt động học tập (giả sử mỗi bài thi mất khoảng duration phút)
            int studyDuration = mockTest.getDurationTimes() != null ? mockTest.getDurationTimes() : 60;
            studyActivityService.recordStudyActivity(userId, studyDuration);
            System.out.println("📝 Recorded study activity: " + studyDuration + " minutes");
            
        } catch (Exception e) {
            System.err.println("❌ Error saving result: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu kết quả: " + e.getMessage());
        }

        // Trả về kết quả với chi tiết đáp án
        ExamResultResponse dto = new ExamResultResponse();
        dto.setExamId(examId);
        dto.setResultId(result.getId());
        dto.setTotalQuestions(totalQuestions);
        dto.setNoCorrect(correct);
        dto.setNoIncorrect(incorrect);
        dto.setScores(score);
        dto.setTestDate(result.getTestDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dto.setAnswerDetails(answerDetails); // Thêm chi tiết đáp án
        
        System.out.println("🎉 Returning result with " + answerDetails.size() + " answer details");
        return dto;
    }

    public ExamResultResponse submitPracticeTest(Long examId, List<Long> partIds, SubmitExamRequest request, Long userId) {
        MockTest mockTest = mockTestRepo.findById(examId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Get questions from selected parts only
        Map<Long, String> correctAnswers = new HashMap<>();
        Map<Long, Integer> questionPoints = new HashMap<>();
        int totalQuestions = 0;
        int totalPoints = 0;

        for (Long partId : partIds) {
            List<MockTestQuestion> questions = questionRepo.findByQuestionPart_Id(partId);
            for (MockTestQuestion q : questions) {
                correctAnswers.put(q.getId(), q.getCorrectAnswer());
                questionPoints.put(q.getId(), q.getPoints());
                totalQuestions++;
                totalPoints += q.getPoints();
            }
        }

        // Grade the practice test
        int correct = 0;
        int incorrect = 0;
        int earnedPoints = 0;

        for (SubmitAnswerRequest ans : request.getAnswers()) {
            String correctAns = correctAnswers.get(ans.getQuestionId());
            int points = questionPoints.getOrDefault(ans.getQuestionId(), 1);
            
            if (correctAns != null && correctAns.equals(ans.getSelectedAnswer())) {
                correct++;
                earnedPoints += points;
            } else {
                incorrect++;
            }
        }

        double score = (totalPoints > 0) ? (earnedPoints * 100.0 / totalPoints) : 0;

        // Save practice test result
        PracticeTestResult result = PracticeTestResult.builder()
            .testType("PRACTICE")
            .noCorrect(correct)
            .noIncorrect(incorrect)
            .totalQuestions(totalQuestions)
            .scores(score)
            .earnedPoints(earnedPoints)
            .totalPoints(totalPoints)
            .completedParts(partIds)
            .mockTest(mockTest)
            .user(user)
            .build();
        result = practiceResultRepo.save(result);

        // Return result
        ExamResultResponse dto = new ExamResultResponse();
        dto.setExamId(examId);
        dto.setResultId(result.getId());
        dto.setTotalQuestions(totalQuestions);
        dto.setNoCorrect(correct);
        dto.setNoIncorrect(incorrect);
        dto.setScores(score);
        dto.setEarnedPoints(earnedPoints);
        dto.setTotalPoints(totalPoints);
        dto.setTestDate(result.getTestDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return dto;
    }

    public List<ExamResultResponse> getExamHistory(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<ComprehensiveTestResult> results = resultRepo.findByUser(user);
        List<ExamResultResponse> dtos = new ArrayList<>();

        for (ComprehensiveTestResult r : results) {
            ExamResultResponse dto = new ExamResultResponse();
            dto.setExamId(r.getMockTest().getId()); // Qua quan hệ ManyToOne
            dto.setTotalQuestions(r.getNoCorrect() + r.getNoIncorrect());
            dto.setNoCorrect(r.getNoCorrect());
            dto.setNoIncorrect(r.getNoIncorrect());
            dto.setScores(r.getScores());
            dto.setTestDate(r.getTestDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            dtos.add(dto);
        }
        return dtos;
    }

    public List<ExamListItemResponse> searchExams(String title, String level, String type, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        
        // Handle empty strings as null for the query
        String searchTitle = (title != null && !title.trim().isEmpty()) ? title.trim() : null;
        String searchLevel = (level != null && !level.trim().isEmpty()) ? level.trim() : null;
        String searchType = (type != null && !type.trim().isEmpty()) ? type.trim() : null;

        org.springframework.data.domain.Page<MockTest> pageResult = mockTestRepo.searchMockTests(searchTitle, searchLevel, searchType, pageable);
        
        List<MockTest> paginatedTests = pageResult.getContent();

        // Convert sang DTO
        List<ExamListItemResponse> dtos = new ArrayList<>();
        for (MockTest t : paginatedTests) {
            ExamListItemResponse dto = new ExamListItemResponse();
            dto.setId(t.getId());
            dto.setTitle(t.getTitle());
            dto.setDescription(t.getDescription());
            dto.setLevel(t.getLevel());
            dto.setTotalQuestions(t.getTotalQuestions());
            dto.setTotalPart(t.getTotalParts());
            dto.setDurationTimes(t.getDurationTimes());
            dtos.add(dto);
        }
        return dtos;
    }

    public ExamResultResponse getExamResultDetail(Long resultId) {
        ComprehensiveTestResult result = resultRepo.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả thi"));

        ExamResultResponse dto = new ExamResultResponse();
        dto.setExamId(result.getMockTest().getId());
        dto.setTotalQuestions(result.getNoCorrect() + result.getNoIncorrect());
        dto.setNoCorrect(result.getNoCorrect());
        dto.setNoIncorrect(result.getNoIncorrect());
        dto.setScores(result.getScores());
        dto.setTestDate(result.getTestDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return dto;
    }

    public List<ExamListItemResponse> getExamsByLevel(String level) {
        List<MockTest> tests = mockTestRepo.findByLevel(level);
        List<ExamListItemResponse> dtos = new ArrayList<>();

        for (MockTest t : tests) {
            ExamListItemResponse dto = new ExamListItemResponse();
            dto.setId(t.getId());
            dto.setTitle(t.getTitle());
            dto.setDescription(t.getDescription());
            dto.setLevel(t.getLevel());
            dto.setTotalQuestions(t.getTotalQuestions());
            dto.setTotalPart(t.getTotalParts());
            dto.setDurationTimes(t.getDurationTimes());
            dtos.add(dto);
        }
        return dtos;
    }

    public Map<String, Object> getExamStatistics(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<ComprehensiveTestResult> results = resultRepo.findByUser(user);
        Map<String, Object> statistics = new HashMap<>();

        if (results.isEmpty()) {
            statistics.put("totalExams", 0);
            statistics.put("averageScore", 0.0);
            statistics.put("bestScore", 0.0);
            statistics.put("totalCorrect", 0);
            statistics.put("totalIncorrect", 0);
            statistics.put("accuracyRate", 0.0);
            return statistics;
        }

        // Tính thống kê
        int totalExams = results.size();
        double totalScore = results.stream().mapToDouble(ComprehensiveTestResult::getScores).sum();
        double averageScore = totalScore / totalExams;
        double bestScore = results.stream().mapToDouble(ComprehensiveTestResult::getScores).max().orElse(0.0);
        int totalCorrect = results.stream().mapToInt(ComprehensiveTestResult::getNoCorrect).sum();
        int totalIncorrect = results.stream().mapToInt(ComprehensiveTestResult::getNoIncorrect).sum();
        double accuracyRate = (totalCorrect + totalIncorrect) > 0 ?
                (totalCorrect * 100.0 / (totalCorrect + totalIncorrect)) : 0.0;

        statistics.put("totalExams", totalExams);
        statistics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        statistics.put("bestScore", Math.round(bestScore * 100.0) / 100.0);
        statistics.put("totalCorrect", totalCorrect);
        statistics.put("totalIncorrect", totalIncorrect);
        statistics.put("accuracyRate", Math.round(accuracyRate * 100.0) / 100.0);

        // Thêm study streak và total study hours
        Map<String, Object> studyStats = studyActivityService.getUserStudyStats(userId);
        statistics.put("studyStreak", studyStats.get("studyStreak"));
        statistics.put("totalStudyHours", studyStats.get("totalStudyHours"));

        return statistics;
    }

    public List<ExamCommentResponse> getExamComments(Long examId) {
        List<ExamComment> comments = commentRepo.findByMockTest_IdOrderByCreatedAtDesc(examId);
        List<ExamCommentResponse> dtos = new ArrayList<>();

        for (ExamComment comment : comments) {
            ExamCommentResponse dto = new ExamCommentResponse();
            dto.setId(comment.getId());
            dto.setContext(comment.getContext());
            dto.setCreatedAt(comment.getCreatedAt());
            dto.setUpdatedAt(comment.getUpdatedAt());
            // Sử dụng firstName + lastName thay vì username
            String displayName = (comment.getUser().getFirstName() != null ? comment.getUser().getFirstName() : "") +
                           (comment.getUser().getLastName() != null ? " " + comment.getUser().getLastName() : "");
            dto.setUsername(displayName.trim().isEmpty() ? comment.getUser().getEmail() : displayName.trim());
            dtos.add(dto);
        }
        return dtos;
    }

    public ExamCommentResponse addExamComment(Long examId, String context, Long userId) {
        MockTest mockTest = mockTestRepo.findById(examId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        ExamComment comment = ExamComment.builder()
            .context(context)
            .mockTest(mockTest)
            .user(user)
            .build();
        comment = commentRepo.save(comment);

        ExamCommentResponse dto = new ExamCommentResponse();
        dto.setId(comment.getId());
        dto.setContext(comment.getContext());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        // Sử dụng firstName + lastName thay vì username
        String displayName = (user.getFirstName() != null ? user.getFirstName() : "") +
                       (user.getLastName() != null ? " " + user.getLastName() : "");
        dto.setUsername(displayName.trim().isEmpty() ? user.getEmail() : displayName.trim());
        return dto;
    }
}