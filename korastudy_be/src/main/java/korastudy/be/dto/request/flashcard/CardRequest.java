package korastudy.be.dto.request.flashcard;

import lombok.Data;

@Data
public class CardRequest {
    private String term;
    private String definition;
    private String example;
    private String imageUrl;
}