package korastudy.be.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import korastudy.be.service.IUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService implements IUploadService {

    private final Cloudinary cloudinary;

    // Constants for configuration - SỬA THÀNH INTEGER
    private static final int UPLOAD_TIMEOUT_MS = 120000; // 2 phút - ✅ SỬA THÀNH int
    private static final int VIDEO_UPLOAD_TIMEOUT_MS = 300000; // 5 phút cho video - ✅ SỬA THÀNH int
    private static final int MAX_FILE_SIZE_MB = 500; // 500MB max
    private static final int MAX_VIDEO_SIZE_MB = 100; // 100MB cho video

    @Override
    public String uploadImage(MultipartFile file, String title) {
        validateFile(file, "image", MAX_FILE_SIZE_MB);

        try {
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "course-images");

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", "korastudy/course-images",
                    "public_id", publicId,
                    "transformation", "q_auto"
                    // ❌ XÓA timeout parameter từ đây
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info("✅ Image uploaded successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error("❌ Image upload failed", e);
            throw new RuntimeException("Lỗi khi upload image: " + e.getMessage());
        }
    }

    @Override
    public String uploadVideo(MultipartFile file, String title) {
        validateFile(file, "video", MAX_VIDEO_SIZE_MB);

        try {
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "course-videos");

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "video",
                    "folder", "korastudy/course-videos",
                    "public_id", publicId,
                    "quality", "auto",
                    "chunk_size", 6000000 // ✅ CHỈ GIỮ LẠI chunk_size
                    // ❌ XÓA timeout và eager_async
            );

            log.info("🚀 Starting video upload: {} (size: {} MB)",
                    file.getOriginalFilename(),
                    file.getSize() / (1024 * 1024));

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            // Kiểm tra cả secure_url và url thường
            String url = null;
            if (uploadResult.get("secure_url") != null) {
                url = uploadResult.get("secure_url").toString();
            } else if (uploadResult.get("url") != null) {
                url = uploadResult.get("url").toString();
            }

            if (url == null) {
                log.error("❌ Upload failed - no URL returned. Full result: {}", uploadResult);
                throw new RuntimeException("Upload failed: No URL returned");
            }

            log.info("✅ Video uploaded successfully: {}", url);
            return url;

        } catch (IOException e) {
            log.error("❌ Video upload failed for file: {}", file.getOriginalFilename(), e);

            // ✅ XỬ LÝ LỖI TIMEOUT CỤ THỂ
            if (e.getMessage().contains("timeout") || e.getMessage().contains("Timeout")) {
                throw new RuntimeException("Upload video timeout - File quá lớn hoặc kết nối chậm. Vui lòng thử lại với file nhỏ hơn.");
            }

            throw new RuntimeException("Lỗi khi upload video: " + e.getMessage());
        }
    }

    @Override
    public String uploadDocument(MultipartFile file, String title) {
        validateFile(file, "document", MAX_FILE_SIZE_MB);

        try {
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "course-documents");

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "raw",
                    "folder", "korastudy/course-documents",
                    "public_id", publicId
                    // ❌ XÓA timeout parameter
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info("✅ Document uploaded successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error("❌ Document upload failed", e);
            throw new RuntimeException("Lỗi khi upload document: " + e.getMessage());
        }
    }

    @Override
    public String uploadAudio(MultipartFile file, String title) {
        validateFile(file, "audio", MAX_FILE_SIZE_MB);

        try {
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "exam-audio");

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "video", // Cloudinary uses 'video' for audio files
                    "folder", "korastudy/exam-audio",
                    "public_id", publicId
                    // ❌ XÓA timeout parameter
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info("✅ Audio uploaded successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error("❌ Audio upload failed", e);
            throw new RuntimeException("Lỗi khi upload audio: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String publicId = extractPublicIdSimple(fileUrl);

            if (publicId != null) {
                String resourceType = determineResourceType(fileUrl);

                log.info("🗑️ Deleting file - URL: {}, Public ID: {}, Resource Type: {}", fileUrl, publicId, resourceType);

                Map result = cloudinary.uploader().destroy(publicId,
                        ObjectUtils.asMap("resource_type", resourceType));

                log.info("✅ File deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn("⚠️ Cannot extract public_id from URL: {}", fileUrl);
                throw new RuntimeException("Không thể xác định public_id từ URL: " + fileUrl);
            }
        } catch (IOException e) {
            log.error("❌ File deletion failed", e);
            throw new RuntimeException("Lỗi khi xóa file từ Cloudinary: " + e.getMessage());
        }
    }

    /**
     * ✅ THÊM PHƯƠNG THỨC VALIDATE FILE
     */
    private void validateFile(MultipartFile file, String fileType, int maxSizeMB) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    String.format("%s không được vượt quá %dMB",
                            fileType.substring(0, 1).toUpperCase() + fileType.substring(1),
                            maxSizeMB)
            );
        }

        // Validate file type
        if ("video".equals(fileType)) {
            if (!isVideoFile(file)) {
                throw new IllegalArgumentException("Chỉ chấp nhận video định dạng MP4, AVI, MOV, WMV");
            }
        } else if ("image".equals(fileType)) {
            if (!isImageFile(file)) {
                throw new IllegalArgumentException("Chỉ chấp nhận ảnh định dạng JPG, PNG, GIF");
            }
        } else if ("audio".equals(fileType)) {
            if (!isAudioFile(file)) {
                throw new IllegalArgumentException("Chỉ chấp nhận audio định dạng MP3, WAV");
            }
        }

        log.info("✅ File validated: {} (size: {} MB, type: {})",
                file.getOriginalFilename(),
                file.getSize() / (1024 * 1024),
                file.getContentType());
    }

    /**
     * ✅ THÊM VALIDATION METHODS
     */
    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename().toLowerCase();
        return (contentType != null && contentType.startsWith("video/")) ||
                fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
                fileName.endsWith(".mov") || fileName.endsWith(".wmv");
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private boolean isAudioFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename().toLowerCase();
        return (contentType != null && contentType.startsWith("audio/")) ||
                fileName.endsWith(".mp3") || fileName.endsWith(".wav");
    }

    /**
     * PHƯƠNG THỨC CŨ: Extract public_id từ URL
     */
    private String extractPublicIdSimple(String url) {
        try {
            log.debug("🔍 Extracting public_id from URL: {}", url);

            String[] parts = url.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String pathAfterUpload = parts[1];
            int korastudyIndex = pathAfterUpload.indexOf("korastudy/");
            if (korastudyIndex == -1) {
                return null;
            }

            String publicIdWithExtension = pathAfterUpload.substring(korastudyIndex);
            int lastDotIndex = publicIdWithExtension.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return publicIdWithExtension.substring(0, lastDotIndex);
            }

            return publicIdWithExtension;

        } catch (Exception e) {
            log.warn("⚠️ Failed to extract public_id from URL: {}", url, e);
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
     * PHƯƠNG THỨC CŨ: Generate public_id đơn giản (KHÔNG có timestamp)
     */
    private String generateSimplePublicId(String title, String originalFilename, String folder) {
        String baseName;

        if (title != null && !title.trim().isEmpty()) {
            baseName = sanitizeFileName(title);
        } else if (originalFilename != null) {
            String nameWithoutExt = originalFilename.replaceFirst("[.][^.]+$", "");
            baseName = sanitizeFileName(nameWithoutExt);
        } else {
            baseName = UUID.randomUUID().toString();
        }

        return baseName;
    }

    /**
     * Sanitize filename
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9-_]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "")
                .toLowerCase();
    }
}