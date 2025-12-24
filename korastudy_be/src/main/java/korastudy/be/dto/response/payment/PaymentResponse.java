package korastudy.be.dto.response.payment;


import jdk.jshell.Snippet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private Double transactionPrice;
    private String transactionStatus;
    private String transactionCode;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private Long userId;
    private Long courseId;

}
