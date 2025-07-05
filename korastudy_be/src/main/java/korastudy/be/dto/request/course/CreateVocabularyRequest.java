package korastudy.be.dto.request.course;

import lombok.Data;

@Data
public class CreateVocabularyRequest {
    private String word;

    private String meaning;

    private String level;

    private String imageUrl;

    private String audioUrl;

    private Long topicId;
}
