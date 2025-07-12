package korastudy.be.repository.flashcard;


import korastudy.be.entity.FlashCard.Card;
import korastudy.be.entity.FlashCard.SetCard;
import korastudy.be.entity.FlashCard.UserCardProgress;
import korastudy.be.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCardProgressRepository extends JpaRepository<UserCardProgress, Long> {
    List<UserCardProgress> findByUserAndCard_SetCard(User user, SetCard setCard);
    Optional<UserCardProgress> findByUserAndCard(User user, Card card);
}