package korastudy.be.dto.response.course;

import korastudy.be.dto.response.quiz.QuizDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionDTO {
    private Long id;
    private String sectionName;
    private Integer orderIndex;
    private List<LessonDTO> lessons;
    private List<QuizDTO> quizzes;
}
