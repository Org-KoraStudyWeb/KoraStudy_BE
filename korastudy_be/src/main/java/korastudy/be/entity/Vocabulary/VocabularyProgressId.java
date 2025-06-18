package korastudy.be.entity.Vocabulary;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyProgressId implements Serializable {
    private String user;
    private String vocabulary;
    private String topic;
}
