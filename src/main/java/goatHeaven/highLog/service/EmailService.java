package goatHeaven.highLog.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[HighLog] 이메일 인증 번호");
            helper.setText(buildOtpEmailContent(otp), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    private String buildOtpEmailContent(String otp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #333;">HighLog 이메일 인증</h2>
                        <p style="color: #666;">안녕하세요, HighLog입니다.</p>
                        <p style="color: #666;">아래 인증 번호를 입력하여 이메일 인증을 완료해주세요.</p>
                        <div style="background: #007bff; color: white; padding: 15px 30px; font-size: 24px; font-weight: bold; text-align: center; border-radius: 5px; margin: 20px 0;">
                            %s
                        </div>
                        <p style="color: #999; font-size: 12px;">이 인증 번호는 3분간 유효합니다.</p>
                    </div>
                </body>
                </html>
                """.formatted(otp);
    }
}
