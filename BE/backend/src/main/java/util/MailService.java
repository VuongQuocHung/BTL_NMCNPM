package util;

import util.EnvConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {
    private final String username;
    private final String password;
    private final Properties props;

    public MailService() {
        this.username = EnvConfig.get("MAIL_USERNAME", "");
        this.password = EnvConfig.get("MAIL_PASSWORD", "");
        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
    }

    public void sendVerificationEmail(String email, String token) {
        new Thread(() -> {
            try {
                String verifyLink = EnvConfig.get("FRONTEND_URL", "http://localhost:8080") + "/api/auth/verify-email?token=" + token;
                String html = "<h2>Xác nhận tài khoản của bạn</h2>" +
                        "<p>Cảm ơn bạn đã đăng ký! Vui lòng click vào link bên dưới để kích hoạt tài khoản:</p>" +
                        "<a href=\"" + verifyLink + "\">Xác nhận tài khoản</a>" +
                        "<p>Link có hiệu lực trong 24 giờ.</p>";
                sendHtml(email, "Xác nhận tài khoản Laptop Shop", html);
                System.out.println("Verification email sent to: " + email);
            } catch (Exception e) {
                System.err.println("Error sending email: " + e.getMessage());
            }
        }).start();
    }

    public void sendResetPasswordEmail(String email, String token) {
        new Thread(() -> {
            try {
                String html = "<h2>Đặt lại mật khẩu</h2>" +
                        "<p>Token của bạn: <strong>" + token + "</strong></p>" +
                        "<p>Token có hiệu lực trong 15 phút.</p>";
                sendHtml(email, "Đặt lại mật khẩu Laptop Shop", html);
                System.out.println("Reset password email sent to: " + email);
            } catch (Exception e) {
                System.err.println("Error sending email: " + e.getMessage());
            }
        }).start();
    }

    private void sendHtml(String to, String subject, String htmlBody) throws MessagingException {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");
        message.setContent(htmlBody, "text/html; charset=UTF-8");
        Transport.send(message);
    }
}