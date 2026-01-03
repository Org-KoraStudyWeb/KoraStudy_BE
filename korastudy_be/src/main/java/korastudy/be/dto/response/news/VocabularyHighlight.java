package korastudy.be.dto.response.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabularyHighlight {
    private Long id;
    private String koreanWord;
    private String vietnameseMeaning;
    private String romanization;
    private String wordType;
    private String topikLevel;
    private Integer positionStart;
    private Integer positionEnd;
    private String contextSentence;
    private Boolean isInFlashcard; // Already saved to flashcard?
}
