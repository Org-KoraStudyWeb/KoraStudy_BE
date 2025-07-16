package korastudy.be.controller;

import korastudy.be.dto.request.flashcard.SetCardRequest;
import korastudy.be.dto.request.flashcard.UserCardProgressRequest;
import korastudy.be.payload.response.ApiError;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.IFlashCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashCardController {

    private final IFlashCardService flashCardService;

    /**
     * Lấy danh sách bộ flashcard của user đang đăng nhập
     */
    @GetMapping("/user")
    public ResponseEntity<List<Map<String, Object>>> getUserFlashcardSets(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Map<String, Object>> response = flashCardService.getUserFlashcardSets(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }



    /**
     * Xem chi tiết 1 bộ flashcard
     */
    @GetMapping("/{setId}")
    public ResponseEntity<Map<String, Object>> getFlashcardSet(
            @PathVariable Long setId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = flashCardService.getFlashcardSetDetail(setId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật trạng thái progress của user với 1 card
     */
    @PatchMapping("/progress")
    public ResponseEntity<ApiSuccess> updateProgress(
            @RequestBody UserCardProgressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        flashCardService.updateProgress(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiSuccess.of("Progress updated successfully!"));
    }

    /**
     * Tạo bộ flashcard cho user
     */
    @PostMapping("")
    public ResponseEntity<ApiSuccess> createSetCard(
            @RequestBody SetCardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        flashCardService.createUserFlashcardSet(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiSuccess.of("Flashcard set created successfully!"));
    }

    /**
     * Xóa bộ flashcard của user
     */
    @DeleteMapping("/{setId}")
    public ResponseEntity<?> deleteSetCard(
            @PathVariable Long setId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            flashCardService.deleteFlashcardSet(setId, userDetails.getUsername());
            return ResponseEntity.ok(ApiSuccess.of("Đã xoá bộ flashcard thành công!"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        }
    }

    /**
     * Cập nhật bộ flashcard của user
     */
    @PutMapping("/{setId}")
    public ResponseEntity<?> updateSetCard(
            @PathVariable Long setId,
            @RequestBody SetCardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            flashCardService.updateFlashcardSet(setId, request, userDetails.getUsername());
            return ResponseEntity.ok(ApiSuccess.of("Đã cập nhật bộ flashcard thành công!"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        }
    }

    /**
     * Lấy danh sách bộ flashcard hệ thống (không gán user)
     */
    @GetMapping("/system")
    public ResponseEntity<List<Map<String, Object>>> getSystemFlashcardSets() {
        List<Map<String, Object>> response = flashCardService.getSystemFlashcardSets();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Tạo bộ flashcard hệ thống (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/system")
    public ResponseEntity<ApiSuccess> createSystemFlashcardSet(@RequestBody SetCardRequest request) {
        flashCardService.createSystemFlashcardSet(request);
        return ResponseEntity.ok(ApiSuccess.of("Flashcard system set created successfully!"));
    }

    /**
     * Cập nhật bộ flashcard hệ thống (chỉ admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/system/{setId}")
    public ResponseEntity<?> updateSystemFlashcardSet(
            @PathVariable Long setId,
            @RequestBody SetCardRequest request) {
        try {
            flashCardService.updateSystemFlashcardSet(setId, request);
            return ResponseEntity.ok(ApiSuccess.of("Đã cập nhật bộ flashcard hệ thống thành công!"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        }
    }

    /**
     * Xóa bộ flashcard hệ thống (chỉ admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/system/{setId}")
    public ResponseEntity<?> deleteSystemFlashcardSet(@PathVariable Long setId) {
        try {
            flashCardService.deleteSystemFlashcardSet(setId);
            return ResponseEntity.ok(ApiSuccess.of("Đã xóa bộ flashcard hệ thống thành công!"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        }
    }

}