package korastudy.be.entity.FlashCard;

import jakarta.persistence.*;
import lombok.*;
import korastudy.be.entity.User.User;

@Entity
@Table(name = "user_card_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    private Boolean isKnown; // true: đã thuộc, false: chưa thuộc
}