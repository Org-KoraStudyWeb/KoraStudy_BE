-- Insert sample data for MockTest functionality - SQL Server Version
-- This script will create sample mock tests with parts, questions, and answers

-- Insert sample MockTest data
INSERT INTO mock_test (test_title, test_description, test_level, total_question, total_part, duration_times, created_at, last_modified) VALUES
('TOPIK I - Test 1', 'TOPIK I level practice test with basic Korean language skills', 'TOPIK I', 40, 2, 100, GETDATE(), GETDATE()),
('TOPIK I - Test 2', 'TOPIK I level practice test focusing on reading comprehension', 'TOPIK I', 40, 2, 100, GETDATE(), GETDATE()),
('TOPIK II - Test 1', 'TOPIK II level practice test for intermediate Korean learners', 'TOPIK II', 50, 3, 180, GETDATE(), GETDATE()),
('TOPIK II - Test 2', 'TOPIK II level practice test with advanced grammar and vocabulary', 'TOPIK II', 50, 3, 180, GETDATE(), GETDATE()),
('TOPIK Beginner', 'Basic Korean language test for beginners', 'Beginner', 30, 2, 90, GETDATE(), GETDATE());

-- Insert sample TestPart data
INSERT INTO test_part (part_number, title, part_description, test_id, created_at, last_modified) VALUES
-- Parts for TOPIK I - Test 1 (test_id = 1)
(1, 'Listening', 'Listening comprehension section', 1, GETDATE(), GETDATE()),
(2, 'Reading', 'Reading comprehension section', 1, GETDATE(), GETDATE()),

-- Parts for TOPIK I - Test 2 (test_id = 2)
(1, 'Listening', 'Listening comprehension section', 2, GETDATE(), GETDATE()),
(2, 'Reading', 'Reading comprehension section', 2, GETDATE(), GETDATE()),

-- Parts for TOPIK II - Test 1 (test_id = 3)
(1, 'Listening', 'Listening comprehension section', 3, GETDATE(), GETDATE()),
(2, 'Reading', 'Reading comprehension section', 3, GETDATE(), GETDATE()),
(3, 'Writing', 'Writing section', 3, GETDATE(), GETDATE()),

-- Parts for TOPIK II - Test 2 (test_id = 4)
(1, 'Listening', 'Listening comprehension section', 4, GETDATE(), GETDATE()),
(2, 'Reading', 'Reading comprehension section', 4, GETDATE(), GETDATE()),
(3, 'Writing', 'Writing section', 4, GETDATE(), GETDATE()),

-- Parts for TOPIK Beginner (test_id = 5)
(1, 'Basic Listening', 'Basic listening skills', 5, GETDATE(), GETDATE()),
(2, 'Basic Reading', 'Basic reading skills', 5, GETDATE(), GETDATE());

-- Insert sample MockQuestion data
INSERT INTO mock_question (question_option, image_url, audio_url, question_text, part_id, created_at) VALUES
-- Questions for Part 1 (Listening) of TOPIK I - Test 1
('A', NULL, '/audio/q1.mp3', N'다음을 듣고 알맞은 답을 고르십시오.', 1, GETDATE()),
('B', NULL, '/audio/q2.mp3', N'대화를 듣고 여자가 할 일을 고르십시오.', 1, GETDATE()),
('C', NULL, '/audio/q3.mp3', N'다음을 듣고 내용과 같은 것을 고르십시오.', 1, GETDATE()),

-- Questions for Part 2 (Reading) of TOPIK I - Test 1
('A', '/images/q4.jpg', NULL, N'다음 그림을 보고 알맞은 문장을 고르십시오.', 2, GETDATE()),
('B', NULL, NULL, N'빈칸에 들어갈 가장 알맞은 것을 고르십시오. 저는 매일 아침 _____ 먹습니다.', 2, GETDATE()),
('C', NULL, NULL, N'다음 글의 내용과 같은 것을 고르십시오.', 2, GETDATE()),

-- Questions for Part 1 (Listening) of TOPIK II - Test 1
('A', NULL, '/audio/q7.mp3', N'다음을 듣고 남자의 심정으로 가장 알맞은 것을 고르십시오.', 5, GETDATE()),
('B', NULL, '/audio/q8.mp3', N'다음을 듣고 여자가 말하는 의도를 고르십시오.', 5, GETDATE()),

-- Questions for Part 2 (Reading) of TOPIK II - Test 1
('A', NULL, NULL, N'다음 글의 주제로 가장 알맞은 것을 고르십시오.', 6, GETDATE()),
('B', NULL, NULL, N'다음 글에서 <보기>와 같은 의미로 쓰인 것을 고르십시오.', 6, GETDATE());

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
(N'한국어를 배우고 있다', 1, 1, 3, GETDATE(), GETDATE()),
(N'한국어를 가르치고 있다', 0, 1, 3, GETDATE(), GETDATE()),
(N'한국어를 번역하고 있다', 0, 1, 3, GETDATE(), GETDATE()),
(N'한국어를 연구하고 있다', 0, 1, 3, GETDATE(), GETDATE()),

