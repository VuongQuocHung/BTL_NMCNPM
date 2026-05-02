package util;

import model.dto.*;
import util.EnvConfig;
import dao.RoleDao;
import dao.UserDao;
import model.Role;
import model.User;
import util.JwtService;
import util.PasswordUtil;
import util.ApiException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final JwtService jwtService;
    private final MailService mailService;
    private final String googleClientId;

    public AuthService(UserDao userDao, RoleDao roleDao, JwtService jwtService, MailService mailService) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.googleClientId = EnvConfig.get("GOOGLE_CLIENT_ID", "");
    }

    public AuthResponse login(LoginRequest req) {
        User user = userDao.findByEmail(req.getEmail())
                .orElseThrow(() -> ApiException.badRequest("Email hoặc mật khẩu không đúng"));
        if (!PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            throw ApiException.badRequest("Email hoặc mật khẩu không đúng");
        }
        String roleName = user.getRole().getName().toUpperCase(Locale.ROOT);
        String token = jwtService.generateToken(user.getEmail(), roleName);
        return AuthResponse.builder()
                .id(user.getId()).token(token).tokenType("Bearer")
                .email(user.getEmail()).fullName(user.getFullName()).role(roleName).build();
    }

    public AuthResponse register(RegisterRequest req) {
        if (userDao.existsByEmail(req.getEmail())) {
            throw ApiException.badRequest("Email đã được sử dụng");
        }
        Role customerRole = roleDao.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));
        String verificationToken = UUID.randomUUID().toString();
        User user = User.builder()
                .email(req.getEmail().trim().toLowerCase(Locale.ROOT))
                .password(PasswordUtil.encode(req.getPassword()))
                .fullName(req.getFullName()).phone(req.getPhone())
                .role(customerRole).enabled(false)
                .verificationToken(verificationToken).build();
        userDao.save(user);
        mailService.sendVerificationEmail(user.getEmail(), verificationToken);
        String roleName = user.getRole().getName().toUpperCase(Locale.ROOT);
        String token = jwtService.generateToken(user.getEmail(), roleName);
        return AuthResponse.builder()
                .id(user.getId()).token(token).tokenType("Bearer")
                .email(user.getEmail()).fullName(user.getFullName()).role(roleName)
                .message("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.").build();
    }

    public String verifyEmail(String token) {
        User user = userDao.findByVerificationToken(token).orElse(null);
        if (user == null) return "redirect:login.jsp?verified=0";
        user.setEnabled(true);
        user.setVerificationToken(null);
        userDao.merge(user);
        return "redirect:login.jsp?verified=1";
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest req) {
        User user = userDao.findByEmail(req.getEmail())
                .orElseThrow(() -> ApiException.notFound("Email không tồn tại"));
        String resetToken = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userDao.merge(user);
        mailService.sendResetPasswordEmail(user.getEmail(), resetToken);
        return Map.of("message", "Đã gửi email đặt lại mật khẩu");
    }

    public Map<String, String> resetPassword(ResetPasswordRequest req) {
        User user = userDao.findByResetToken(req.getToken())
                .orElseThrow(() -> ApiException.badRequest("Token không hợp lệ"));
        if (user.getResetTokenExpiry() != null && user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw ApiException.badRequest("Token đã hết hạn");
        }
        user.setPassword(PasswordUtil.encode(req.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userDao.merge(user);
        return Map.of("message", "Đặt lại mật khẩu thành công");
    }

    public Map<String, String> changePassword(Long userId, ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw ApiException.badRequest("Xác nhận mật khẩu không khớp");
        }
        User user = userDao.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        if (!PasswordUtil.matches(req.getOldPassword(), user.getPassword())) {
            throw ApiException.badRequest("Mật khẩu cũ không đúng");
        }
        user.setPassword(PasswordUtil.encode(req.getNewPassword()));
        userDao.merge(user);
        return Map.of("message", "Đổi mật khẩu thành công");
    }

    public AuthResponse googleLogin(GoogleLoginRequest req) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + req.getIdToken();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw ApiException.badRequest("Google token không hợp lệ");

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var payload = mapper.readTree(response.body());
            String email = payload.get("email").asText();
            String name = payload.has("name") ? payload.get("name").asText() : email;

            User user = userDao.findByEmail(email).orElseGet(() -> {
                Role customerRole = roleDao.findByName("CUSTOMER")
                        .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));
                User newUser = User.builder()
                        .email(email).fullName(name).phone("0000000000")
                        .password(PasswordUtil.encode(UUID.randomUUID().toString()))
                        .role(customerRole).enabled(true).build();
                return userDao.save(newUser);
            });

            String roleName = user.getRole().getName().toUpperCase(Locale.ROOT);
            String token = jwtService.generateToken(user.getEmail(), roleName);
            return AuthResponse.builder()
                    .id(user.getId()).token(token).tokenType("Bearer")
                    .email(user.getEmail()).fullName(user.getFullName()).role(roleName).build();
        } catch (ApiException e) { throw e; }
        catch (Exception e) { throw ApiException.badRequest("Google login thất bại: " + e.getMessage()); }
    }
}
