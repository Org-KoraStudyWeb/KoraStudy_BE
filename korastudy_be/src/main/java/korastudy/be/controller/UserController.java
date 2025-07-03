package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.auth.UpdateManagerProfileRequest;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user/profile")
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
}
