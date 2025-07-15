package korastudy.be.service;

import korastudy.be.dto.request.flashcard.CardRequest;
import korastudy.be.dto.request.flashcard.SetCardRequest;
import korastudy.be.dto.request.flashcard.UserCardProgressRequest;
import korastudy.be.entity.User.User;

import java.util.List;
import java.util.Map;

public interface IFlashCardService {

    // Lấy danh sách bộ flashcard của user
    List<Map<String, Object>> getUserFlashcardSets(String username);

    // Lấy danh sách bộ flashcard hệ thống
    List<Map<String, Object>> getSystemFlashcardSets();

    // Xem chi tiết 1 bộ flashcard
    Map<String, Object> getFlashcardSetDetail(Long setId, String username);

    // Cập nhật trạng thái progress của user với 1 card
    void updateProgress(UserCardProgressRequest request, String username);

    // Tạo bộ flashcard cho user
    void createUserFlashcardSet(SetCardRequest request, String username);

    // Tạo bộ flashcard hệ thống (admin)
    void createSystemFlashcardSet(SetCardRequest request);

    // Xóa bộ flashcard của user
    void deleteFlashcardSet(Long setId, String username);

    // Cập nhật bộ flashcard của user
    void updateFlashcardSet(Long setId, SetCardRequest request, String username);
}