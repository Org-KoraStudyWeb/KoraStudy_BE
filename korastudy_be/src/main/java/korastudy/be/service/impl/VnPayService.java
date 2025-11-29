package korastudy.be.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import korastudy.be.utils.VnPayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VnPayService {

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    // ==========================
    // Tạo URL thanh toán
    // ==========================
    public String createPaymentUrl(HttpServletRequest request, long amount, String orderInfo) {
        try {
            String vnp_TxnRef = VnPayUtils.getRandomNumber(8);
            String vnp_IpAddr = VnPayUtils.getIpAddress(request);

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
            cld.add(Calendar.MINUTE, 15);
            vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

            String query = VnPayUtils.buildQueryString(vnp_Params);
            String vnp_SecureHash = VnPayUtils.hmacSHA512(vnp_HashSecret, query);

            return vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo URL thanh toán VNPay", e);
        }
    }

    // ==========================
    // Xác minh thanh toán callback
    // ==========================
    public boolean verifyPayment(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();

        // Lấy tất cả tham số từ request (trừ vnp_SecureHash)
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String key = params.nextElement();
            String value = request.getParameter(key);
            if (value != null && value.length() > 0) {
                fields.put(key, value);
            }
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String signValue = VnPayUtils.hmacSHA512(vnp_HashSecret, VnPayUtils.buildQueryString(fields));

        // So sánh hash để xác thực
        if (signValue.equals(vnp_SecureHash)) {
            // Kiểm tra thêm mã phản hồi từ VNPay
            String responseCode = fields.get("vnp_ResponseCode");
            return "00".equals(responseCode); // "00" nghĩa là thanh toán thành công
        }
        return false;
    }
}
