package korastudy.be.entity.Post;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_meta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private String meta_key;

    private String postMetaContext;

}
