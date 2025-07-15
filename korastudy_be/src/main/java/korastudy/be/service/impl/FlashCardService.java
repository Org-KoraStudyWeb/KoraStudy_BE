package korastudy.be.service.impl;

import korastudy.be.dto.request.flashcard.CardRequest;
import korastudy.be.dto.request.flashcard.SetCardRequest;
import korastudy.be.dto.request.flashcard.UserCardProgressRequest;
import korastudy.be.entity.FlashCard.Card;
import korastudy.be.entity.FlashCard.SetCard;
import korastudy.be.entity.FlashCard.UserCardProgress;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.UserRepository;
import korastudy.be.repository.flashcard.CardRepository;
import korastudy.be.repository.flashcard.SetCardRepository;
import korastudy.be.repository.flashcard.UserCardProgressRepository;
import korastudy.be.service.IFlashCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FlashCardService implements IFlashCardService {

    private final CardRepository cardRepository;
    private final SetCardRepository setCardRepository;
    private final UserCardProgressRepository progressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserFlashcardSets(String username) {
        User user = getUserByUsername(username);
        List<SetCard> userSets = setCardRepository.findByUser(user);

        return userSets.stream().map(set -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", set.getId());
            data.put("title", set.getTitle());
            data.put("description", set.getDescription());
            data.put("category", set.getCategory());
            data.put("cardCount", set.getCards().size());
            return data;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSystemFlashcardSets() {
        List<SetCard> systemSets = setCardRepository.findByUserIsNull();

        return systemSets.stream().map(set -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", set.getId());
            data.put("title", set.getTitle());
            data.put("description", set.getDescription());
            data.put("category", set.getCategory());
            data.put("cardCount", set.getCards().size());
            return data;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFlashcardSetDetail(Long setId, String username) {
        User user = getUserByUsername(username);
        SetCard setCard = getSetCardById(setId);

        List<UserCardProgress> progresses = progressRepository.findByUserAndCard_SetCard(user, setCard);

        Map<Long, Boolean> progressMap = progresses.stream()
                .collect(Collectors.toMap(
                        p -> p.getCard().getId(),
                        UserCardProgress::getIsKnown
                ));

        int total = setCard.getCards().size();
        int known = (int) progresses.stream().filter(UserCardProgress::getIsKnown).count();
        int percent = total > 0 ? (known * 100 / total) : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("id", setCard.getId());
        response.put("title", setCard.getTitle());
        response.put("description", setCard.getDescription());
        response.put("category", setCard.getCategory());
        response.put("isPublic", setCard.getUser() == null);

        Map<String, Object> progress = new HashMap<>();
        progress.put("total", total);
        progress.put("known", known);
        progress.put("percent", percent);
        response.put("progress", progress);

        List<Map<String, Object>> cards = setCard.getCards().stream().map(card -> {
            Map<String, Object> c = new HashMap<>();
            c.put("id", card.getId());
            c.put("term", card.getTerm());
            c.put("definition", card.getDefinition());
            c.put("example", card.getExample());
            c.put("imageUrl", card.getImageUrl());
            c.put("isKnown", progressMap.getOrDefault(card.getId(), false));
            return c;
        }).collect(Collectors.toList());

        response.put("cards", cards);
        return response;
    }

    @Override
    public void updateProgress(UserCardProgressRequest request, String username) {
        User user = getUserByUsername(username);
        Card card = getCardById(request.getCardId());

        UserCardProgress progress = progressRepository.findByUserAndCard(user, card)
                .orElse(UserCardProgress.builder().user(user).card(card).build());

        progress.setIsKnown(request.getIsKnown());
        progressRepository.save(progress);
    }

    @Override
    public void createUserFlashcardSet(SetCardRequest request, String username) {
        User user = getUserByUsername(username);

        SetCard setCard = SetCard.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .user(user)
                .build();

        List<Card> cards = request.getCards().stream().map(c -> Card.builder()
                .term(c.getTerm())
                .definition(c.getDefinition())
                .example(c.getExample())
                .imageUrl(c.getImageUrl())
                .setCard(setCard)
                .build()).collect(Collectors.toList());

        setCard.setCards(cards);
        setCardRepository.save(setCard);
    }

    @Override
    public void createSystemFlashcardSet(SetCardRequest request) {
        SetCard setCard = SetCard.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .user(null)  // NULL => flashcard hệ thống
                .build();

        List<Card> cards = request.getCards().stream().map(c -> Card.builder()
                .term(c.getTerm())
                .definition(c.getDefinition())
                .example(c.getExample())
                .imageUrl(c.getImageUrl())
                .setCard(setCard)
                .build()).collect(Collectors.toList());

        setCard.setCards(cards);
        setCardRepository.save(setCard);
    }

    @Override
    public void deleteFlashcardSet(Long setId, String username) {
        User user = getUserByUsername(username);
        SetCard setCard = getSetCardById(setId);

        // Kiểm tra quyền
        if (setCard.getUser() == null) {
            throw new AccessDeniedException("Không thể xoá bộ flashcard hệ thống");
        }
        if (!setCard.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xoá bộ này");
        }

        // Chỉ cần xoá SetCard ➜ Cascade xoá Card ➜ Cascade xoá UserCardProgress
        setCardRepository.delete(setCard);
    }

    @Override
    public void updateFlashcardSet(Long setId, SetCardRequest request, String username) {
        User user = getUserByUsername(username);
        SetCard setCard = getSetCardById(setId);

        if (!setCard.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền sửa bộ này");
        }

        setCard.setTitle(request.getTitle());
        setCard.setDescription(request.getDescription());
        setCard.setCategory(request.getCategory());

        // Xóa tiến trình cũ trước (nếu không Hibernate tự orphan)
        for (Card oldCard : setCard.getCards()) {
            oldCard.getProgresses().clear(); // orphanRemoval sẽ xoá progress
        }
        setCard.getCards().clear();

        for (CardRequest c : request.getCards()) {
            Card card = new Card();
            card.setTerm(c.getTerm());
            card.setDefinition(c.getDefinition());
            card.setExample(c.getExample());
            card.setImageUrl(c.getImageUrl());
            card.setSetCard(setCard);
            setCard.getCards().add(card);
        }

        setCardRepository.save(setCard);
    }

    // Helper methods
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private SetCard getSetCardById(Long setId) {
        return setCardRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("SetCard not found"));
    }

    private Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }
}