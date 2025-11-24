package korastudy.be.service;

import korastudy.be.dto.request.payment.PaymentRequest;
import korastudy.be.dto.response.payment.PaymentResponse;
import korastudy.be.entity.PaymentHistory;

import java.util.List;

public interface IPaymentService {

    // Tạo payment mới, trả về DTO
    PaymentResponse createPayment(PaymentRequest request);

    // Cập nhật trạng thái thành SUCCESS
    PaymentResponse markAsPaid(Long paymentId);

    // Hủy payment
    PaymentResponse cancelPayment(Long paymentId);

    // Lấy lịch sử payment của user theo userId
    List<PaymentResponse> getPaymentHistoryByUser(Long userId);

    // Lấy trạng thái payment
    String getPaymentStatus(Long paymentId);
}
