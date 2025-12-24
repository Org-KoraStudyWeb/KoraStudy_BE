package korastudy.be.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponse {
    private Long id;
    private BigDecimal amount;
    private String status;
    private String transactionCode;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private Long userId;
    private String userFullName;
    private Long courseId;
    private String courseTitle;
    private Double coursePrice;
    private String paymentMethod;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}