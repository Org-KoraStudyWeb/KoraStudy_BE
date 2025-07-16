package korastudy.be.service.impl;

import korastudy.be.dto.request.Exam.*;
import korastudy.be.dto.response.Exam.*;
import korastudy.be.entity.MockTest.*;
import korastudy.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminExamService {
    
    private final MockTestRepository mockTestRepo;
    private final MockTestPartRepository partRepo;
    private final MockTestQuestionRepository questionRepo;
    private final MockTestAnswersRepository answersRepo;

    public List<ExamListItemResponse> getAllExamsForAdmin() {
        List<MockTest> tests = mockTestRepo.findAll();
        List<ExamListItemResponse> dtos = new ArrayList<>();
        
        for (MockTest test : tests) {
            ExamListItemResponse dto = new ExamListItemResponse();
            dto.setId(test.getId());
            dto.setTitle(test.getTitle());
            dto.setDescription(test.getDescription());
            dto.setLevel(test.getLevel());
            dto.setTotalQuestions(test.getTotalQuestions());
            dto.setTotalPart(test.getTotalParts());
            dto.setDurationTimes(test.getDurationTimes());
            dto.setCreatedAt(test.getCreatedAt());
            dto.setUpdatedAt(test.getUpdatedAt());
            dto.setIsActive(test.getIsActive());
            dtos.add(dto);
        }
        return dtos;
    }

    public AdminExamDetailResponse getExamForEdit(Long id) {
        MockTest test = mockTestRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));
        
        AdminExamDetailResponse dto = new AdminExamDetailResponse();
        dto.setId(test.getId());
        dto.setTitle(test.getTitle());
        dto.setDescription(test.getDescription());
        dto.setLevel(test.getLevel());
        dto.setTotalQuestions(test.getTotalQuestions());
        dto.setTotalParts(test.getTotalParts());
        dto.setDurationTimes(test.getDurationTimes());
        dto.setInstructions(test.getInstructions());
        dto.setRequirements(test.getRequirements());
        dto.setIsActive(test.getIsActive());
        dto.setCreatedAt(test.getCreatedAt());
        dto.setUpdatedAt(test.getUpdatedAt());

        // Load parts with questions
        List<MockTestPart> parts = partRepo.findByMockTestIdOrderByPartNumber(id);
        List<AdminExamPartResponse> partDTOs = new ArrayList<>();
        
        for (MockTestPart part : parts) {
            AdminExamPartResponse partDTO = new AdminExamPartResponse();
            partDTO.setId(part.getId());
            partDTO.setPartNumber(part.getPartNumber());
            partDTO.setTitle(part.getTitle());
            partDTO.setDescription(part.getDescription());
            partDTO.setInstructions(part.getInstructions());
            partDTO.setQuestionCount(part.getQuestionCount());
            partDTO.setTimeLimit(part.getTimeLimit());

            // Load questions
            List<MockTestQuestion> questions = questionRepo.findByQuestionPart_IdOrderByQuestionOrder(part.getId());
            List<AdminExamQuestionResponse> questionDTOs = new ArrayList<>();
            
            for (MockTestQuestion q : questions) {
                AdminExamQuestionResponse qdto = new AdminExamQuestionResponse();
                qdto.setId(q.getId());
                qdto.setQuestionText(q.getQuestionText());
                qdto.setQuestionType(q.getQuestionType());
                qdto.setOption(q.getOption());
                qdto.setCorrectAnswer(q.getCorrectAnswer());
                qdto.setExplanation(q.getExplanation());
                qdto.setImageUrl(q.getImageUrl());
                qdto.setAudioUrl(q.getAudioUrl());
                qdto.setQuestionOrder(q.getQuestionOrder());
                qdto.setPoints(q.getPoints());
                questionDTOs.add(qdto);
            }
            partDTO.setQuestions(questionDTOs);
            partDTOs.add(partDTO);
        }
        dto.setParts(partDTOs);
        return dto;
    }

    public AdminExamDetailResponse createExam(CreateExamRequest request) {
        MockTest test = MockTest.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .level(request.getLevel())
            .totalQuestions(0)
            .totalParts(0)
            .durationTimes(request.getDurationTimes())
            .instructions(request.getInstructions())
            .requirements(request.getRequirements())
            .isActive(true)
            .build();
        
        test = mockTestRepo.save(test);
        return getExamForEdit(test.getId());
    }

    public AdminExamDetailResponse updateExam(Long id, UpdateExamRequest request) {
        MockTest test = mockTestRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));
        
        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setLevel(request.getLevel());
        test.setDurationTimes(request.getDurationTimes());
        test.setInstructions(request.getInstructions());
        test.setRequirements(request.getRequirements());
        test.setUpdatedAt(LocalDateTime.now());
        
        mockTestRepo.save(test);
        return getExamForEdit(id);
    }

    public void deleteExam(Long id) {
        MockTest test = mockTestRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));
        
        // Xóa tất cả questions trước
        List<MockTestPart> parts = partRepo.findByMockTestId(id);
        for (MockTestPart part : parts) {
            questionRepo.deleteByQuestionPart_Id(part.getId());
        }
        
        // Xóa tất cả parts
        partRepo.deleteByMockTestId(id);
        
        // Xóa exam
        mockTestRepo.delete(test);
    }

    public AdminExamPartResponse addPart(Long examId, CreateExamPartRequest request) {
        MockTest test = mockTestRepo.findById(examId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));
        
        int nextPartNumber = partRepo.findMaxPartNumberByMockTestId(examId) + 1;
        
        MockTestPart part = MockTestPart.builder()
            .partNumber(nextPartNumber)
            .title(request.getTitle())
            .description(request.getDescription())
            .instructions(request.getInstructions())
            .questionCount(0)
            .timeLimit(request.getTimeLimit())
            .mockTest(test)
            .build();
        
        part = partRepo.save(part);
        
        // Update total parts
        test.setTotalParts(test.getTotalParts() + 1);
        mockTestRepo.save(test);
        
        AdminExamPartResponse dto = new AdminExamPartResponse();
        dto.setId(part.getId());
        dto.setPartNumber(part.getPartNumber());
        dto.setTitle(part.getTitle());
        dto.setDescription(part.getDescription());
        dto.setInstructions(part.getInstructions());
        dto.setQuestionCount(part.getQuestionCount());
        dto.setTimeLimit(part.getTimeLimit());
        dto.setQuestions(new ArrayList<>());
        
        return dto;
    }

    public AdminExamPartResponse updatePart(Long partId, UpdateExamPartRequest request) {
        MockTestPart part = partRepo.findById(partId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phần thi"));
        
        part.setTitle(request.getTitle());
        part.setDescription(request.getDescription());
        part.setInstructions(request.getInstructions());
        part.setTimeLimit(request.getTimeLimit());
        
        partRepo.save(part);
        
        AdminExamPartResponse dto = new AdminExamPartResponse();
        dto.setId(part.getId());
        dto.setPartNumber(part.getPartNumber());
        dto.setTitle(part.getTitle());
        dto.setDescription(part.getDescription());
        dto.setInstructions(part.getInstructions());
        dto.setQuestionCount(part.getQuestionCount());
        dto.setTimeLimit(part.getTimeLimit());
        
        return dto;
    }

    public void deletePart(Long partId) {
        MockTestPart part = partRepo.findById(partId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phần thi"));
        
        MockTest test = part.getMockTest();
        
        // Xóa tất cả questions trong part
        questionRepo.deleteByQuestionPart_Id(partId);
        
        // Xóa part
        partRepo.delete(part);
        
        // Update counters
        test.setTotalParts(test.getTotalParts() - 1);
        test.setTotalQuestions(questionRepo.countByQuestionPart_MockTest_Id(test.getId()));
        mockTestRepo.save(test);
    }

    public AdminExamQuestionResponse addQuestion(Long partId, CreateExamQuestionRequest request) {
        MockTestPart part = partRepo.findById(partId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phần thi"));
        
        int nextOrder = questionRepo.findMaxQuestionOrderByPartId(partId) + 1;
        
        MockTestQuestion question = MockTestQuestion.builder()
            .questionText(request.getQuestionText())
            .questionType(request.getQuestionType())
            .option(request.getOption())
            .correctAnswer(request.getCorrectAnswer())
            .explanation(request.getExplanation())
            .questionOrder(nextOrder)
            .points(request.getPoints() != null ? request.getPoints() : 1)
            .questionPart(part)
            .build();
        
        question = questionRepo.save(question);
        
        // Update counters
        part.setQuestionCount(part.getQuestionCount() + 1);
        partRepo.save(part);
        
        MockTest test = part.getMockTest();
        test.setTotalQuestions(test.getTotalQuestions() + 1);
        mockTestRepo.save(test);
        
        AdminExamQuestionResponse dto = new AdminExamQuestionResponse();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setOption(question.getOption());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setImageUrl(question.getImageUrl());
        dto.setAudioUrl(question.getAudioUrl());
        dto.setQuestionOrder(question.getQuestionOrder());
        dto.setPoints(question.getPoints());
        
        return dto;
    }

    public AdminExamQuestionResponse updateQuestion(Long questionId, UpdateExamQuestionRequest request) {
        MockTestQuestion question = questionRepo.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));
        
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setOption(request.getOption());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(request.getExplanation());
        if (request.getPoints() != null) {
            question.setPoints(request.getPoints());
        }
        
        questionRepo.save(question);
        
        AdminExamQuestionResponse dto = new AdminExamQuestionResponse();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setOption(question.getOption());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setImageUrl(question.getImageUrl());
        dto.setAudioUrl(question.getAudioUrl());
        dto.setQuestionOrder(question.getQuestionOrder());
        dto.setPoints(question.getPoints());
        
        return dto;
    }

    public void deleteQuestion(Long questionId) {
        MockTestQuestion question = questionRepo.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));
        
        MockTestPart part = question.getQuestionPart();
        MockTest test = part.getMockTest();
        
        questionRepo.delete(question);
        
        // Update counters
        part.setQuestionCount(part.getQuestionCount() - 1);
        partRepo.save(part);
        
        test.setTotalQuestions(test.getTotalQuestions() - 1);
        mockTestRepo.save(test);
    }

    public void updateQuestionImage(Long questionId, String imageUrl) {
        MockTestQuestion question = questionRepo.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));
        
        question.setImageUrl(imageUrl);
        questionRepo.save(question);
    }

    public void updateQuestionAudio(Long questionId, String audioUrl) {
        MockTestQuestion question = questionRepo.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));
        
        question.setAudioUrl(audioUrl);
        questionRepo.save(question);
    }

    public boolean toggleExamActive(Long id) {
        MockTest test = mockTestRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));
        
        test.setIsActive(!test.getIsActive());
        mockTestRepo.save(test);
        
        return test.getIsActive();
    }

    public AdminExamDetailResponse duplicateExam(Long id) {
        MockTest originalTest = mockTestRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));
        
        // Tạo bản sao exam
        MockTest newTest = MockTest.builder()
            .title(originalTest.getTitle() + " (Copy)")
            .description(originalTest.getDescription())
            .level(originalTest.getLevel())
            .totalQuestions(0)
            .totalParts(0)
            .durationTimes(originalTest.getDurationTimes())
            .instructions(originalTest.getInstructions())
            .requirements(originalTest.getRequirements())
            .isActive(false)
            .build();
        
        newTest = mockTestRepo.save(newTest);
        
        // Copy parts và questions
        List<MockTestPart> originalParts = partRepo.findByMockTestIdOrderByPartNumber(id);
        
        for (MockTestPart originalPart : originalParts) {
            MockTestPart newPart = MockTestPart.builder()
                .partNumber(originalPart.getPartNumber())
                .title(originalPart.getTitle())
                .description(originalPart.getDescription())
                .instructions(originalPart.getInstructions())
                .questionCount(0)
                .timeLimit(originalPart.getTimeLimit())
                .mockTest(newTest)
                .build();
            
            newPart = partRepo.save(newPart);
            
            // Copy questions
            List<MockTestQuestion> originalQuestions = questionRepo.findByQuestionPart_IdOrderByQuestionOrder(originalPart.getId());
            
            for (MockTestQuestion originalQuestion : originalQuestions) {
                MockTestQuestion newQuestion = MockTestQuestion.builder()
                    .questionText(originalQuestion.getQuestionText())
                    .questionType(originalQuestion.getQuestionType())
                    .option(originalQuestion.getOption())
                    .correctAnswer(originalQuestion.getCorrectAnswer())
                    .explanation(originalQuestion.getExplanation())
                    .imageUrl(originalQuestion.getImageUrl())
                    .audioUrl(originalQuestion.getAudioUrl())
                    .questionOrder(originalQuestion.getQuestionOrder())
                    .points(originalQuestion.getPoints())
                    .questionPart(newPart)
                    .build();
                
                questionRepo.save(newQuestion);
                
                newPart.setQuestionCount(newPart.getQuestionCount() + 1);
                newTest.setTotalQuestions(newTest.getTotalQuestions() + 1);
            }
            
            partRepo.save(newPart);
            newTest.setTotalParts(newTest.getTotalParts() + 1);
        }
        
        mockTestRepo.save(newTest);
        
        return getExamForEdit(newTest.getId());
    }

    public AdminExamQuestionResponse getQuestionById(Long questionId) {
        MockTestQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        AdminExamQuestionResponse dto = new AdminExamQuestionResponse();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setOption(question.getOption());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setImageUrl(question.getImageUrl());
        dto.setAudioUrl(question.getAudioUrl());
        dto.setQuestionOrder(question.getQuestionOrder());
        dto.setPoints(question.getPoints());

        return dto;
    }


}
