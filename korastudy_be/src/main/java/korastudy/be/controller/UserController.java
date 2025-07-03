package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.UpdateManagerProfileRequest;
import korastudy.be.dto.request.UserProfileUpdate;
import korastudy.be.entity.User.User;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user/")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DELIVERY_MANAGER', 'CONTENT_MANAGER', 'USER')")
public class UserController {
    private final IUserService userService;

    @PutMapping("/update")
    public ResponseEntity<ApiSuccess> updateProfile(@Valid @RequestBody UpdateManagerProfileRequest request, Principal principal) {
        String username = principal.getName(); // từ JWT
        userService.updateProfileAndNotify(username, request);
        return ResponseEntity.ok(ApiSuccess.of("Cập nhật hồ sơ thành công, chờ admin duyệt"));
    }

    /**
     * Trung - Cập nhật thông tin hồ sơ của người dùng
     * @param userId ID của người dùng cần cập nhật
     * @param dto DTO chứa thông tin cập nhật
     * @return ResponseEntity với thông tin người dùng đã cập nhật
     */
    @PutMapping("/profile/{id}")
    public ResponseEntity<User> updateProfile(
            @PathVariable("id") Long userId,
            @RequestBody UserProfileUpdate dto
    ) {
        User updatedUser = userService.updateProfile(userId, dto);
        return ResponseEntity.ok(updatedUser);
    }
}
