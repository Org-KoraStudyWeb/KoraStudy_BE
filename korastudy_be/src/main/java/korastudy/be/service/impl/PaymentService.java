package korastudy.be.service.impl;

// Spring imports
import com.itextpdf.text.Font;
import korastudy.be.service.IPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// iText PDF imports (CHỈ CẦN import com.itextpdf.text.* là đủ)
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

// Apache POI Excel imports
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Java imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

// Lombok
import lombok.RequiredArgsConstructor;

// Your project imports
import korastudy.be.dto.request.payment.PaymentRequest;
import korastudy.be.dto.response.payment.PaymentDetailResponse;
import korastudy.be.dto.response.payment.PaymentResponse;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Enrollment;
import korastudy.be.entity.PaymentHistory;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import korastudy.be.repository.*;
import korastudy.be.service.IEmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements IPaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final AccountRepository accountRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final IEmailService emailService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            // Xác thực người dùng
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED: Missing or invalid authentication token. Please log in again.");
            }

            // Lấy thông tin tài khoản từ authentication
            String username = authentication.getName();
            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            User user = account.getUser();

            // Tìm khóa học theo ID
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Tạo mã giao dịch duy nhất
            String transactionCode = java.util.UUID.randomUUID().toString().replace("-", "");

            // Tạo lịch sử thanh toán với thông tin từ request
            PaymentHistory payment = PaymentHistory.builder()
                    .user(user)
                    .course(course)
                    .transactionPrice(request.getAmount())
                    .transactionStatus("PENDING")
                    .transactionCode(transactionCode)
                    .buyerName(request.getBuyerName())
                    .buyerEmail(request.getBuyerEmail())
                    .buyerPhone(request.getBuyerPhone())
                    .paymentMethod("VNPAY")
                    .dateTransaction(LocalDateTime.now())
                    .build();

            // Lưu thông tin thanh toán
            paymentHistoryRepository.save(payment);

            return mapToResponse(payment);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public PaymentResponse markAsPaid(Long paymentId) {
        try {
            // Tìm thanh toán theo ID
            PaymentHistory payment = paymentHistoryRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Cập nhật trạng thái thanh toán thành công
            payment.setTransactionStatus("SUCCESS");
            payment.setDateTransaction(LocalDateTime.now());
            paymentHistoryRepository.save(payment);

            // Kiểm tra xem user đã đăng ký khóa học chưa
            Optional<Enrollment> existing = enrollmentRepository.findByUserIdAndCourseId(
                    payment.getUser().getId(),
                    payment.getCourse().getId()
            );

            // Nếu chưa đăng ký thì tạo mới enrollment
            if (existing.isEmpty()) {
                Enrollment enrollment = Enrollment.builder()
                        .user(payment.getUser())
                        .course(payment.getCourse())
                        .enrollDate(LocalDate.now())
                        .expiryDate(LocalDate.now().plusMonths(6))
                        .progress(0.0)
                        .build();
                enrollmentRepository.save(enrollment);
            }

            // Gửi email xác nhận thanh toán
            try {
                emailService.sendPaymentConfirmation(
                        payment.getUser().getAccount(),
                        payment.getCourse(),
                        payment.getTransactionPrice().intValue()
                );
            } catch (Exception e) {
                // Bỏ qua lỗi gửi email, không ảnh hưởng đến thanh toán
            }

            return mapToResponse(payment);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        try {
            // Tìm thanh toán theo ID
            PaymentHistory payment = paymentHistoryRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Cập nhật trạng thái thành đã hủy
            payment.setTransactionStatus("CANCELLED");
            paymentHistoryRepository.save(payment);

            return mapToResponse(payment);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<PaymentResponse> getPaymentHistoryByUser(Long userId) {
        // Lấy lịch sử thanh toán của user
        return paymentHistoryRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public String getPaymentStatus(Long paymentId) {
        // Lấy trạng thái thanh toán
        return paymentHistoryRepository.findById(paymentId)
                .map(PaymentHistory::getTransactionStatus)
                .orElse("NOT_FOUND");
    }

    // Chuyển đổi entity sang DTO
    private PaymentResponse mapToResponse(PaymentHistory payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getTransactionPrice(),
                payment.getTransactionStatus(),
                payment.getTransactionCode(),
                payment.getBuyerName(),
                payment.getBuyerEmail(),
                payment.getBuyerPhone(),
                payment.getUser().getId(),
                payment.getCourse().getId()
        );
    }

    /*
     * Hiển thị data của payment cho user
     */

    @Override
    public Page<PaymentDetailResponse> getAllPaymentsForAdmin(Pageable pageable) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Admin role required.");
            }

            Page<PaymentHistory> paymentsPage = paymentHistoryRepository.findAllByOrderByDateTransactionDesc(pageable);

            return paymentsPage.map(this::mapToDetailedResponse);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching payment data", e);
        }
    }


    @Override
    public PaymentDetailResponse getPaymentDetailsForAdmin(Long paymentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Kiểm tra quyền admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Admin role required.");
            }

            PaymentHistory payment = paymentHistoryRepository.findById(paymentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

            return mapToDetailedResponse(payment);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching payment details", e);
        }
    }

    @Override
    public Page<PaymentDetailResponse> searchPayments(
            String keyword,
            String status,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Admin role required.");
            }

            // Nếu có toDate, set giờ là 23:59:59 để bao gồm cả ngày đó
            if (toDate != null) {
                toDate = toDate.with(LocalTime.MAX);
            }

            Page<PaymentHistory> paymentsPage = paymentHistoryRepository.searchPayments(
                    keyword, status, paymentMethod, fromDate, toDate, pageable
            );

            return paymentsPage.map(this::mapToDetailedResponse);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error searching payments", e);
        }
    }

    @Override
    public Double getTotalRevenue() {
        try {
            // Chỉ tính các giao dịch thành công
            Double totalRevenue = paymentHistoryRepository.calculateTotalRevenue();
            return totalRevenue != null ? totalRevenue : 0.0;
        } catch (Exception e) {
            return 0.0; // Trả về 0 thay vì throw exception để dashboard vẫn hiển thị được
        }
    }

    @Override
    public Map<String, Long> countByStatus() {
        try {
            List<Object[]> results = paymentHistoryRepository.countByStatus();

            // Chuyển đổi từ List<Object[]> sang Map<String, Long>
            Map<String, Long> statusCounts = new HashMap<>();

            for (Object[] result : results) {
                if (result.length >= 2) {
                    String status = (String) result[0];
                    Long count = (Long) result[1];
                    statusCounts.put(status, count);
                }
            }

            return statusCounts;

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public byte[] exportToPdf(LocalDateTime fromDate, LocalDateTime toDate) {
        try {
            // Kiểm tra quyền
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Admin role required.");
            }

            // Lấy dữ liệu
            List<PaymentHistory> payments;
            if (fromDate != null && toDate != null) {
                toDate = toDate.with(LocalTime.MAX); // Bao gồm cả ngày cuối
                payments = paymentHistoryRepository.findByDateTransactionBetween(fromDate, toDate);
            } else {
                payments = paymentHistoryRepository.findAllByOrderByDateTransactionDesc();
            }

            // Tạo PDF
            return generatePdfReport(payments);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error generating PDF report", e);
        }
    }

    @Override
    public byte[] exportToExcel(LocalDateTime fromDate, LocalDateTime toDate) {
        try {
            // Kiểm tra quyền
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Admin role required.");
            }

            // Lấy dữ liệu
            List<PaymentHistory> payments;
            if (fromDate != null && toDate != null) {
                toDate = toDate.with(LocalTime.MAX);
                payments = paymentHistoryRepository.findByDateTransactionBetween(fromDate, toDate);
            } else {
                payments = paymentHistoryRepository.findAllByOrderByDateTransactionDesc();
            }

            // Tạo Excel
            return generateExcelReport(payments);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error generating Excel report", e);
        }
    }

    // Helper methods cho export
    private byte[] generatePdfReport(List<PaymentHistory> payments) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // ============ PHẦN QUAN TRỌNG: LOAD FONT TIẾNG VIỆT ============
            BaseFont vietnameseFont = loadVietnameseBaseFont();

            // Tạo các font từ BaseFont đã load
            Font titleFont = new Font(vietnameseFont, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font headerFont = new Font(vietnameseFont, 9, Font.BOLD);
            Font cellFont = new Font(vietnameseFont, 8);
            Font infoFont = new Font(vietnameseFont, 10);
            Font statsFont = new Font(vietnameseFont, 10, Font.BOLD);
            // ===============================================================

            // Tạo tiêu đề
            Paragraph title = new Paragraph("BÁO CÁO LỊCH SỬ THANH TOÁN\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Thông tin báo cáo
            Paragraph info = new Paragraph(
                    String.format("Ngày xuất báo cáo: %s\nSố lượng giao dịch: %d\n\n",
                            java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                            payments.size()),
                    infoFont
            );
            document.add(info);

            // Tạo bảng
            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Độ rộng cột
            float[] columnWidths = {3f, 8f, 10f, 10f, 8f, 6f, 8f, 8f, 10f, 12f, 10f};
            table.setWidths(columnWidths);

            // Tiêu đề cột với style
            String[] headers = {
                    "ID", "Mã GD", "Người mua", "Email", "SĐT",
                    "Số tiền", "Trạng thái", "PTTT", "Ngày GD", "Khóa học", "Người dùng"
            };

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new BaseColor(220, 220, 220));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Dữ liệu
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (PaymentHistory payment : payments) {
                // ID
                addTableCell(table, payment.getId().toString(), cellFont);

                // Mã GD
                addTableCell(table, payment.getTransactionCode() != null ? payment.getTransactionCode() : "N/A", cellFont);

                // Người mua
                addTableCell(table, payment.getBuyerName(), cellFont);

                // Email
                addTableCell(table, payment.getBuyerEmail(), cellFont);

                // SĐT
                addTableCell(table, payment.getBuyerPhone() != null ? payment.getBuyerPhone() : "N/A", cellFont);

                // Số tiền
                PdfPCell amountCell = new PdfPCell(new Phrase(
                        String.format("%,.0f VND", payment.getTransactionPrice()),
                        cellFont
                ));
                amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                amountCell.setPadding(5);
                table.addCell(amountCell);

                // Trạng thái
                addTableCell(table, payment.getTransactionStatus(), cellFont);

                // Phương thức TT
                addTableCell(table, payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "N/A", cellFont);

                // Ngày GD
                addTableCell(table, payment.getDateTransaction().format(dateFormatter), cellFont);

                // Khóa học
                String courseName = payment.getCourse() != null ? payment.getCourse().getCourseName() : "N/A";
                addTableCell(table, courseName, cellFont);

                // Người dùng
                String userName = payment.getUser() != null ? payment.getUser().getFullName() : "N/A";
                addTableCell(table, userName, cellFont);
            }

            document.add(table);

            // Thống kê
            long successCount = payments.stream()
                    .filter(p -> "SUCCESS".equals(p.getTransactionStatus()))
                    .count();

            double totalRevenue = payments.stream()
                    .filter(p -> "SUCCESS".equals(p.getTransactionStatus()))
                    .mapToDouble(PaymentHistory::getTransactionPrice)
                    .sum();

            Paragraph stats = new Paragraph("\n\n");
            stats.add(new Chunk("THỐNG KÊ:\n", statsFont));
            stats.add(new Chunk(String.format(
                    "• Tổng số giao dịch: %d\n• Giao dịch thành công: %d\n• Tổng doanh thu: %,.0f VND\n",
                    payments.size(), successCount, totalRevenue
            ), cellFont));
            document.add(stats);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }

        return baos.toByteArray();
    }

    // ============ PHƯƠNG THỨC MỚI: LOAD FONT TIẾNG VIỆT ============
    private BaseFont loadVietnameseBaseFont() throws DocumentException, IOException {
        InputStream fontStream = null;

        try {
            // 1. Thử load từ resources (dành cho Azure/Linux)
            fontStream = getClass().getClassLoader().getResourceAsStream("fonts/arial.ttf");

            if (fontStream == null) {
                // 2. Thử load các biến thể khác
                String[] possibleFontNames = {
                        "arial.ttf"
                };

                for (String fontName : possibleFontNames) {
                    fontStream = getClass().getClassLoader().getResourceAsStream("fonts/" + fontName);
                    if (fontStream != null) {
                        log.info("Found font: fonts/" + fontName);
                        break;
                    }
                }
            }

            if (fontStream != null) {
                // Đọc font từ InputStream
                byte[] fontBytes = IOUtils.toByteArray(fontStream);
                BaseFont font = BaseFont.createFont(
                        "arial.ttf",                    // Tên font
                        BaseFont.IDENTITY_H,           // Encoding Unicode
                        BaseFont.EMBEDDED,             // Nhúng font vào PDF
                        BaseFont.CACHED,               // Cache font
                        fontBytes,                     // Dữ liệu font
                        null                           // Không cần font data
                );
                fontStream.close();
                log.info("Vietnamese font loaded successfully from resources");
                return font;
            }

            log.warn("No font found in resources, trying system fonts...");

        } catch (Exception e) {
            log.warn("Error loading font from resources: {}", e.getMessage());
        } finally {
            if (fontStream != null) {
                try { fontStream.close(); } catch (Exception e) {}
            }
        }

        // 3. Fallback: Thử font hệ thống
        try {
            // Windows path
            BaseFont font = BaseFont.createFont(
                    "c:/windows/fonts/arial.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );
            log.info("Loaded font from Windows system");
            return font;
        } catch (Exception e) {
            // Linux path
            try {
                BaseFont font = BaseFont.createFont(
                        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED
                );
                log.info("Loaded font from Linux system");
                return font;
            } catch (Exception ex) {
                // 4. Final fallback: Font mặc định (không tiếng Việt)
                log.error("Cannot load Vietnamese font, using default Latin font");
                return BaseFont.createFont(
                        BaseFont.HELVETICA,
                        BaseFont.CP1252,
                        BaseFont.EMBEDDED
                );
            }
        }
    }

    private void addTableCell(PdfPTable table, String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private byte[] generateExcelReport(List<PaymentHistory> payments) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Payment Report");

            // Tạo style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Tạo style cho cell
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Style cho số tiền
            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.cloneStyleFrom(cellStyle);
            DataFormat format = workbook.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("#,##0"));
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Style cho ngày
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(cellStyle);
            dateStyle.setDataFormat(format.getFormat("dd/mm/yyyy hh:mm"));

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Mã GD", "Người mua", "Email", "SĐT",
                    "Số tiền (VND)", "Trạng thái", "PTTT", "Ngày GD", "Khóa học", "Người dùng"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dữ liệu
            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (PaymentHistory payment : payments) {
                Row row = sheet.createRow(rowNum++);

                // ID
                Cell idCell = row.createCell(0);
                idCell.setCellValue(payment.getId());
                idCell.setCellStyle(cellStyle);

                // Mã GD
                Cell codeCell = row.createCell(1);
                codeCell.setCellValue(payment.getTransactionCode() != null ? payment.getTransactionCode() : "N/A");
                codeCell.setCellStyle(cellStyle);

                // Người mua
                Cell nameCell = row.createCell(2);
                nameCell.setCellValue(payment.getBuyerName());
                nameCell.setCellStyle(cellStyle);

                // Email
                Cell emailCell = row.createCell(3);
                emailCell.setCellValue(payment.getBuyerEmail());
                emailCell.setCellStyle(cellStyle);

                // SĐT
                Cell phoneCell = row.createCell(4);
                phoneCell.setCellValue(payment.getBuyerPhone() != null ? payment.getBuyerPhone() : "N/A");
                phoneCell.setCellStyle(cellStyle);

                // Số tiền
                Cell amountCell = row.createCell(5);
                amountCell.setCellValue(payment.getTransactionPrice());
                amountCell.setCellStyle(amountStyle);

                // Trạng thái
                Cell statusCell = row.createCell(6);
                statusCell.setCellValue(payment.getTransactionStatus());
                statusCell.setCellStyle(cellStyle);

                // PTTT
                Cell methodCell = row.createCell(7);
                methodCell.setCellValue(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "N/A");
                methodCell.setCellStyle(cellStyle);

                // Ngày GD
                Cell dateCell = row.createCell(8);
                dateCell.setCellValue(payment.getDateTransaction().format(dateFormatter));
                dateCell.setCellStyle(cellStyle);

                // Khóa học
                Cell courseCell = row.createCell(9);
                String courseName = payment.getCourse() != null ? payment.getCourse().getCourseName() : "N/A";
                courseCell.setCellValue(courseName);
                courseCell.setCellStyle(cellStyle);

                // Người dùng
                Cell userCell = row.createCell(10);
                String userName = payment.getUser() != null ? payment.getUser().getFullName() : "N/A";
                userCell.setCellValue(userName);
                userCell.setCellStyle(cellStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Thêm thống kê ở sheet riêng
            Sheet statsSheet = workbook.createSheet("Thống kê");
            Row statsRow1 = statsSheet.createRow(0);
            statsRow1.createCell(0).setCellValue("Tổng số giao dịch:");
            statsRow1.createCell(1).setCellValue(payments.size());

            long successCount = payments.stream()
                    .filter(p -> "SUCCESS".equals(p.getTransactionStatus()))
                    .count();

            Row statsRow2 = statsSheet.createRow(1);
            statsRow2.createCell(0).setCellValue("Giao dịch thành công:");
            statsRow2.createCell(1).setCellValue(successCount);

            double totalRevenue = payments.stream()
                    .filter(p -> "SUCCESS".equals(p.getTransactionStatus()))
                    .mapToDouble(PaymentHistory::getTransactionPrice)
                    .sum();

            Row statsRow3 = statsSheet.createRow(2);
            statsRow3.createCell(0).setCellValue("Tổng doanh thu:");
            statsRow3.createCell(1).setCellValue(totalRevenue);
            statsRow3.getCell(1).setCellStyle(amountStyle);

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }


    private PaymentDetailResponse mapToDetailedResponse(PaymentHistory payment) {
        // CẢI THIỆN: Thêm null check để tránh NPE
        return PaymentDetailResponse.builder()
                .id(payment.getId())
                .amount(BigDecimal.valueOf(payment.getTransactionPrice()))
                .status(payment.getTransactionStatus())
                .transactionCode(payment.getTransactionCode())
                .buyerName(payment.getBuyerName())
                .buyerEmail(payment.getBuyerEmail())
                .buyerPhone(payment.getBuyerPhone())
                .userId(payment.getUser() != null ? payment.getUser().getId() : null)
                .userFullName(payment.getUser() != null ? payment.getUser().getFullName() : "N/A")
                .courseId(payment.getCourse() != null ? payment.getCourse().getId() : null)
                .coursePrice(payment.getTransactionPrice())
                .courseTitle(payment.getCourse() != null ? payment.getCourse().getCourseName() : "N/A")
                .paymentMethod(payment.getPaymentMethod())
                .transactionDate(payment.getDateTransaction())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getLastModified())
                .build();
    }

}