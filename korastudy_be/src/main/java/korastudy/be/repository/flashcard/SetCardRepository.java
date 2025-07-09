package korastudy.be.repository.flashcard;

import korastudy.be.entity.FlashCard.SetCard;
import korastudy.be.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetCardRepository extends JpaRepository<SetCard, Long> {
    List<SetCard> findByUserIsNull();
    List<SetCard> findByUser(User user);
}