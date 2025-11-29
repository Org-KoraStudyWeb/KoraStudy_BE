package korastudy.be.controller;

import korastudy.be.service.impl.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    // FE: formData.append("file", file)
    @PostMapping("/course-image")
    public ResponseEntity<?> uploadCourseImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = cloudinaryService.uploadImage(file); // mặc định folder bạn set trong service
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Upload failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String url = cloudinaryService.uploadImage(file); // Chỉ truyền file, không truyền folder
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Upload avatar failed", "message", e.getMessage()));
        }
    }

    // (tuỳ chọn) upload audio
    @PostMapping("/audio")
    public ResponseEntity<?> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            String url = cloudinaryService.uploadAudio(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
