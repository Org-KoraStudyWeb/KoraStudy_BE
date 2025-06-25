package korastudy.be.entity.FlashCard;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.User.User;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "set_card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetCard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "setCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards;
}
