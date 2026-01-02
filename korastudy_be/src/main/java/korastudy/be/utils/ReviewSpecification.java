package korastudy.be.utils;

import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Enum.ReviewType;
import korastudy.be.entity.Review.Review;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class ReviewSpecification {

    public static Specification<Review> hasTargetType(String targetType) {
        return (root, query, criteriaBuilder) -> {
            if (targetType == null || targetType.trim().isEmpty()) {
                return null; // Không áp dụng filter
            }
            try {
                ReviewType reviewType = ReviewType.valueOf(targetType.toUpperCase());
                return criteriaBuilder.equal(root.get("reviewType"), reviewType);
            } catch (IllegalArgumentException e) {
                return null; // Bỏ qua filter không hợp lệ
            }
        };
    }

    public static Specification<Review> hasStatus(ReviewStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null; // Không áp dụng filter
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Review> hasTargetId(Long targetId) {
        return (root, query, criteriaBuilder) -> {
            if (targetId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("targetId"), targetId);
        };
    }

    public static Specification<Review> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), ReviewStatus.ACTIVE);
    }
}
