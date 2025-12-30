package korastudy.be.dto.response.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDTO {

    // ============ TỔNG QUAN ============
    private Long totalReviews;
    private Long activeReviews;
    private Long reportedReviews;
    private Long hiddenReviews;
    private Long deletedReviews;

    // ============ PHÂN LOẠI THEO LOẠI ============
    private Long courseReviews;
    private Long mockTestReviews;

    // ============ RATING ============
    private Double averageRating;
    private Double averageCourseRating;
    private Double averageMockTestRating;

    // ============ DISTRIBUTION ============
    private Map<Integer, Long> ratingDistribution;
    private Map<String, Long> typeDistribution;
    private Map<String, Long> statusDistribution;

    // ============ XU HƯỚNG ============
    private Long reviewsLast7Days;
    private Long reviewsLast30Days;
    private Double averageRatingTrend;

    // ============ DỮ LIỆU CHO BIỂU ĐỒ ============
    private List<Integer> dailyReviews;           // Đổi từ monthlyReviews -> dailyReviews
    private List<Map<String, Object>> recentActivities;
    private List<Map<String, Object>> topReviewedCourses;

    // ============ CONSTRUCTOR TỪ MAP ============
    public ReviewStatsDTO(Map<String, Object> statsMap) {
        this.totalReviews = getLongValue(statsMap, "totalReviews");
        this.activeReviews = getLongValue(statsMap, "activeReviews");
        this.reportedReviews = getLongValue(statsMap, "reportedReviews");
        this.hiddenReviews = getLongValue(statsMap, "hiddenReviews");
        this.deletedReviews = getLongValue(statsMap, "deletedReviews");

        this.courseReviews = getLongValue(statsMap, "courseReviews");
        this.mockTestReviews = getLongValue(statsMap, "mockTestReviews");

        this.averageRating = getDoubleValue(statsMap, "averageRating");
        this.averageCourseRating = getDoubleValue(statsMap, "averageCourseRating");
        this.averageMockTestRating = getDoubleValue(statsMap, "averageMockTestRating");

        this.reviewsLast7Days = getLongValue(statsMap, "reviewsLast7Days");
        this.reviewsLast30Days = getLongValue(statsMap, "reviewsLast30Days");
        this.averageRatingTrend = getDoubleValue(statsMap, "averageRatingTrend");

        // Lấy rating distribution
        if (statsMap.get("ratingDistribution") instanceof Map) {
            @SuppressWarnings("unchecked") Map<Integer, Long> ratingDist = (Map<Integer, Long>) statsMap.get("ratingDistribution");
            this.ratingDistribution = ratingDist;
        }

        // Lấy daily reviews (7 ngày)
        if (statsMap.get("dailyReviews") instanceof List) {
            @SuppressWarnings("unchecked") List<Integer> dailyReviews = (List<Integer>) statsMap.get("dailyReviews");
            this.dailyReviews = dailyReviews;
        }

        // Lấy recent activities
        if (statsMap.get("recentActivities") instanceof List) {
            @SuppressWarnings("unchecked") List<Map<String, Object>> activities = (List<Map<String, Object>>) statsMap.get("recentActivities");
            this.recentActivities = activities;
        }

        // Lấy top reviewed courses
        if (statsMap.get("topReviewedCourses") instanceof List) {
            @SuppressWarnings("unchecked") List<Map<String, Object>> courses = (List<Map<String, Object>>) statsMap.get("topReviewedCourses");
            this.topReviewedCourses = courses;
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        return 0L;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return 0.0;
    }
}