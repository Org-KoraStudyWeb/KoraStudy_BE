package korastudy.be.service.impl;

import korastudy.be.entity.MockTest.*;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import korastudy.be.repository.MockTestRepository;
import korastudy.be.repository.PracticeTestResultRepository;
import korastudy.be.service.IAccountService;
import korastudy.be.service.ISampleDataService;
import korastudy.be.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SampleDataService implements ISampleDataService {
    
    private final MockTestRepository mockTestRepository;
    private final PracticeTestResultRepository practiceTestResultRepository;
    
    @Override
    public void createSampleMockTests() {
        log.info("Creating sample mock tests...");
        
        // Create TOPIK I Test 1
        MockTest topikI1 = createMockTest(
            "TOPIK I - Test 1", 
            "TOPIK I level practice test with basic Korean language skills", 
            "TOPIK I", 
            40, 2, 100
        );
        
        // Add parts to TOPIK I Test 1
        MockTestPart listeningPart1 = createMockTestPart(1, "Listening", "Listening comprehension section", topikI1);
        MockTestPart readingPart1 = createMockTestPart(2, "Reading", "Reading comprehension section", topikI1);
        
        // Add questions and answers
        createSampleQuestionsAndAnswers(listeningPart1, readingPart1);
        
        topikI1.setParts(Arrays.asList(listeningPart1, readingPart1));
        mockTestRepository.save(topikI1);
        
        // Create TOPIK I Test 2
        MockTest topikI2 = createMockTest(
            "TOPIK I - Test 2", 
            "TOPIK I level practice test focusing on reading comprehension", 
            "TOPIK I", 
            40, 2, 100
        );
        
        MockTestPart listeningPart2 = createMockTestPart(1, "Listening", "Listening comprehension section", topikI2);
        MockTestPart readingPart2 = createMockTestPart(2, "Reading", "Reading comprehension section", topikI2);
        
        createSampleQuestionsAndAnswers(listeningPart2, readingPart2);
        
        topikI2.setParts(Arrays.asList(listeningPart2, readingPart2));
        mockTestRepository.save(topikI2);
        
        // Create TOPIK II Test 1
        MockTest topikII1 = createMockTest(
            "TOPIK II - Test 1", 
            "TOPIK II level practice test for intermediate Korean learners", 
            "TOPIK II", 
            50, 3, 180
        );
        
        MockTestPart listeningPart3 = createMockTestPart(1, "Listening", "Listening comprehension section", topikII1);
        MockTestPart readingPart3 = createMockTestPart(2, "Reading", "Reading comprehension section", topikII1);
        MockTestPart writingPart3 = createMockTestPart(3, "Writing", "Writing section", topikII1);
        
        createSampleQuestionsAndAnswers(listeningPart3, readingPart3);
        createAdvancedQuestions(writingPart3);
        
        topikII1.setParts(Arrays.asList(listeningPart3, readingPart3, writingPart3));
        mockTestRepository.save(topikII1);
        
        // Create Beginner Test
        MockTest beginnerTest = createMockTest(
            "TOPIK Beginner", 
            "Basic Korean language test for beginners", 
            "Beginner", 
            30, 2, 90
        );
        
        MockTestPart basicListening = createMockTestPart(1, "Basic Listening", "Basic listening skills", beginnerTest);
        MockTestPart basicReading = createMockTestPart(2, "Basic Reading", "Basic reading skills", beginnerTest);
        
        createBasicQuestions(basicListening, basicReading);
        
        beginnerTest.setParts(Arrays.asList(basicListening, basicReading));
        mockTestRepository.save(beginnerTest);
        
        log.info("Sample mock tests created successfully");
    }
    
    @Override
    public void createSampleUsers() {
        log.info("Creating sample users and test results...");
        // This would require implementing user creation logic
        // For now, we'll skip this as it depends on existing user service
        log.info("Sample users creation skipped - requires existing user service implementation");
    }
    
    @Override
    public void createAllSampleData() {
        createSampleMockTests();
        createSampleUsers();
        log.info("All sample data created successfully");
    }
    
    @Override
    public void clearSampleData() {
        log.info("Clearing sample data...");
        practiceTestResultRepository.deleteAll();
        mockTestRepository.deleteAll();
        log.info("Sample data cleared successfully");
    }
    
    private MockTest createMockTest(String title, String description, String level, int totalQuestions, int totalParts, int duration) {
        return MockTest.builder()
            .title(title)
            .description(description)
            .level(level)
            .totalQuestions(totalQuestions)
            .totalParts(totalParts)
            .durationTimes(duration)
            .build();
    }
    
    private MockTestPart createMockTestPart(int partNumber, String title, String description, MockTest mockTest) {
        return MockTestPart.builder()
            .partNumber(partNumber)
            .title(title)
            .description(description)
            .mockTest(mockTest)
            .questions(new ArrayList<>())
            .answers(new ArrayList<>())
            .build();
    }
    
    private void createSampleQuestionsAndAnswers(MockTestPart listeningPart, MockTestPart readingPart) {
        // Listening questions
        MockTestQuestion q1 = MockTestQuestion.builder()
            .option("A")
            .audioUrl("/audio/q1.mp3")
            .questionText("다음을 듣고 알맞은 답을 고르십시오.")
            .questionPart(listeningPart)
            .answers(new ArrayList<>())
            .build();
        
        createAnswers(q1, Arrays.asList("듣기", "말하기", "읽기", "쓰기"), 0);
        
        MockTestQuestion q2 = MockTestQuestion.builder()
            .option("B")
            .audioUrl("/audio/q2.mp3")
            .questionText("대화를 듣고 여자가 할 일을 고르십시오.")
            .questionPart(listeningPart)
            .answers(new ArrayList<>())
            .build();
        
        createAnswers(q2, Arrays.asList("요리하기", "청소하기", "공부하기", "운동하기"), 0);
        
        // Reading questions
        MockTestQuestion q3 = MockTestQuestion.builder()
            .option("A")
            .imageUrl("/images/q3.jpg")
            .questionText("다음 그림을 보고 알맞은 문장을 고르십시오.")
            .questionPart(readingPart)
            .answers(new ArrayList<>())
            .build();
        
        createAnswers(q3, Arrays.asList("책을 읽고 있습니다", "음악을 듣고 있습니다", "영화를 보고 있습니다", "게임을 하고 있습니다"), 0);
        
        MockTestQuestion q4 = MockTestQuestion.builder()
            .option("B")
            .questionText("빈칸에 들어갈 가장 알맞은 것을 고르십시오. 저는 매일 아침 _____ 먹습니다.")
            .questionPart(readingPart)
            .answers(new ArrayList<>())
            .build();
        
        createAnswers(q4, Arrays.asList("밥을", "물을", "커피를", "차를"), 0);
        
        listeningPart.setQuestions(Arrays.asList(q1, q2));
        readingPart.setQuestions(Arrays.asList(q3, q4));
    }
    
    private void createAdvancedQuestions(MockTestPart writingPart) {
        MockTestQuestion q1 = MockTestQuestion.builder()
            .option("A")
            .questionText("다음 주제에 대해 자신의 의견을 600-700자로 쓰십시오. '온라인 교육의 장점과 단점'")
            .questionPart(writingPart)
            .answers(new ArrayList<>())
            .build();
        
        // Writing questions don't have multiple choice answers
        writingPart.setQuestions(Arrays.asList(q1));
    }
    
    private void createBasicQuestions(MockTestPart basicListening, MockTestPart basicReading) {
        // Basic listening question
        MockTestQuestion q1 = MockTestQuestion.builder()
            .option("A")
            .audioUrl("/audio/basic1.mp3")
            .questionText("무엇에 대한 이야기입니까?")
            .questionPart(basicListening)
            .answers(new ArrayList<>())
            .build();
        
        createAnswers(q1, Arrays.asList("음식", "날씨", "가족", "학교"), 1);
        
        // Basic reading question
        MockTestQuestion q2 = MockTestQuestion.builder()
            .option("A")
            .questionText("이 사람은 어디에 갑니까?")
            .questionPart(basicReading)
            .answers(new ArrayList<>())
            .build();
        
        createAnswers(q2, Arrays.asList("병원", "학교", "회사", "집"), 1);
        
        basicListening.setQuestions(Arrays.asList(q1));
        basicReading.setQuestions(Arrays.asList(q2));
    }
    
    private void createAnswers(MockTestQuestion question, List<String> answerTexts, int correctIndex) {
        List<MockTestAnswers> answers = new ArrayList<>();
        
        for (int i = 0; i < answerTexts.size(); i++) {
            MockTestAnswers answer = MockTestAnswers.builder()
                .selectedAnswer(answerTexts.get(i))
                .isCorrect(i == correctIndex)
                .questionAnswer(question)
                .answerPart(question.getQuestionPart())
                .build();
            answers.add(answer);
        }
        
        question.setAnswers(answers);
    }
}
