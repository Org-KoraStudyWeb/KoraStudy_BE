package korastudy.be.dto.request.flashcard;

import lombok.Data;

import java.util.List;

@Data
public class SetCardRequest {
    private String title;
    private String description;
    private String category;
    private List<CardRequest> cards;
}