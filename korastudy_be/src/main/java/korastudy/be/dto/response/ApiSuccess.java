package korastudy.be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiSuccess {
    private String message;

    public static ApiSuccess of(String message) {
        return new ApiSuccess(message);
    }
}