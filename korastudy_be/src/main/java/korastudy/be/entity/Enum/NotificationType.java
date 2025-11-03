package korastudy.be.entity.Enum;

public enum NotificationType {
    SYSTEM("Hệ thống"),
    EXAM_RESULT("Kết quả bài thi"),
    FORUM_INTERACTION("Tương tác cộng đồng"),
    PROFILE("Hồ sơ"),
    COURSE("Khóa học");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}