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

    private final UserStudyActivityRepository activityRepo;
    private final UserRepository userRepo;

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
        return totalMinutes / 60; // Convert to hours
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
        
        return stats;
    }
}
