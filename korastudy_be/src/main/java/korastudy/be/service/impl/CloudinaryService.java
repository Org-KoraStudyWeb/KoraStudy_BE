package korastudy.be.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import korastudy.be.service.IUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService implements IUploadService {

    private final Cloudinary cloudinary;

    // Cloudinary free tier limits: 10MB for documents/images, 100MB for videos
    private static final int MAX_FILE_SIZE_MB = 10;
    private static final int MAX_VIDEO_SIZE_MB = 100;

    // =========== OVERLOAD METHODS (KHÔNG CẦN TITLE) ===========

    @Override
    public String uploadImage(MultipartFile file) {
        // Gọi phương thức có title với title là filename
        String title = file != null && file.getOriginalFilename() != null ? file.getOriginalFilename() : "image_" + System.currentTimeMillis();
        return uploadImage(file, title);
    }

    @Override
    public String uploadVideo(MultipartFile file) {
        String title = file != null && file.getOriginalFilename() != null ? file.getOriginalFilename() : "video_" + System.currentTimeMillis();
        return uploadVideo(file, title);
    }

    @Override
    public String uploadDocument(MultipartFile file) {
        String title = file != null && file.getOriginalFilename() != null ? file.getOriginalFilename() : "document_" + System.currentTimeMillis();
        return uploadDocument(file, title);
    }

    @Override
    public String uploadAudio(MultipartFile file) {
        String title = file != null && file.getOriginalFilename() != null ? file.getOriginalFilename() : "audio_" + System.currentTimeMillis();
        return uploadAudio(file, title);
    }

    // =========== ORIGINAL METHODS (VỚI TITLE) ===========

    @Override
    public String uploadImage(MultipartFile file, String title) {
        validateFile(file, "image", MAX_FILE_SIZE_MB);

        try {
            String publicId = generateSafeFileNameForAllTypes(file.getOriginalFilename(), title);

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "image", "folder", "korastudy/course-images", "public_id", publicId, "transformation", "q_auto", "use_filename", false, "unique_filename", false, "overwrite", true);

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info("Image uploaded: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Image upload failed", e);
            throw new RuntimeException("Lỗi khi upload image: " + e.getMessage());
        }
    }

    @Override
    public String uploadVideo(MultipartFile file, String title) {
        validateFile(file, "video", MAX_VIDEO_SIZE_MB);

        try {
            String publicId = generateSafeFileNameForAllTypes(file.getOriginalFilename(), title);

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "video", "folder", "korastudy/course-videos", "public_id", publicId, "quality", "auto", "chunk_size", 6000000, "use_filename", false, "unique_filename", false, "overwrite", true);

            log.info("Uploading video: {} ({} MB)", file.getOriginalFilename(), file.getSize() / (1024 * 1024));

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();

            log.info("Video uploaded: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Video upload failed: {}", file.getOriginalFilename(), e);
            
            // Check for specific error types
            if (e.getMessage() != null) {
                if (e.getMessage().contains("File size too large")) {
                    throw new RuntimeException("Video quá lớn! Cloudinary free tier chỉ hỗ trợ video tối đa 100MB. " +
                            "Vui lòng nén video hoặc nâng cấp Cloudinary account. Chi tiết: " + e.getMessage());
                } else if (e.getMessage().contains("timeout") || e.getMessage().contains("Timeout")) {
                    throw new RuntimeException("Upload video timeout - File quá lớn hoặc kết nối chậm. " +
                            "Vui lòng thử lại với file nhỏ hơn hoặc kiểm tra kết nối mạng.");
                }
            }
            
            throw new RuntimeException("Lỗi khi upload video: " + e.getMessage());
        }
    }

    @Override
    public String uploadDocument(MultipartFile file, String title) {
        validateFile(file, "document", MAX_FILE_SIZE_MB);

        try {
            String originalFilename = file.getOriginalFilename();

            // Tạo public_id từ filename (đã được frontend xử lý)
            String publicId = generateSafeFileNameForAllTypes(originalFilename, title);

            log.info("Document detected: {}, using safe name: {}", originalFilename, publicId);

            // TẠO UPLOAD PARAMS - KHÔNG phân biệt PDF nữa, upload tất cả dưới dạng raw
            Map<String, Object> uploadParams = new HashMap<>();

            // Luôn dùng resource_type raw cho documents
            uploadParams.put("resource_type", "raw");
            uploadParams.put("folder", "korastudy/course-documents");
            uploadParams.put("public_id", publicId); // Giữ nguyên extension

            // Xác định content type
            String contentType = determineContentType(originalFilename);
            if (contentType != null) {
                uploadParams.put("content_type", contentType);
            }

            // COMMON PARAMS
            uploadParams.put("use_filename", false);
            uploadParams.put("unique_filename", false);
            uploadParams.put("overwrite", true);
            uploadParams.put("invalidate", true);

            log.info("Uploading Document: {} -> {}", originalFilename, publicId);

            // UPLOAD
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();

            log.info("Document uploaded successfully: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Document upload failed for file: {}", file.getOriginalFilename(), e);
            
            // Check if error is due to file size limit
            if (e.getMessage() != null && e.getMessage().contains("File size too large")) {
                throw new RuntimeException("File quá lớn! Cloudinary free tier chỉ hỗ trợ file tối đa 10MB. " +
                        "Vui lòng nén file hoặc nâng cấp Cloudinary account. Chi tiết: " + e.getMessage());
            }
            
            throw new RuntimeException("Lỗi khi upload document: " + e.getMessage());
        }
    }

    @Override
    public String uploadAudio(MultipartFile file, String title) {
        validateFile(file, "audio", MAX_FILE_SIZE_MB);

        try {
            String publicId = generateSafeFileNameForAllTypes(file.getOriginalFilename(), title);

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "video", // Cloudinary xử lý audio như video
                    "folder", "korastudy/exam-audio", "public_id", publicId, "use_filename", false, "unique_filename", false, "overwrite", true);

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();

            log.info("Audio uploaded: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Audio upload failed", e);
            throw new RuntimeException("Lỗi khi upload audio: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String publicId = extractPublicIdFromUrl(fileUrl);

            if (publicId != null) {
                String resourceType = determineResourceType(fileUrl);

                log.info("Deleting file: {}, Public ID: {}", fileUrl, publicId);

                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));

                log.info("File deleted: {}", publicId);
            } else {
                log.warn("Cannot extract public_id from URL: {}", fileUrl);
                throw new RuntimeException("Không thể xác định public_id từ URL: " + fileUrl);
            }
        } catch (IOException e) {
            log.error("File deletion failed", e);
            throw new RuntimeException("Lỗi khi xóa file từ Cloudinary: " + e.getMessage());
        }
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * Tạo tên file an toàn cho tất cả loại (PDF, DOCX, DOC, v.v.)
     * Đã được frontend xử lý, backend chỉ đảm bảo an toàn
     */
    private String generateSafeFileNameForAllTypes(String originalFilename, String title) {
        String filenameToProcess = originalFilename;

        // Nếu không có filename, dùng title
        if (filenameToProcess == null || filenameToProcess.trim().isEmpty()) {
            filenameToProcess = title;
        }

        // Nếu vẫn không có, tạo tên ngẫu nhiên
        if (filenameToProcess == null || filenameToProcess.trim().isEmpty()) {
            return "file_" + UUID.randomUUID().toString().substring(0, 8);
        }

        // BƯỚC 1: Loại bỏ extension trùng trước khi xử lý
        filenameToProcess = fixDuplicateExtension(filenameToProcess);

        // BƯỚC 2: Xử lý bình thường
        return processFilename(filenameToProcess);
    }

    /**
     * Fix duplicate extensions (e.g., .mp4.mp4 -> .mp4)
     */
    private String fixDuplicateExtension(String filename) {
        if (filename == null) return filename;

        // Tìm tất cả các dấu chấm
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex <= 0) return filename; // Không có extension

        int secondLastDotIndex = filename.lastIndexOf('.', lastDotIndex - 1);
        if (secondLastDotIndex <= 0) return filename; // Chỉ có 1 dấu chấm

        // Lấy 2 extension cuối cùng
        String lastExtension = filename.substring(lastDotIndex).toLowerCase();
        String secondLastExtension = filename.substring(secondLastDotIndex, lastDotIndex).toLowerCase();

        // Nếu 2 extension giống nhau
        if (lastExtension.equals(secondLastExtension)) {
            // Xóa extension trùng
            String fixedName = filename.substring(0, secondLastDotIndex) + lastExtension;
            log.info("Fixed duplicate extension: {} -> {}", filename, fixedName);
            return fixedName;
        }

        return filename;
    }

    /**
     * Process filename after fixing duplicates
     */
    private String processFilename(String filename) {
        String nameWithoutExt = filename;
        String extension = "";

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            extension = filename.substring(lastDotIndex).toLowerCase();
            nameWithoutExt = filename.substring(0, lastDotIndex);
        }

        // Chuẩn hóa tên file
        String normalizedName = normalizeVietnameseIfNeeded(nameWithoutExt);

        String safeName = normalizedName.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("[\\s]+", "_").replaceAll("_{2,}", "_").replaceAll("^_+|_+$", "").toLowerCase().trim();

        if (safeName.isEmpty()) {
            safeName = "file_" + UUID.randomUUID().toString().substring(0, 8);
        }

        int maxNameLength = 80;
        if (safeName.length() > maxNameLength) {
            safeName = safeName.substring(0, maxNameLength);
        }

        return safeName + extension;
    }

    /**
     * Chuẩn hóa tiếng Việt - bỏ dấu
     */
    private String normalizeVietnameseIfNeeded(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        try {
            // Kiểm tra xem có chứa ký tự tiếng Việt có dấu không
            boolean hasVietnameseAccents = input.matches(".*[áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ].*");

            if (!hasVietnameseAccents) {
                return input; // Không cần xử lý
            }

            // Bỏ dấu tiếng Việt
            String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
            temp = temp.replaceAll("\\p{M}", ""); // Loại bỏ các ký tự dấu
            temp = temp.replace('đ', 'd').replace('Đ', 'D');
            return temp;
        } catch (Exception e) {
            log.warn("Failed to normalize Vietnamese text: {}", input, e);
            // Fallback: chỉ giữ chữ cái, số, dấu gạch dưới
            return input.replaceAll("[^a-zA-Z0-9\\s]", "_");
        }
    }

    /**
     * Xác định content type dựa trên extension
     */
    private String determineContentType(String filename) {
        if (filename == null) return null;

        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFilename.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerFilename.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerFilename.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerFilename.endsWith(".ppt") || lowerFilename.endsWith(".pptx")) {
            return "application/vnd.ms-powerpoint";
        } else if (lowerFilename.endsWith(".xls") || lowerFilename.endsWith(".xlsx")) {
            return "application/vnd.ms-excel";
        } else if (lowerFilename.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerFilename.endsWith(".rar")) {
            return "application/x-rar-compressed";
        } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lowerFilename.endsWith(".wav")) {
            return "audio/wav";
        } else if (lowerFilename.endsWith(".mp4")) {
            return "video/mp4";
        }

        return "application/octet-stream"; // default
    }

    /**
     * Extract public_id từ URL
     */
    private String extractPublicIdFromUrl(String url) {
        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String pathAfterUpload = parts[1];

            int queryIndex = pathAfterUpload.indexOf('?');
            if (queryIndex > 0) {
                pathAfterUpload = pathAfterUpload.substring(0, queryIndex);
            }

            int korastudyIndex = pathAfterUpload.indexOf("korastudy/");
            if (korastudyIndex == -1) {
                return null;
            }

            return pathAfterUpload.substring(korastudyIndex);

        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", url, e);
            return null;
        }
    }

    /**
     * Xác định resource type từ URL
     */
    private String determineResourceType(String url) {
        if (url.contains("/image/upload/")) {
            return "image";
        } else if (url.contains("/video/upload/")) {
            return "video";
        } else if (url.contains("/raw/upload/")) {
            return "raw";
        } else {
            return "image"; // default
        }
    }

    /**
     * Validate file
     */
    private void validateFile(MultipartFile file, String fileType, int maxSizeMB) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên file không được để trống");
        }

        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(String.format("%s không được vượt quá %dMB", fileType.substring(0, 1).toUpperCase() + fileType.substring(1), maxSizeMB));
        }

        if ("video".equals(fileType)) {
            if (!isVideoFile(file)) {
                throw new IllegalArgumentException("Chỉ chấp nhận video định dạng MP4, AVI, MOV, WMV, MKV");
            }
        } else if ("image".equals(fileType)) {
            if (!isImageFile(file)) {
                throw new IllegalArgumentException("Chỉ chấp nhận ảnh định dạng JPG, PNG, GIF, WEBP");
            }
        } else if ("audio".equals(fileType)) {
            if (!isAudioFile(file)) {
                throw new IllegalArgumentException("Chỉ chấp nhận audio định dạng MP3, WAV, M4A");
            }
        }
        // Document không check cụ thể vì có nhiều loại

        log.debug("File validated: {} ({} MB)", originalFilename, file.getSize() / (1024 * 1024));
    }

    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename().toLowerCase();
        return (contentType != null && contentType.startsWith("video/")) || fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mov") || fileName.endsWith(".wmv") || fileName.endsWith(".mkv") || fileName.endsWith(".flv");
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename().toLowerCase();
        return (contentType != null && contentType.startsWith("image/")) || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".webp") || fileName.endsWith(".bmp");
    }

    private boolean isAudioFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename().toLowerCase();
        return (contentType != null && contentType.startsWith("audio/")) || fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".m4a") || fileName.endsWith(".ogg") || fileName.endsWith(".flac");
    }
}