-- Answers for Question 4
(N'책을 읽고 있습니다', 1, 2, 4, GETDATE(), GETDATE()),
(N'음악을 듣고 있습니다', 0, 2, 4, GETDATE(), GETDATE()),
(N'영화를 보고 있습니다', 0, 2, 4, GETDATE(), GETDATE()),
(N'게임을 하고 있습니다', 0, 2, 4, GETDATE(), GETDATE()),

-- Answers for Question 5
(N'밥을', 1, 2, 5, GETDATE(), GETDATE()),
(N'물을', 0, 2, 5, GETDATE(), GETDATE()),
(N'커피를', 0, 2, 5, GETDATE(), GETDATE()),
(N'차를', 0, 2, 5, GETDATE(), GETDATE()),

-- Answers for Question 6
(N'날씨가 좋다', 1, 2, 6, GETDATE(), GETDATE()),
(N'날씨가 나쁘다', 0, 2, 6, GETDATE(), GETDATE()),
(N'날씨가 춥다', 0, 2, 6, GETDATE(), GETDATE()),
(N'날씨가 덥다', 0, 2, 6, GETDATE(), GETDATE()),

-- Answers for Question 7 (TOPIK II)
(N'기쁘다', 0, 5, 7, GETDATE(), GETDATE()),
(N'슬프다', 0, 5, 7, GETDATE(), GETDATE()),
(N'걱정된다', 1, 5, 7, GETDATE(), GETDATE()),
(N'화가 난다', 0, 5, 7, GETDATE(), GETDATE()),

-- Answers for Question 8 (TOPIK II)
(N'제안하고 있다', 1, 5, 8, GETDATE(), GETDATE()),
(N'거절하고 있다', 0, 5, 8, GETDATE(), GETDATE()),
(N'사과하고 있다', 0, 5, 8, GETDATE(), GETDATE()),
(N'감사하고 있다', 0, 5, 8, GETDATE(), GETDATE()),

-- Answers for Question 9 (TOPIK II)
(N'건강한 생활습관의 중요성', 1, 6, 9, GETDATE(), GETDATE()),
(N'운동의 종류와 방법', 0, 6, 9, GETDATE(), GETDATE()),
(N'음식의 영양가치', 0, 6, 9, GETDATE(), GETDATE()),
(N'병원 치료의 필요성', 0, 6, 9, GETDATE(), GETDATE()),

-- Answers for Question 10 (TOPIK II)
(N'그러므로', 1, 6, 10, GETDATE(), GETDATE()),
(N'그러나', 0, 6, 10, GETDATE(), GETDATE()),
(N'또한', 0, 6, 10, GETDATE(), GETDATE()),
(N'예를 들어', 0, 6, 10, GETDATE(), GETDATE());

-- Insert some sample users and accounts (if not exists)
-- Check if account exists before inserting
IF NOT EXISTS (SELECT 1 FROM account WHERE username = 'testuser1')
INSERT INTO account (username, password, email) VALUES
('testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXgMkwqVvj7tJFJOoNfcpXfEq.', 'testuser1@example.com');

IF NOT EXISTS (SELECT 1 FROM account WHERE username = 'testuser2')
INSERT INTO account (username, password, email) VALUES
('testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXgMkwqVvj7tJFJOoNfcpXfEq.', 'testuser2@example.com');

-- Insert sample user data
IF NOT EXISTS (SELECT 1 FROM [user] WHERE user_code = 'USER001')
INSERT INTO [user] (user_code, full_name, phone_number, account_id) VALUES
('USER001', 'Test User 1', '0123456789', 1);

IF NOT EXISTS (SELECT 1 FROM [user] WHERE user_code = 'USER002')
INSERT INTO [user] (user_code, full_name, phone_number, account_id) VALUES
('USER002', 'Test User 2', '0987654321', 2);

-- Insert sample practice test results
INSERT INTO practice_test_result (test_type, test_date, no_correct, no_incorrect, user_id, test_id, created_at, last_modified) VALUES
('PRACTICE', DATEADD(day, -1, GETDATE()), 35, 5, 1, 1, GETDATE(), GETDATE()),
('PRACTICE', DATEADD(day, -2, GETDATE()), 32, 8, 1, 2, GETDATE(), GETDATE()),
('PRACTICE', DATEADD(day, -3, GETDATE()), 28, 12, 1, 1, GETDATE(), GETDATE()),
('PRACTICE', DATEADD(day, -1, GETDATE()), 42, 8, 2, 3, GETDATE(), GETDATE()),
('PRACTICE', DATEADD(day, -2, GETDATE()), 38, 12, 2, 3, GETDATE(), GETDATE());

-- Insert sample comments
INSERT INTO mock_test_comment (comment_text, rating, user_id, test_id, created_at, last_modified) VALUES
('This test is very helpful for TOPIK preparation!', 5, 1, 1, GETDATE(), GETDATE()),
('Good practice questions, but could use more variety.', 4, 2, 1, GETDATE(), GETDATE()),
('Excellent test for intermediate level.', 5, 1, 3, GETDATE(), GETDATE()),
('The listening section is quite challenging.', 4, 2, 3, GETDATE(), GETDATE());
