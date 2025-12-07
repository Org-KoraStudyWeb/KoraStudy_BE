package korastudy.be.service;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {

    // =========== METHODS VỚI TITLE (CHO BACKWARD COMPATIBLE) ===========
    String uploadImage(MultipartFile file, String title);

    String uploadVideo(MultipartFile file, String title);

    String uploadDocument(MultipartFile file, String title);

    String uploadAudio(MultipartFile file, String title);

    // =========== METHODS KHÔNG CẦN TITLE (NEW) ===========
    String uploadImage(MultipartFile file);

    String uploadVideo(MultipartFile file);

    String uploadDocument(MultipartFile file);

    String uploadAudio(MultipartFile file);

    void deleteFile(String fileUrl);
}