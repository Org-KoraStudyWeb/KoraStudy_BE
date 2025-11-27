package korastudy.be.service;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {

    // Method với title
    String uploadImage(MultipartFile file, String title);

    String uploadVideo(MultipartFile file, String title);

    String uploadDocument(MultipartFile file, String title);

    String uploadAudio(MultipartFile file, String title);

    // ✅ THÊM: Overload method không cần title (backward compatible)
    default String uploadImage(MultipartFile file) {
        return uploadImage(file, file != null ? file.getOriginalFilename() : "untitled");
    }

    default String uploadVideo(MultipartFile file) {
        return uploadVideo(file, file != null ? file.getOriginalFilename() : "untitled");
    }

    default String uploadDocument(MultipartFile file) {
        return uploadDocument(file, file != null ? file.getOriginalFilename() : "untitled");
    }

    default String uploadAudio(MultipartFile file) {
        return uploadAudio(file, file != null ? file.getOriginalFilename() : "untitled");
    }

    void deleteFile(String fileUrl);
}