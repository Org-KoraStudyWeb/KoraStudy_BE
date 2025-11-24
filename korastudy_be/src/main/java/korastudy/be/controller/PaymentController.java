package korastudy.be.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import korastudy.be.dto.request.payment.PaymentRequest;
import korastudy.be.dto.response.payment.BuyerInfoResponse;
import korastudy.be.dto.response.payment.CreatePaymentResponse;
import korastudy.be.dto.response.payment.PaymentResponse;
import korastudy.be.entity.PaymentHistory;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.PaymentHistoryRepository;
import korastudy.be.service.IPaymentService;
import korastudy.be.service.impl.VnPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;
    private final VnPayService vnPayService;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final AccountRepository accountRepository;

    // Tạo yêu cầu thanh toán
    @Transactional
    @PostMapping("/create")
    public ResponseEntity<CreatePaymentResponse> createPayment(@RequestBody PaymentRequest request, HttpServletRequest httpRequest) {
        try {
            // Tạo payment record trong database
            PaymentResponse paymentResponse = paymentService.createPayment(request);

            // Chuẩn bị thông tin cho VNPay
            String txnRef = paymentResponse.getTransactionCode();
            String orderInfo = "Thanh toan khoa hoc " + txnRef;
            long amount = paymentResponse.getTransactionPrice().longValue();

            // Tạo URL thanh toán VNPay
            String paymentUrl = vnPayService.createPaymentUrl(httpRequest, amount, orderInfo, txnRef);

            return ResponseEntity.ok(new CreatePaymentResponse(paymentResponse, paymentUrl));

        } catch (Exception e) {
            throw e;
        }
    }

    // Xử lý callback từ VNPay sau khi thanh toán
    @GetMapping("/vnpay-return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String txnRef = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");

        try {
            // Tìm payment record dựa trên mã giao dịch
            PaymentHistory payment = paymentHistoryRepository.findByTransactionCode(txnRef).orElseThrow(() -> new RuntimeException("Payment not found for TxnRef: " + txnRef));

            // Xác thực chữ ký từ VNPay
            boolean validSignature = vnPayService.verifyPayment(request);

            String status = "failed";

            // Xử lý kết quả thanh toán
            if (validSignature && "00".equals(responseCode)) {
                // Thanh toán thành công - cập nhật trạng thái và tạo enrollment
                paymentService.markAsPaid(payment.getId());
                status = "success";
            } else {
                // Thanh toán thất bại - hủy payment
                paymentService.cancelPayment(payment.getId());
                status = "failed";
            }

            // Redirect về frontend với kết quả
            String redirectUrl = String.format("http://localhost:3000/payment/result?paymentId=%d&status=%s", payment.getId(), status);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            // Xử lý lỗi và redirect về frontend với thông báo lỗi
            String errorUrl = "http://localhost:3000/payment/result?status=error&message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
        }
    }

    // Hủy thanh toán theo ID
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    // Lấy lịch sử thanh toán của user hiện tại
    @GetMapping("/history")
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(@AuthenticationPrincipal UserDetails userDetails) {
        // Lấy thông tin user từ authentication
        String username = userDetails.getUsername();
        Account account = accountRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Long userId = account.getUser().getId();

        // Lấy lịch sử thanh toán
        List<PaymentResponse> history = paymentService.getPaymentHistoryByUser(userId);
        return ResponseEntity.ok(history);
    }

    // Lấy trạng thái thanh toán theo ID
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long paymentId) {
        String status = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/buyer-info")
    public ResponseEntity<BuyerInfoResponse> getBuyerInfo(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            Account account = accountRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            User user = account.getUser();

            // Lấy thông tin từ user profile để pre-fill form
            BuyerInfoResponse response = BuyerInfoResponse.builder().buyerName(user.getFullName())    // "Từ Đàm Văn Thiên"
                    .buyerEmail(user.getEmail())      // Email từ user
                    .buyerPhone(user.getPhoneNumber())      // Số điện thoại từ user
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e;
        }
    }
}