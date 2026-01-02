package korastudy.be.dto.request.blog;

import korastudy.be.entity.Enum.ReportStatus;
import lombok.Data;

@Data
public class ReviewReportRequest {
    private ReportStatus status;
    private String adminNote;
}
