package korastudy.be.controller;

import korastudy.be.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final IUploadService uploadService;

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