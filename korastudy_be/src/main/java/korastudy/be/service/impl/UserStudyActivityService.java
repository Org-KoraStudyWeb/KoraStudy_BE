package korastudy.be.service.impl;

import korastudy.be.entity.User.User;
import korastudy.be.entity.User.UserStudyActivity;
import korastudy.be.repository.UserRepository;
import korastudy.be.repository.UserStudyActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserStudyActivityService {

    private final korastudy.be.repository.ComprehensiveTestResultRepository comprehensiveTestResultRepository;
    private final korastudy.be.repository.PracticeTestResultRepository practiceTestResultRepository;
    private final korastudy.be.repository.TestResultRepository testResultRepository;
    private final korastudy.be.repository.UserStudyActivityRepository activityRepo;
    private final korastudy.be.repository.UserRepository userRepo;

    /**
     * Ghi nhận hoạt động học tập của user (gọi khi user làm bài thi, flashcard, xem bài học...)
     */
    @Transactional
    public void recordStudyActivity(Long userId, int durationMinutes) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        LocalDate today = LocalDate.now();
        UserStudyActivity activity = activityRepo.findByUserAndStudyDate(user, today)
                .orElse(UserStudyActivity.builder()
                        .user(user)
                        .studyDate(today)
                        .studyDurationMinutes(0)
                        .activitiesCount(0)
                        .build());

        activity.setStudyDurationMinutes(activity.getStudyDurationMinutes() + durationMinutes);
        activity.setActivitiesCount(activity.getActivitiesCount() + 1);
        activity.setLastActivityTime(LocalDateTime.now());

        activityRepo.save(activity);
    }

    /**
     * Tính study streak (số ngày liên tiếp học)
     */
    public int calculateStudyStreak(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;
        
        // Lấy activities 365 ngày gần nhất
        List<UserStudyActivity> activities = activityRepo.findRecentActivities(user, today.minusDays(365));
        
        if (activities.isEmpty()) {
            return 0;
        }

        int streak = 0;
        
        // Kiểm tra có học hôm nay hoặc hôm qua không
        boolean foundToday = activities.stream()
                .anyMatch(a -> a.getStudyDate().equals(today));
        boolean foundYesterday = activities.stream()
                .anyMatch(a -> a.getStudyDate().equals(today.minusDays(1)));
        
        if (!foundToday && !foundYesterday) {
            return 0; // Streak bị gián đoạn
        }
        
        // Bắt đầu đếm từ hôm nay hoặc hôm qua
        if (!foundToday) {
            checkDate = today.minusDays(1);
        }
        
        // Đếm ngược để tính streak
        for (UserStudyActivity activity : activities) {
            if (activity.getStudyDate().equals(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            }
        }
        
        return streak;
    }

    /**
     * Lấy tổng thời gian học (giờ)
     */
    public int getTotalStudyHours(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Integer totalMinutes = activityRepo.getTotalStudyMinutes(user);
        return totalMinutes != null ? totalMinutes / 60 : 0;
    }

    /**
     * Lấy thống kê đầy đủ
     */
    public Map<String, Object> getUserStudyStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        int streak = calculateStudyStreak(userId);
        int totalHours = getTotalStudyHours(userId);
        
        stats.put("studyStreak", streak);
        stats.put("totalStudyHours", totalHours);
        stats.put("recentDetailedActivities", getRecentDetailedActivities(userId));
        
        return stats;
    }

    /**
     * Lấy danh sách hoạt động chi tiết gần đây (Exam, Quiz...)
     */
    public List<korastudy.be.dto.response.user.UserRecentActivityResponse> getRecentDetailedActivities(Long userId) {
        List<korastudy.be.dto.response.user.UserRecentActivityResponse> activities = new java.util.ArrayList<>();
        
        // 1. Lấy kết quả thi tổng hợp (Comprehensive)
        // Lấy top 5
        List<korastudy.be.entity.MockTest.ComprehensiveTestResult> comprehensiveResults = 
            comprehensiveTestResultRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
        comprehensiveResults.stream().limit(5).forEach(result -> {
            activities.add(korastudy.be.dto.response.user.UserRecentActivityResponse.builder()
                .id(result.getId())
                .title("Bài thi thử: " + (result.getMockTest() != null ? result.getMockTest().getTitle() : "N/A"))
                .type(korastudy.be.dto.response.user.UserRecentActivityResponse.ActivityType.COMPREHENSIVE_TEST)
                .timestamp(result.getCreatedAt())
                .score(result.getScores() != null ? String.format("%.1f", result.getScores()) : "N/A")
                .result(result.getScores() != null && result.getScores() >= 5.0 ? "Đạt" : "Không đạt") // Giả sử >= 5 là đạt
                .build());
        });

        // 2. Lấy kết quả thi luyện tập (Practice)
        User user = userRepo.findById(userId).orElse(null);
        if (user != null) {
            List<korastudy.be.entity.MockTest.PracticeTestResult> practiceResults = 
                practiceTestResultRepository.findByUserOrderByTestDateDesc(user);
                
            practiceResults.stream().limit(5).forEach(result -> {
                activities.add(korastudy.be.dto.response.user.UserRecentActivityResponse.builder()
                    .id(result.getId())
                    .title("Luyện tập: " + (result.getMockTest() != null ? result.getMockTest().getTitle() : "N/A"))
                    .type(korastudy.be.dto.response.user.UserRecentActivityResponse.ActivityType.PRACTICE_TEST)
                    .timestamp(result.getTestDate())
                    .score(result.getScores() != null ? String.format("%.1f", result.getScores()) : "N/A")
                    .result(null) // Luyện tập thường không xét Đạt/Không đạt
                    .build());
            });
        }

        // 3. Lấy kết quả bài học (Quiz)
        List<korastudy.be.entity.Course.TestResult> quizResults = 
            testResultRepository.findByUserIdOrderByTakenDateDesc(userId);
            
        quizResults.stream().limit(5).forEach(result -> {
            activities.add(korastudy.be.dto.response.user.UserRecentActivityResponse.builder()
                .id(result.getId())
                .title("Bài kiểm tra: " + (result.getQuiz() != null ? result.getQuiz().getTitle() : "N/A"))
                .type(korastudy.be.dto.response.user.UserRecentActivityResponse.ActivityType.QUIZ)
                .timestamp(result.getTakenDate())
                .score(result.getScore() != null ? String.format("%.1f", result.getScore()) : "N/A")
                .result(Boolean.TRUE.equals(result.getIsPassed()) ? "Đạt" : "Cần cố gắng")
                .build());
        });

        // 4. Sắp xếp lại tổng thể theo thời gian giảm dần
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        // 5. Lấy top 10 hoạt động mới nhất
        if (activities.size() > 10) {
            return activities.subList(0, 10);
        }
        
        return activities;
    }
}
