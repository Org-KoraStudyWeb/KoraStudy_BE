package korastudy.be.util;

import korastudy.be.dto.exam.*;
import korastudy.be.entity.MockTest.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExamMapper {
    
    public MockTestDTO toDTO(MockTest mockTest, boolean includeCorrectAnswers) {
        if (mockTest == null) return null;
        
        List<MockTestPartDTO> parts = mockTest.getParts() != null ? 
            mockTest.getParts().stream()
                .map(part -> toPartDTO(part, includeCorrectAnswers))
                .collect(Collectors.toList()) : 
            new ArrayList<>();
        
        return MockTestDTO.builder()
            .id(mockTest.getId())
            .title(mockTest.getTitle())
            .description(mockTest.getDescription())
            .level(mockTest.getLevel())
            .totalQuestions(mockTest.getTotalQuestions())
            .totalParts(mockTest.getTotalParts())
            .durationTimes(mockTest.getDurationTimes())
            .createdAt(mockTest.getCreatedAt())
            .updatedAt(mockTest.getLastModified())
            .parts(parts)
            .build();
    }
    
    public MockTestPartDTO toPartDTO(MockTestPart part, boolean includeCorrectAnswers) {
        if (part == null) return null;
        
        List<MockTestQuestionDTO> questions = part.getQuestions() != null ?
            part.getQuestions().stream()
                .map(question -> toQuestionDTO(question, includeCorrectAnswers))
                .collect(Collectors.toList()) :
            new ArrayList<>();
        
        return MockTestPartDTO.builder()
            .id(part.getId())
            .partNumber(part.getPartNumber())
            .title(part.getTitle())
            .description(part.getDescription())
            .questions(questions)
            .build();
    }
    
    public MockTestQuestionDTO toQuestionDTO(MockTestQuestion question, boolean includeCorrectAnswers) {
        if (question == null) return null;
        
        List<MockTestAnswerDTO> answers = question.getAnswers() != null ?
            question.getAnswers().stream()
                .map(answer -> toAnswerDTO(answer, includeCorrectAnswers))
                .collect(Collectors.toList()) :
            new ArrayList<>();
        
        return MockTestQuestionDTO.builder()
            .id(question.getId())
            .option(question.getOption())
            .imageUrl(question.getImageUrl())
            .audioUrl(question.getAudioUrl())
            .questionText(question.getQuestionText())
            .answers(answers)
            .build();
    }
    
    public MockTestAnswerDTO toAnswerDTO(MockTestAnswers answer, boolean includeCorrectAnswers) {
        if (answer == null) return null;
        
        return MockTestAnswerDTO.builder()
            .id(answer.getId())
            .selectedAnswer(answer.getSelectedAnswer())
            .isCorrect(includeCorrectAnswers ? answer.getIsCorrect() : null)
            .build();
    }
    
    public TestResultDTO toTestResultDTO(PracticeTestResult result) {
        if (result == null) return null;
        
        int totalQuestions = result.getNoCorrect() + result.getNoIncorrect();
        double percentage = totalQuestions > 0 ? (double) result.getNoCorrect() / totalQuestions * 100 : 0;
        
        return TestResultDTO.builder()
            .id(result.getId())
            .testType(result.getTestType())
            .testDate(result.getTestDate())
            .noCorrect(result.getNoCorrect())
            .noIncorrect(result.getNoIncorrect())
            .totalQuestions(totalQuestions)
            .score(percentage)
            .level(result.getMockTest().getLevel())
            .testTitle(result.getMockTest().getTitle())
            .mockTestId(result.getMockTest().getId())
            .username(result.getUser().getAccount().getUsername())
            .build();
    }
    
    public String calculateGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
}
