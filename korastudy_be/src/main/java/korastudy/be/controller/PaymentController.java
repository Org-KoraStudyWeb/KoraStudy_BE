package korastudy.be.controller;

import jakarta.servlet.http.HttpServletRequest;
import korastudy.be.entity.PaymentHistory;
import korastudy.be.service.IEmailService;
import korastudy.be.service.IPaymentService;
import korastudy.be.service.impl.VnPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;
    private final VnPayService vnPayService;
    private final IEmailService emailService;

    /**
     * B1️⃣: User chọn khóa học -> gọi API này để tạo link thanh toán
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createPayment(HttpServletRequest request, @RequestParam Long userId, @RequestParam Long courseId, @RequestParam Double amount) {

        // Tạo bản ghi payment
        PaymentHistory payment = paymentService.createPayment(userId, courseId, amount);

        // Gọi service VNPay để tạo URL thanh toán
        String orderInfo = "Thanh toán khóa học: " + payment.getCourse().getCourseName();
        String paymentUrl = vnPayService.createPaymentUrl(request, payment.getTransactionPrice().longValue(), orderInfo);

        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl, "paymentId", String.valueOf(payment.getId())));
    }

    /**
     * B2️⃣: VNPay redirect về sau khi thanh toán -> xác nhận giao dịch
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnPayReturn(HttpServletRequest request) {
        boolean success = vnPayService.verifyPayment(request);
        String txnRef = request.getParameter("vnp_TxnRef");
        Long paymentId = (txnRef != null) ? Long.valueOf(txnRef) : null;

        if (success && paymentId != null) {
            PaymentHistory paid = paymentService.markAsPaid(paymentId);
            emailService.sendPaymentConfirmation(paid.getUser().getAccount(), paid.getCourse(), paid.getTransactionPrice().intValue());
            return ResponseEntity.ok("Thanh toán thành công! Khóa học đã được thêm vào My Courses.");
        } else {
            if (paymentId != null) paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok("Thanh toán thất bại hoặc bị hủy.");
        }
    }

    /**
     * B3️⃣: Lấy lịch sử thanh toán của user
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getUserPayments(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentHistoryByUser(userId));
    }

    /**
     * B4️⃣: Kiểm tra trạng thái thanh toán cụ thể
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(paymentId));
    }
}
