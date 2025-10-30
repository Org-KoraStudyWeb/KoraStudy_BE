package korastudy.be.service.impl;

import jakarta.mail.internet.MimeMessage;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.User.Account;
import korastudy.be.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPaymentConfirmation(Account account, Course course, int amount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 📨 Gửi đến người dùng
            helper.setTo(account.getEmail());
            helper.setSubject("🎓 Xác nhận thanh toán khóa học - " + course.getCourseName());

            // 💌 Nội dung email HTML
            String content = """
                    <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                        <h2 style="color:#2E86C1;">Xin chào %s,</h2>
                        <p>Cảm ơn bạn đã đăng ký khóa học <strong>%s</strong>.</p>
                        <p><b>Số tiền đã thanh toán:</b> %d VND</p>
                        <p>Thông tin khóa học:</p>
                        <ul>
                            <li><b>Mã khóa học:</b> %d</li>
                            <li><b>Mô tả:</b> %s</li>
                        </ul>
                        <p>Chúc bạn học thật hiệu quả 🎉</p>
                        <hr>
                        <p style="font-size:13px; color:gray;">
                            Trân trọng,<br>
                            <b>Đội ngũ KoraStudy</b>
                        </p>
                    </body>
                    </html>
                    """.formatted(account.getUsername(), course.getCourseName(), amount, course.getId(), course.getCourseDescription());

            helper.setText(content, true);
            mailSender.send(message);

            System.out.println("✅ Email đã gửi tới: " + account.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email xác nhận thanh toán: " + e.getMessage());
        }
    }
}