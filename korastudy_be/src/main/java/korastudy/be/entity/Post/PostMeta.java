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
    @Column(name = "post_meta_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private String key;

    private String postMetaContext;

}
