package korastudy.be.dto.response.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollmentStatsDTO {
    /**
     * Tổng số đăng ký
     */
    private Long totalEnrollments;

    /**
     * Số đăng ký đang học
     */
    private Long activeEnrollments;

    /**
     * Số đăng ký đã hoàn thành
     */
    private Long completedEnrollments;
}