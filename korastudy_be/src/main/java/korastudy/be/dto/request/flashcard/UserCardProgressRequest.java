package korastudy.be.dto.request.flashcard;

import lombok.Data;

@Data
public class UserCardProgressRequest {
    private Long cardId;
    private Boolean isKnown;
}

