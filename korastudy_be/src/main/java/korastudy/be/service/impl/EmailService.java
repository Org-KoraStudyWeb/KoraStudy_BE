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
    public void sendVerificationEmail(String toEmail, String verificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String verificationUrl = "http://localhost:3000/verify-email?token=" + verificationToken;

            String emailContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            .container { max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif; }
                            .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
                            .content { padding: 20px; background-color: #f9f9f9; }
                            .button { background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; display: inline-block; }
                            .footer { text-align: center; margin-top: 20px; color: #666; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h2>Xác thực Email</h2>
                            </div>
                            <div class="content">
                                <p>Xin chào,</p>
                                <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấp vào nút bên dưới để xác thực địa chỉ email của bạn:</p>
                                <p style="text-align: center;">
                                    <a href="%s" class="button">Xác thực Email</a>
                                </p>
                                <p>Nếu nút không hoạt động, bạn có thể sao chép và dán đường link sau vào trình duyệt:</p>
                                <p>%s</p>
                                <p>Link này sẽ hết hạn sau 24 giờ.</p>
                            </div>
                            <div class="footer">
                                <p>Trân trọng,<br>Đội ngũ hỗ trợ</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(verificationUrl, verificationUrl);

            helper.setTo(toEmail);
            helper.setSubject("Xác thực địa chỉ email");
            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email xác thực: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;

            // SỬA: Dùng String.format() thay vì .formatted()
            String emailContent = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            .container { max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif; }
                            .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                            .content { padding: 30px; background-color: #f9f9f9; }
                            .button { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; display: inline-block; font-weight: bold; }
                            .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                            .security-note { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h2>🔐 Đặt Lại Mật Khẩu</h2>
                            </div>
                            <div class="content">
                                <p>Xin chào,</p>
                                <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản KoraStudy của bạn.</p>
                                <p style="text-align: center; margin: 30px 0;">
                                    <a href="%s" class="button">Đặt Lại Mật Khẩu</a>
                                </p>
                                <p>Nếu nút không hoạt động, bạn có thể sao chép và dán đường link sau vào trình duyệt:</p>
                                <p style="word-break: break-all; color: #667eea;">%s</p>
                    
                                <div class="security-note">
                                    <strong>📝 Lưu ý bảo mật:</strong>
                                    <ul>
                                        <li>Link này sẽ hết hạn sau 1 giờ</li>
                                        <li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>
                                        <li>Mật khẩu mới nên có ít nhất 6 ký tự và kết hợp chữ, số</li>
                                    </ul>
                                </div>
                    
                                <p>Trân trọng,<br><strong>Đội ngũ hỗ trợ KoraStudy</strong></p>
                            </div>
                            <div class="footer">
                                <p>© 2024 KoraStudy. Tất cả quyền được bảo lưu.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """, resetUrl, resetUrl); // SỬA: Dùng String.format()

            helper.setTo(toEmail);
            helper.setSubject("🔐 Đặt Lại Mật Khẩu - KoraStudy");
            helper.setText(emailContent, true);

            mailSender.send(message);
            System.out.println("✅ Email reset password đã gửi đến: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi email reset password: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi gửi email đặt lại mật khẩu: " + e.getMessage(), e);
        }
    }


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