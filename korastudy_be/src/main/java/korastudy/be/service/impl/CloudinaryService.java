package korastudy.be.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import korastudy.be.service.IUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService implements IUploadService {

    private final Cloudinary cloudinary;

    private static final int UPLOAD_TIMEOUT_MS = 120000;
    private static final int VIDEO_UPLOAD_TIMEOUT_MS = 300000;
    private static final int MAX_FILE_SIZE_MB = 500;
    private static final int MAX_VIDEO_SIZE_MB = 100;

    @Override
    public String uploadImage(MultipartFile file, String title) {
        validateFile(file, "image", MAX_FILE_SIZE_MB);

        try {
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "course-images");

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "image", "folder", "korastudy/course-images", "public_id", publicId, "transformation", "q_auto");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info("Image uploaded successfully: {}", url);
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
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "course-videos");

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "video", "folder", "korastudy/course-videos", "public_id", publicId, "quality", "auto", "chunk_size", 6000000);

            log.info("Starting video upload: {} (size: {} MB)", file.getOriginalFilename(), file.getSize() / (1024 * 1024));

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String url = null;
            if (uploadResult.get("secure_url") != null) {
                url = uploadResult.get("secure_url").toString();
            } else if (uploadResult.get("url") != null) {
                url = uploadResult.get("url").toString();
            }

            if (url == null) {
                log.error("Upload failed - no URL returned. Full result: {}", uploadResult);
                throw new RuntimeException("Upload failed: No URL returned");
            }

            log.info("Video uploaded successfully: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Video upload failed for file: {}", file.getOriginalFilename(), e);

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

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "raw", "folder", "korastudy/course-documents", "public_id", publicId, "overwrite", true, "invalidate", true, "type", "upload" // ← QUAN TRỌNG: đảm bảo file public
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info("Document uploaded successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Document upload failed", e);
            throw new RuntimeException("Lỗi khi upload document: " + e.getMessage());
        }
    }

    @Override
    public String uploadAudio(MultipartFile file, String title) {
        validateFile(file, "audio", MAX_FILE_SIZE_MB);

        try {
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "exam-audio");

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "video", "folder", "korastudy/exam-audio", "public_id", publicId);

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info("Audio uploaded successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Audio upload failed", e);
            throw new RuntimeException("Lỗi khi upload audio: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String publicId = extractPublicIdSimple(fileUrl);

            if (publicId != null) {
                String resourceType = determineResourceType(fileUrl);

                log.info("Deleting file - URL: {}, Public ID: {}, Resource Type: {}", fileUrl, publicId, resourceType);

                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));

                log.info("File deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn("Cannot extract public_id from URL: {}", fileUrl);
                throw new RuntimeException("Không thể xác định public_id từ URL: " + fileUrl);
            }
        } catch (IOException e) {
            log.error("File deletion failed", e);
            throw new RuntimeException("Lỗi khi xóa file từ Cloudinary: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file, String fileType, int maxSizeMB) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(String.format("%s không được vượt quá %dMB", fileType.substring(0, 1).toUpperCase() + fileType.substring(1), maxSizeMB));
        }

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

        log.info("File validated: {} (size: {} MB, type: {})", file.getOriginalFilename(), file.getSize() / (1024 * 1024), file.getContentType());
    }

    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename().toLowerCase();
        return (contentType != null && contentType.startsWith("video/")) || fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mov") || fileName.endsWith(".wmv");
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private boolean isAudioFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename().toLowerCase();
        return (contentType != null && contentType.startsWith("audio/")) || fileName.endsWith(".mp3") || fileName.endsWith(".wav");
    }

    private String extractPublicIdSimple(String url) {
        try {
            // Tách URL để lấy phần sau /upload/
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }

            String pathAfterUpload = url.substring(uploadIndex + "/upload/".length());

            // Xóa phần version (v123456789/)
            if (pathAfterUpload.startsWith("v")) {
                int slashIndex = pathAfterUpload.indexOf("/");
                if (slashIndex > 0 && pathAfterUpload.length() > 1 && Character.isDigit(pathAfterUpload.charAt(1))) {
                    pathAfterUpload = pathAfterUpload.substring(slashIndex + 1);
                }
            }

            // Xóa các transformation flags nếu có (f_auto, fl_attachment, etc.)
            if (pathAfterUpload.startsWith("f_") || pathAfterUpload.startsWith("fl_")) {
                int slashIndex = pathAfterUpload.indexOf("/");
                if (slashIndex > 0) {
                    pathAfterUpload = pathAfterUpload.substring(slashIndex + 1);

                    // Có thể có nhiều transformation flags
                    while (pathAfterUpload.startsWith("f_") || pathAfterUpload.startsWith("fl_")) {
                        slashIndex = pathAfterUpload.indexOf("/");
                        if (slashIndex > 0) {
                            pathAfterUpload = pathAfterUpload.substring(slashIndex + 1);
                        } else {
                            break;
                        }
                    }
                }
            }

            // Tìm phần bắt đầu từ korastudy/
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

    private String determineResourceType(String url) {
        if (url.contains("/image/upload/")) {
            return "image";
        } else if (url.contains("/video/upload/")) {
            return "video";
        } else if (url.contains("/raw/upload/")) {
            return "raw";
        } else {
            // Phân tích từ URL
            if (url.contains("/course-documents/")) {
                return "raw";
            } else if (url.contains("/course-images/")) {
                return "image";
            } else if (url.contains("/course-videos/") || url.contains("/exam-audio/")) {
                return "video";
            } else {
                return "image"; // default
            }
        }
    }

    private String generateSimplePublicId(String title, String originalFilename, String folder) {
        String baseName;

        if (title != null && !title.trim().isEmpty()) {
            baseName = sanitizeFileName(title);
        } else if (originalFilename != null) {
            baseName = sanitizeFileName(originalFilename);
        } else {
            baseName = UUID.randomUUID().toString();
        }

        return baseName;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }

        // Tìm dấu chấm cuối cùng để tách đuôi file
        int lastDotIndex = fileName.lastIndexOf('.');
        String namePart, extensionPart = "";

        if (lastDotIndex > 0) {
            namePart = fileName.substring(0, lastDotIndex);
            extensionPart = fileName.substring(lastDotIndex); // Giữ nguyên đuôi file
        } else {
            namePart = fileName;
        }

        // Chỉ sanitize phần tên file, không ảnh hưởng đuôi
        String sanitizedName = namePart.replaceAll("[^a-zA-Z0-9-_.]", "_").replaceAll("_{2,}", "_").replaceAll("^_|_$", "").toLowerCase();

        // Đảm bảo không có nhiều dấu chấm liên tiếp trong tên
        sanitizedName = sanitizedName.replaceAll("\\.{2,}", ".");

        return sanitizedName + extensionPart;
    }
}