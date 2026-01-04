package korastudy.be.service.impl;

import korastudy.be.config.VnPayConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayService {

    private final VnPayConfig vnPayConfig;

    // ==========================
    // Tạo URL thanh toán - ĐÃ FIX
    // ==========================
    public String createPaymentUrl(HttpServletRequest request, long amount, String orderInfo, String txnRef) {
        try {
            String vnp_IpAddr = getIpAddress(request);

            //  KHÔNG nhân 100 - dùng amount trực tiếp
            long finalAmount = amount * 100;

            // 1. Khởi tạo các tham số
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnPayConfig.getVersion());
            vnp_Params.put("vnp_Command", vnPayConfig.getCommand());
            vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnp_Params.put("vnp_Amount", String.valueOf(finalAmount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", txnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            // Tạo Calendar và SimpleDateFormat với timezone GMT+7 (Việt Nam)
            TimeZone vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
            Calendar cld = Calendar.getInstance(vietnamTimeZone);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(vietnamTimeZone); // QUAN TRỌNG: Set timezone cho formatter
            
            vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
            cld.add(Calendar.MINUTE, 15);
            vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

            // Sắp xếp
            Map<String, String> sortedParams = new TreeMap<>(vnp_Params);

            // 2. Tạo query string ĐÃ MÃ HÓA
            StringBuilder queryString = new StringBuilder();
            Iterator<Map.Entry<String, String>> itr = sortedParams.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                queryString.append(entry.getKey());
                queryString.append('=');
                queryString.append(urlEncode(entry.getValue()));
                if (itr.hasNext()) {
                    queryString.append('&');
                }
            }

            String queryStringEncoded = queryString.toString();
            log.info("VNPAY HASH DATA STRING (Đã Encode): {}", queryStringEncoded);

            // 3. Tạo chữ ký
            String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), queryStringEncoded);
            log.info("VNPAY SECURE HASH (Local): {}", vnp_SecureHash);

            // 4. Tạo URL hoàn chỉnh
            String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryStringEncoded + "&vnp_SecureHash=" + vnp_SecureHash;
            log.info("VNPAY Payment URL: {}", paymentUrl);

            return paymentUrl;

        } catch (Exception e) {
            log.error("Lỗi khi tạo URL thanh toán VNPay", e);
            throw new RuntimeException("Lỗi khi tạo URL thanh toán VNPay", e);
        }
    }

    // ==========================
    // Xác thực callback - ĐÃ FIX
    // ==========================
    public boolean verifyPayment(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();

        // Lấy tất cả parameters từ request
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String key = params.nextElement();
            String value = request.getParameter(key);
            if (value != null && value.length() > 0) {
                fields.put(key, value);
            }
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            log.error("vnp_SecureHash is null!");
            return false;
        }
        fields.remove("vnp_SecureHashType");

        log.info("All parameters from VNPAY: {}", fields);

        // CHỈ lấy các field bắt đầu bằng "vnp_"
        Map<String, String> vnpFields = new TreeMap<>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (entry.getKey().startsWith("vnp_")) {
                vnpFields.put(entry.getKey(), entry.getValue());
            }
        }

        //  Tạo query string với URL ENCODING (giống khi tạo URL)
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpFields.entrySet()) {
            if (hashData.length() > 0) {
                hashData.append('&');
            }
            hashData.append(entry.getKey());
            hashData.append('=');
            hashData.append(urlEncode(entry.getValue())); // <-- QUAN TRỌNG: ENCODE lại
        }

        String hashDataStr = hashData.toString();
        log.info("CALLBACK HASH DATA (Đã encode): {}", hashDataStr);

        String signValue = hmacSHA512(vnPayConfig.getHashSecret(), hashDataStr);

        log.info("CALLBACK HASH (Local): {}", signValue);
        log.info("CALLBACK HASH (VNPAY): {}", vnp_SecureHash);

        // So sánh case-insensitive
        boolean isValid = signValue.equalsIgnoreCase(vnp_SecureHash);

        if (isValid) {
            String responseCode = fields.get("vnp_ResponseCode");
            return "00".equals(responseCode);
        } else {
            log.warn("VNPAY Signature Mismatch! Local: {} vs VNPAY: {}", signValue, vnp_SecureHash);
            return false;
        }
    }

    // ==========================
    // Hàm encode URL (Giữ nguyên)
    // ==========================
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            log.error("Cannot encode URL value: {}", value, e);
            return "";
        }
    }

    // ==========================
    // HMAC SHA512 (Giữ nguyên)
    // ==========================
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                sb.append(String.format("%02X", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Lỗi tạo HMAC SHA512", e);
            throw new RuntimeException("Lỗi tạo HMAC SHA512", e);
        }
    }

    // ==========================
    // Lấy IP client (Giữ nguyên)
    // ==========================
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip.split(",")[0];
    }
}