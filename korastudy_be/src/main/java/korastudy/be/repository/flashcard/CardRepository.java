package korastudy.be.repository.flashcard;


import korastudy.be.entity.FlashCard.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository

public interface CardRepository extends JpaRepository<Card, Long> {
}