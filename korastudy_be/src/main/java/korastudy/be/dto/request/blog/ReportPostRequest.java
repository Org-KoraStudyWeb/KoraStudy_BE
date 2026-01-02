package korastudy.be.dto.request.blog;

import lombok.Data;

@Data
public class ReportPostRequest {
    private String reason;
    private String description;
}
