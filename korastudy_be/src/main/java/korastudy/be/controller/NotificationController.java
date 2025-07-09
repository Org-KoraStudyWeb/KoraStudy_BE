package korastudy.be.controller;

import korastudy.be.entity.Notification;
import korastudy.be.entity.User.User;
import korastudy.be.exception.AlreadyExistsException;
import korastudy.be.service.impl.NotificationService;
import korastudy.be.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<List<Notification>> getMyNotifications(Principal principal) {
        String username = principal.getName(); // lấy từ token đã xác thực
        User user = userService.getUserByAccountUsername(username)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy thông tin người dùng"));
        return ResponseEntity.ok(notificationService.getNotificationsForUser(user.getId()));
    }
}
