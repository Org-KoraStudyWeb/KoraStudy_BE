package korastudy.be.dto.request.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Long courseId;
    private Double amount;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
}
