-- Simple Sample Data for MockTest Testing - SQL Server
-- This script focuses only on MockTest data to test frontend

-- Insert sample MockTest data
INSERT INTO mock_test (test_title, test_description, test_level, total_question, total_part, duration_times, created_at, last_modified) VALUES
('TOPIK I - Sample Test', 'TOPIK I level practice test with basic Korean language skills', 'TOPIK I', 40, 2, 100, GETDATE(), GETDATE()),
('TOPIK II - Sample Test', 'TOPIK II level practice test for intermediate Korean learners', 'TOPIK II', 50, 3, 180, GETDATE(), GETDATE()),
('TOPIK Beginner Test', 'Basic Korean language test for beginners', 'Beginner', 30, 2, 90, GETDATE(), GETDATE());

-- Insert sample TestPart data
INSERT INTO test_part (part_number, title, part_description, test_id, created_at, last_modified) VALUES
-- Parts for TOPIK I - Sample Test (test_id = 1)
(1, 'Listening', 'Listening comprehension section', 1, GETDATE(), GETDATE()),
(2, 'Reading', 'Reading comprehension section', 1, GETDATE(), GETDATE()),

-- Parts for TOPIK II - Sample Test (test_id = 2)
(1, 'Listening', 'Listening comprehension section', 2, GETDATE(), GETDATE()),
(2, 'Reading', 'Reading comprehension section', 2, GETDATE(), GETDATE()),
(3, 'Writing', 'Writing section', 2, GETDATE(), GETDATE()),

-- Parts for TOPIK Beginner Test (test_id = 3)
(1, 'Basic Listening', 'Basic listening skills', 3, GETDATE(), GETDATE()),
(2, 'Basic Reading', 'Basic reading skills', 3, GETDATE(), GETDATE());

-- Insert sample MockQuestion data
INSERT INTO mock_question (question_option, image_url, audio_url, question_text, part_id, created_at) VALUES
-- Questions for Part 1 (Listening) of TOPIK I
('A', NULL, '/audio/q1.mp3', N'다음을 듣고 알맞은 답을 고르십시오.', 1, GETDATE()),
('B', NULL, '/audio/q2.mp3', N'대화를 듣고 여자가 할 일을 고르십시오.', 1, GETDATE()),

-- Questions for Part 2 (Reading) of TOPIK I
('A', NULL, NULL, N'빈칸에 들어갈 가장 알맞은 것을 고르십시오.', 2, GETDATE()),
('B', NULL, NULL, N'다음 글의 내용과 같은 것을 고르십시오.', 2, GETDATE()),

-- Questions for TOPIK II
('A', NULL, '/audio/q5.mp3', N'다음을 듣고 남자의 심정을 고르십시오.', 3, GETDATE()),
('B', NULL, NULL, N'다음 글의 주제를 고르십시오.', 4, GETDATE());

-- Insert sample TestAnswers data
INSERT INTO test_answers (selected_answer, is_correct, part_id, question_id, created_at, last_modified) VALUES
-- Answers for Question 1
(N'듣기', 1, 1, 1, GETDATE(), GETDATE()),
(N'말하기', 0, 1, 1, GETDATE(), GETDATE()),
(N'읽기', 0, 1, 1, GETDATE(), GETDATE()),
(N'쓰기', 0, 1, 1, GETDATE(), GETDATE()),

-- Answers for Question 2
(N'요리하기', 1, 1, 2, GETDATE(), GETDATE()),
(N'청소하기', 0, 1, 2, GETDATE(), GETDATE()),
(N'공부하기', 0, 1, 2, GETDATE(), GETDATE()),
(N'운동하기', 0, 1, 2, GETDATE(), GETDATE()),

-- Answers for Question 3
(N'밥을', 1, 2, 3, GETDATE(), GETDATE()),
(N'물을', 0, 2, 3, GETDATE(), GETDATE()),
(N'커피를', 0, 2, 3, GETDATE(), GETDATE()),
(N'차를', 0, 2, 3, GETDATE(), GETDATE()),

-- Answers for Question 4
(N'날씨가 좋다', 1, 2, 4, GETDATE(), GETDATE()),
(N'날씨가 나쁘다', 0, 2, 4, GETDATE(), GETDATE()),
(N'날씨가 춥다', 0, 2, 4, GETDATE(), GETDATE()),
(N'날씨가 덥다', 0, 2, 4, GETDATE(), GETDATE()),

-- Answers for Question 5 (TOPIK II)
(N'기쁘다', 0, 3, 5, GETDATE(), GETDATE()),
(N'슬프다', 0, 3, 5, GETDATE(), GETDATE()),
(N'걱정된다', 1, 3, 5, GETDATE(), GETDATE()),
(N'화가 난다', 0, 3, 5, GETDATE(), GETDATE()),

-- Answers for Question 6 (TOPIK II)
(N'건강한 생활습관', 1, 4, 6, GETDATE(), GETDATE()),
(N'운동의 종류', 0, 4, 6, GETDATE(), GETDATE()),
(N'음식의 영양가치', 0, 4, 6, GETDATE(), GETDATE()),
(N'병원 치료', 0, 4, 6, GETDATE(), GETDATE());
