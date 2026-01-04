package korastudy.be.entity.Enum;

/**
 * Trạng thái của bài báo
 */
public enum ArticleStatus {
    DRAFT,      // Bản nháp - chưa publish
    SCHEDULED,  // Đã lên lịch publish
    PUBLISHED,  // Đã publish - user có thể xem
    ARCHIVED    // Đã lưu trữ - ẩn khỏi user
}
