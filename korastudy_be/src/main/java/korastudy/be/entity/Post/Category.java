package korastudy.be.entity.Post;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "category")
public class Category extends BaseTimeEntity {

    @Id
    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "category_title")
    private String categoryTitle;

    @Column(name = "category_context")
    private String context;

    @ManyToMany(mappedBy = "categories")
    private List<Post> posts;


}
