package korastudy.be.controller;

import korastudy.be.dto.request.flashcard.CardRequest;
import korastudy.be.dto.request.flashcard.SetCardRequest;
import korastudy.be.dto.request.flashcard.UserCardProgressRequest;
import korastudy.be.entity.FlashCard.Card;
import korastudy.be.entity.FlashCard.SetCard;
import korastudy.be.entity.FlashCard.UserCardProgress;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.payload.response.ApiError;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.repository.UserRepository;
import korastudy.be.repository.flashcard.CardRepository;
import korastudy.be.repository.flashcard.SetCardRepository;
import korastudy.be.repository.flashcard.UserCardProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor

public class FlashCardController {

    private final CardRepository cardRepository;
    private final SetCardRepository setCardRepository;
    private final UserCardProgressRepository progressRepository;
    private final UserRepository userRepository;

    /**
     *  Lấy danh sách bộ flashcard của user đang đăng nhập
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserFlashcardSets(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<SetCard> userSets = setCardRepository.findByUser(user);

        List<Map<String, Object>> response = userSets.stream().map(set -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", set.getId());
            data.put("title", set.getTitle());
            data.put("description", set.getDescription());
            data.put("category", set.getCategory());
            data.put("cardCount", set.getCards().size());
            return data;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     *  Lấy danh sách bộ flashcard hệ thống (không gán user)
     */
    @GetMapping("/system")
    public ResponseEntity<?> getSystemFlashcardSets() {
        List<SetCard> systemSets = setCardRepository.findByUserIsNull();

        List<Map<String, Object>> response = systemSets.stream().map(set -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", set.getId());
            data.put("title", set.getTitle());
            data.put("description", set.getDescription());
            data.put("category", set.getCategory());
            data.put("cardCount", set.getCards().size());
            return data;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     *  Xem chi tiết 1 bộ flashcard
     */
    @GetMapping("/{setId}")
    public ResponseEntity<?> getFlashcardSet(
            @PathVariable Long setId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        SetCard setCard = setCardRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("SetCard not found"));

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

        return ResponseEntity.ok(response);
    }

    /**
     *  Cập nhật trạng thái progress của user với 1 card
     */
    @PatchMapping("/progress")
    public ResponseEntity<?> updateProgress(
            @RequestBody UserCardProgressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        UserCardProgress progress = progressRepository.findByUserAndCard(user, card)
                .orElse(UserCardProgress.builder().user(user).card(card).build());

        progress.setIsKnown(request.getIsKnown());
        progressRepository.save(progress);

        return ResponseEntity.ok(ApiSuccess.of("Progress updated successfully!"));
    }

    /**
     *  Tạo bộ flashcard cho user
     */
    @PostMapping("")
    public ResponseEntity<?> createSetCard(
            @RequestBody SetCardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

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

        return ResponseEntity.ok(ApiSuccess.of("Flashcard set created successfully!"));
    }

    /**
     *  Tạo bộ flashcard hệ thống (admin)
     */
    @PostMapping("/system")
    public ResponseEntity<?> createSystemFlashcardSet(@RequestBody SetCardRequest request) {
        SetCard setCard = SetCard.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .user(null)  // RẤT QUAN TRỌNG: NULL => flashcard hệ thống
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

        return ResponseEntity.ok(ApiSuccess.of("Flashcard system set created successfully!"));
    }

    /**
     *  Xóa bộ flashcard của user
     */
    @DeleteMapping("/{setId}")
    public ResponseEntity<?> deleteSetCard(
            @PathVariable Long setId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Lấy user hiện tại
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Lấy SetCard
        SetCard setCard = setCardRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("SetCard not found"));

        // Kiểm tra quyền
        if (setCard.getUser() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể xoá bộ flashcard hệ thống");
        }
        if (!setCard.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền xoá bộ này");
        }

        //  Chỉ cần xoá SetCard ➜ Cascade xoá Card ➜ Cascade xoá UserCardProgress
        setCardRepository.delete(setCard);

        return ResponseEntity.ok(ApiSuccess.of("Đã xoá bộ flashcard thành công!"));
    }


    /**
     *  Cập nhật bộ flashcard của user
     */
    @PutMapping("/{setId}")
    public ResponseEntity<?> updateSetCard(
            @PathVariable Long setId,
            @RequestBody SetCardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        SetCard setCard = setCardRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("SetCard not found"));

        if (!setCard.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiError.of("Bạn không có quyền sửa bộ này"));
        }

        setCard.setTitle(request.getTitle());
        setCard.setDescription(request.getDescription());
        setCard.setCategory(request.getCategory());

        //  Xóa tiến trình cũ trước (nếu không Hibernate tự orphan)
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

        return ResponseEntity.ok(ApiSuccess.of("Đã cập nhật bộ flashcard thành công!"));
    }

}
