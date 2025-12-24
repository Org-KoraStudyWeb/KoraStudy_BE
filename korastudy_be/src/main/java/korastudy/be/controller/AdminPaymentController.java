package korastudy.be.controller;

import korastudy.be.dto.response.payment.PaymentDetailResponse;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.PaymentHistoryRepository;
import korastudy.be.service.IPaymentService;
import korastudy.be.service.impl.VnPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/payments")  // ← URL có /admin/
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final IPaymentService paymentService;

    /*
     * Lấy tất cả thanh toán với phân trang - TRẢ VỀ PAGE
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateTransaction") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentDetailResponse> pageResult = paymentService.getAllPaymentsForAdmin(pageable);

        // Tạo response object
        Map<String, Object> response = new HashMap<>();
        response.put("payments", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("pageSize", pageResult.getSize());
        response.put("hasNext", pageResult.hasNext());
        response.put("hasPrevious", pageResult.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /*
     * Lấy chi tiết một thanh toán theo ID
     */

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDetailResponse> getPaymentDetails(@PathVariable Long id) {
        PaymentDetailResponse payment = paymentService.getPaymentDetailsForAdmin(id);
        return ResponseEntity.ok(payment);
    }

    /*
     * TÌM KIẾM thanh toán với nhiều điều kiện
     */

    /*
     * TÌM KIẾM thanh toán với nhiều điều kiện
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPayments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,  // ← THÊM sortBy riêng
            @RequestParam(required = false) String sortDirection) { // ← THÊM sortDirection riêng

        // Tạo sort đơn giản
        Sort sort;
        if (sortBy != null && sortDirection != null) {
            sort = sortDirection.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
        } else {
            // Default sort
            sort = Sort.by("dateTransaction").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentDetailResponse> pageResult = paymentService.searchPayments(
                keyword, status, paymentMethod, fromDate, toDate, pageable
        );

        // Tạo response object
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("payments", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("pageSize", pageResult.getSize());
        response.put("hasNext", pageResult.hasNext());
        response.put("hasPrevious", pageResult.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /*
     * XUẤT PDF
     */

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

        byte[] pdfBytes = paymentService.exportToPdf(startDateTime, endDateTime);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("payment-report-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")) + ".pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /*
     * XUẤT EXCEL
     */

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

        byte[] excelBytes = paymentService.exportToExcel(startDateTime, endDateTime);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("payment-report-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")) + ".xlsx")
                .build());

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    /*
     * THỐNG KÊ doanh thu
     */

    @GetMapping("/stats/revenue")
    public ResponseEntity<Double> getTotalRevenue() {
        Double revenue = paymentService.getTotalRevenue();
        return ResponseEntity.ok(revenue);
    }

    /*
     * THỐNG KÊ theo trạng thái
     */
    @GetMapping("/stats/status")
    public ResponseEntity<Map<String, Long>> getPaymentStatusStats() {
        Map<String, Long> stats = paymentService.countByStatus();
        return ResponseEntity.ok(stats);
    }
}
