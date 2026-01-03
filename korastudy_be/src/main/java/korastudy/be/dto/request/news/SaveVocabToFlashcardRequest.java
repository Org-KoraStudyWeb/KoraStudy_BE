package korastudy.be.dto.request.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveVocabToFlashcardRequest {
    private Long articleId;
    private Long newsVocabId;
    private Long setCardId; // Flashcard set to save to
    private String word;
    private String meaning;
    private String example;
}
