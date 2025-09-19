package korastudy.be.entity.FlashCard;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.User.User;
import lombok.*;

import java.util.ArrayList;
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

    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String category;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @OneToMany(mappedBy = "setCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();
}

