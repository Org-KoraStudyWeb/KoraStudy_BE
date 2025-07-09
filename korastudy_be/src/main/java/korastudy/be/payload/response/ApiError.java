package korastudy.be.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
    private String message;

    public static ApiError of(String message) {
        return new ApiError(message);
    }
}
