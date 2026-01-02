package korastudy.be.dto.response.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Enum.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Chỉ hiển thị fields không null
public class ReviewDTO {
    // ==== BASIC FIELDS (for PUBLIC) ====
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private ReviewType reviewType;
    private Long targetId;
    private String targetTitle;
    private String targetType;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private ReviewStatus status;
    private String adminNote;

    // ==== INTERACTION FIELDS (for AUTHENTICATED USERS) ====
    private Long likesCount;
    private Long reportsCount;
    private Boolean userLiked; // For current user - Boolean để có thể null
    private Boolean userReported; // For current user - Boolean để có thể null

    // ==== ADMIN-ONLY FIELDS ====
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Long> likedUserIds = null; // Danh sách ID users đã like

    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Map<String, Object>> reportDetails = null; // Chi tiết reports

    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> reviewerInfo = null; // Thông tin người viết review

    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ipAddress = null; // IP của người viết

    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Map<String, Object>> editHistory = null; // Lịch sử chỉnh sửa

    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isFlagged = false; // Cờ đánh dấu review có vấn đề

    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String flaggedReason = null; // Lý do bị đánh dấu

    // ==== CONVENIENCE METHODS ====

    /**
     * Kiểm tra xem DTO có chứa thông tin ADMIN không
     */
    public boolean hasAdminInfo() {
        return likedUserIds != null ||
                reportDetails != null ||
                reviewerInfo != null ||
                ipAddress != null ||
                editHistory != null;
    }

    /**
     * Xóa tất cả thông tin ADMIN (cho PUBLIC responses)
     */
    public void stripAdminInfo() {
        this.likedUserIds = null;
        this.reportDetails = null;
        this.reviewerInfo = null;
        this.ipAddress = null;
        this.editHistory = null;
        this.isFlagged = null;
        this.flaggedReason = null;
        // Ẩn thông tin sensitive nếu review bị HIDDEN/DELETED
        if (this.status == ReviewStatus.HIDDEN || this.status == ReviewStatus.DELETED) {
            this.comment = "Nội dung đã bị ẩn";
            this.userAvatar = null;
        }
    }

    /**
     * Lấy thông tin cơ bản cho PUBLIC view
     */
    public static ReviewDTO getPublicView(ReviewDTO dto) {
        if (dto == null) return null;

        ReviewDTO publicDTO = ReviewDTO.builder()
                .id(dto.id)
                .userId(dto.userId)
                .username(dto.username)
                .userAvatar(dto.userAvatar)
                .reviewType(dto.reviewType)
                .targetId(dto.targetId)
                .targetTitle(dto.targetTitle)
                .targetType(dto.targetType)
                .rating(dto.rating)
                .createdAt(dto.createdAt)
                .lastModified(dto.lastModified)
                .status(dto.status)
                .likesCount(dto.likesCount)
                .reportsCount(dto.reportsCount)
                .userLiked(dto.userLiked)
                .userReported(dto.userReported)
                .build();

        // Chỉ hiển thị comment nếu review ACTIVE
        if (dto.status == ReviewStatus.ACTIVE || dto.status == ReviewStatus.REPORTED) {
            publicDTO.setComment(dto.comment);
        } else {
            publicDTO.setComment("Nội dung đã bị ẩn");
        }

        // Ẩn adminNote cho PUBLIC
        publicDTO.setAdminNote(null);

        return publicDTO;
    }
}