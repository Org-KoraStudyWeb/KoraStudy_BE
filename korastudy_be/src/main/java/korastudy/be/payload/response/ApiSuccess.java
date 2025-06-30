package korastudy.be.payload.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiSuccess {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String message;

    public static ApiSuccess of(String message) {
        return ApiSuccess.builder().status(201).message(message).build();
    }
}
