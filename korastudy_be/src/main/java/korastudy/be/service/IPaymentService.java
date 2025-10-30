package korastudy.be.service;

import korastudy.be.entity.PaymentHistory;

import java.util.List;

public interface IPaymentService {

    /**
     * Tạo giao dịch thanh toán mới (trạng thái PENDING)
     */
    PaymentHistory createPayment(Long userId, Long courseId, Double amount);

    /**
     * Cập nhật thanh toán thành công (sau khi xác thực)
     */
    PaymentHistory markAsPaid(Long paymentId);

    /**
     * Hủy hoặc hoàn tiền (nếu cần)
     */
    PaymentHistory cancelPayment(Long paymentId);

    /**
     * Lấy lịch sử thanh toán của một người dùng
     */
    List<PaymentHistory> getPaymentHistoryByUser(Long userId);

    /**
     * Kiểm tra trạng thái giao dịch
     */
    String getPaymentStatus(Long paymentId);
}
