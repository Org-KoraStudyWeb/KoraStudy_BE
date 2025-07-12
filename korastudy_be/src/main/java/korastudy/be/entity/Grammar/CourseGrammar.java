package korastudy.be.entity.Grammar;

import jakarta.persistence.*;
import korastudy.be.entity.Course.Course;
import lombok.*;

@Entity
@Table(name = "course_grammar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseGrammar {

    @EmbeddedId
    private CourseGrammarId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("grammarId")
    @JoinColumn(name = "grammar_id")
    private Grammar grammar;
}
