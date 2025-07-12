package korastudy.be.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor // ✅ Bắt buộc để Builder hoạt động ngoài package!
public class ApiSuccess {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String message;

    public static ApiSuccess of(String message) {
        return ApiSuccess.builder()
                .status(201)
                .message(message)
                .build();
    }
}
