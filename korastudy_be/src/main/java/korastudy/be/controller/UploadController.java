package korastudy.be.controller;

import korastudy.be.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final IUploadService uploadService;

    // ============ AVATAR UPLOAD (CẦN THÊM) ============
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Lấy thông tin user từ authentication
            String username = userDetails.getUsername();

            // Tạo title unique cho avatar (dùng username + timestamp)
            String title = "avatar_" + username + "_" + System.currentTimeMillis();

            // Upload ảnh avatar
            String url = uploadService.uploadImage(file, title);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Upload avatar thành công",
                    "url", url,
                    "type", "AVATAR"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // ============ EXISTING ENDPOINTS ============
    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = uploadService.uploadImage(file);
            return ResponseEntity.ok(Map.of("url", url, "type", "IMAGE"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/video")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String url = uploadService.uploadVideo(file);
            return ResponseEntity.ok(Map.of("url", url, "type", "VIDEO"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/document")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String url = uploadService.uploadDocument(file);
            return ResponseEntity.ok(Map.of("url", url, "type", "DOCUMENT"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/audio")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<?> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            String url = uploadService.uploadAudio(file);
            return ResponseEntity.ok(Map.of("url", url, "type", "AUDIO"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<?> deleteFile(@RequestParam String fileUrl) {
        try {
            uploadService.deleteFile(fileUrl);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}