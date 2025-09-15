package korastudy.be.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String message;
    private Object data;

    public static ApiResponse of(String message) {
        return ApiResponse.builder()
                .status(200)
                .message(message)
                .build();
    }

    public static ApiResponse of(String message, Object data) {
        return ApiResponse.builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }
    
    public static ApiResponse of(int status, String message) {
        return ApiResponse.builder()
                .status(status)
                .message(message)
                .build();
    }
    
    public static ApiResponse of(int status, String message, Object data) {
        return ApiResponse.builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }
}
