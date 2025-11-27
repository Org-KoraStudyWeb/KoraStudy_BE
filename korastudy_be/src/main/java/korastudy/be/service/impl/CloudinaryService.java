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

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService implements IUploadService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String title) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            //  QUAY VỀ CÁCH CŨ: Không dùng timestamp trong public_id
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "course-images");

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "image", "folder", "korastudy/course-images", "public_id", publicId, "transformation", "q_auto");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info(" Image uploaded successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error(" Image upload failed", e);
            throw new RuntimeException("Lỗi khi upload image: " + e.getMessage());
        }
    }

    @Override
    public String uploadVideo(MultipartFile file, String title) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "course-videos");

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "video", "folder", "korastudy/course-videos", "public_id", publicId, "quality", "auto"
                    // BỎ eager_async và async
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            // Kiểm tra cả secure_url và url thường
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
            log.error("Video upload failed", e);
            throw new RuntimeException("Lỗi khi upload video: " + e.getMessage());
        }
    }

    @Override
    public String uploadDocument(MultipartFile file, String title) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            //  QUAY VỀ CÁCH CŨ: Không dùng timestamp
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "course-documents");

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "raw", "folder", "korastudy/course-documents", "public_id", publicId);

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info(" Document uploaded successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error(" Document upload failed", e);
            throw new RuntimeException("Lỗi khi upload document: " + e.getMessage());
        }
    }

    @Override
    public String uploadAudio(MultipartFile file, String title) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            //  QUAY VỀ CÁCH CŨ: Không dùng timestamp
            String publicId = generateSimplePublicId(title, file.getOriginalFilename(), "exam-audio");

            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "video", // Cloudinary uses 'video' for audio files
                    "folder", "korastudy/exam-audio", "public_id", publicId);

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String url = uploadResult.get("secure_url").toString();
            log.info(" Audio uploaded successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error(" Audio upload failed", e);
            throw new RuntimeException("Lỗi khi upload audio: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            //  SỬA LẠI: Extract public_id ĐƠN GIẢN theo cách cũ
            String publicId = extractPublicIdSimple(fileUrl);

            if (publicId != null) {
                // Xác định resource type
                String resourceType = determineResourceType(fileUrl);

                log.info(" Deleting file - URL: {}, Public ID: {}, Resource Type: {}", fileUrl, publicId, resourceType);

                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));

                log.info(" File deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn(" Cannot extract public_id from URL: {}", fileUrl);
                throw new RuntimeException("Không thể xác định public_id từ URL: " + fileUrl);
            }
        } catch (IOException e) {
            log.error(" File deletion failed", e);
            throw new RuntimeException("Lỗi khi xóa file từ Cloudinary: " + e.getMessage());
        }
    }

    /**
     * PHƯƠNG THỨC CŨ ĐƠN GIẢN: Extract public_id từ URL
     */
    private String extractPublicIdSimple(String url) {
        try {
            log.debug(" Extracting public_id from URL: {}", url);

            // Format: https://res.cloudinary.com/dfqfh3hzu/image/upload/v1234567/korastudy/course-images/filename.jpg
            // Chúng ta cần lấy: "korastudy/course-images/filename"

            String[] parts = url.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String pathAfterUpload = parts[1];

            // Tìm vị trí của "korastudy/"
            int korastudyIndex = pathAfterUpload.indexOf("korastudy/");
            if (korastudyIndex == -1) {
                return null;
            }

            // Lấy phần từ "korastudy/" đến hết (trước extension)
            String publicIdWithExtension = pathAfterUpload.substring(korastudyIndex);

            // Remove file extension
            int lastDotIndex = publicIdWithExtension.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return publicIdWithExtension.substring(0, lastDotIndex);
            }

            return publicIdWithExtension;

        } catch (Exception e) {
            log.warn(" Failed to extract public_id from URL: {}", url, e);
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
            // Lấy tên file không có extension
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
        return fileName.replaceAll("[^a-zA-Z0-9-_]", "_").replaceAll("_{2,}", "_").replaceAll("^_|_$", "").toLowerCase();
    }
}