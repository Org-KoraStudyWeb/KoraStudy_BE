package korastudy.be.dto.response.blog;

import korastudy.be.entity.Enum.ReportStatus;
import korastudy.be.entity.Post.PostReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostReportResponse {
    private Long id;
    private Long postId;
    private String postTitle;
    private Long reporterId;
    private String reporterName;
    private String reason;
    private String description;
    private ReportStatus status;
    private String adminNote;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime createdAt;

    public static PostReportResponse fromEntity(PostReport report) {
        return PostReportResponse.builder()
                .id(report.getId())
                .postId(report.getPost().getId())
                .postTitle(report.getPost().getPostTitle())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getFirstName() + " " + report.getReporter().getLastName())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .adminNote(report.getAdminNote())
                .reviewedById(report.getReviewedBy() != null ? report.getReviewedBy().getId() : null)
                .reviewedByName(report.getReviewedBy() != null ? 
                    report.getReviewedBy().getFirstName() + " " + report.getReviewedBy().getLastName() : null)
                .createdAt(report.getCreatedAt())
                .build();
    }
}
