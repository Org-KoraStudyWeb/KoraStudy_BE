package korastudy.be.dto.request.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SystemNotificationRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    
    @NotBlank(message = "Nội dung không được để trống")
    private String content;
    
    // Để null nếu muốn gửi cho tất cả người dùng
    private List<Long> userIds;
}