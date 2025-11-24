package korastudy.be.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyerInfoResponse {
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
}