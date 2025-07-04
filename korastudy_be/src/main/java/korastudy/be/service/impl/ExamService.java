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

    public List<ExamListItemResponse> getAllExams() {
        List<MockTest> tests = mockTestRepo.findAll();
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
                questionDTOs.add(qdto);
            }
            partDTO.setQuestions(questionDTOs);
            partDTOs.add(partDTO);
        }
        dto.setParts(partDTOs);
        return dto;
    }

    public ExamResultResponse submitExam(Long examId, SubmitExamRequest request, Long userId) {
        // Tìm MockTest
        MockTest mockTest = mockTestRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));

        // Tìm User
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Lấy danh sách câu hỏi và đáp án đúng
        List<MockTestPart> parts = partRepo.findByMockTestId(examId);
        Map<Long, String> correctAnswers = new HashMap<>();
        int totalQuestions = 0;

        for (MockTestPart part : parts) {
            List<MockTestQuestion> questions = questionRepo.findByQuestionPart_Id(part.getId());
            for (MockTestQuestion q : questions) {
                // Tìm đáp án đúng theo quan hệ ManyToOne
                List<MockTestAnswers> ansList = answersRepo.findByQuestionAnswer_Id(part.getId());
                for (MockTestAnswers ans : ansList) {
                    if (ans.getIsCorrect()) { // Boolean isCorrect
                        correctAnswers.put(q.getId(), ans.getSelectedAnswer());
                    }
                }
                totalQuestions++;
            }
        }

        // Chấm điểm
        int correct = 0;
        int incorrect = 0;
        for (SubmitAnswerRequest ans : request.getAnswers()) {
            String correctAns = correctAnswers.get(ans.getQuestionId());
            if (correctAns != null && correctAns.equals(ans.getSelectedAnswer())) {
                correct++;
            } else {
                incorrect++;
            }
        }
        double score = (totalQuestions > 0) ? (correct * 1.0 / totalQuestions) * 100 : 0;

        // Lưu kết quả với quan hệ ManyToOne
        ComprehensiveTestResult result = ComprehensiveTestResult.builder()
                .testType("MOCK")
                .testDate(LocalDateTime.now())
                .noCorrect(correct)
                .noIncorrect(incorrect)
                .scores(score)
                .mockTest(mockTest) // Quan hệ ManyToOne
                .user(user) // Quan hệ ManyToOne
                .build();
        resultRepo.save(result);

        // Trả về kết quả
        ExamResultResponse dto = new ExamResultResponse();
        dto.setExamId(examId);
        dto.setTotalQuestions(totalQuestions);
        dto.setNoCorrect(correct);
        dto.setNoIncorrect(incorrect);
        dto.setScores(score);
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
        List<MockTest> tests = mockTestRepo.findAll();
        List<MockTest> filteredTests = tests;

        // Filter theo title
        if (title != null && !title.trim().isEmpty()) {
            filteredTests = filteredTests.stream()
                    .filter(test -> test.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .toList();
        }

        // Filter theo level
        if (level != null && !level.trim().isEmpty()) {
            filteredTests = filteredTests.stream()
                    .filter(test -> test.getLevel().equals(level))
                    .toList();
        }

//        // Filter theo type (nếu có trường type trong MockTest)
////        if (type != null && !type.trim().isEmpty()) {
////            filteredTests = filteredTests.stream()
////                    .filter(test -> test.getTestType() != null && test.getTestType().equals(type))
////                    .toList();
////        }

        // Pagination
        int start = page * size;
        int end = Math.min(start + size, filteredTests.size());

        if (start >= filteredTests.size()) {
            return new ArrayList<>();
        }

        List<MockTest> paginatedTests = filteredTests.subList(start, end);

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

        return statistics;
    }
}