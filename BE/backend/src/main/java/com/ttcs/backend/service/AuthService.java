package com.ttcs.backend.service;

import com.ttcs.backend.auth.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttcs.backend.entity.Role;
import com.ttcs.backend.entity.User;
import com.ttcs.backend.repository.RoleRepository;
import com.ttcs.backend.repository.UserRepository;
import com.ttcs.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;
    private final ObjectMapper objectMapper;

    @Value("${google.client-id}")
    private String googleClientId;

    public AuthResponse register(RegisterRequest request) {
                String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

                if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email nay da duoc su dung");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("CUSTOMER").build()));

        User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone().trim())
                .role(customerRole)
                .enabled(false)                                          // Chưa xác thực
                .verificationToken(UUID.randomUUID().toString())
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        User savedUser = userRepository.save(user);

        // Gửi email xác nhận (async, không block response)
        mailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationToken());

        // Trả về thông báo thay vì token (chưa được đăng nhập)
        return AuthResponse.builder()
                .message("Đăng ký thành công! Vui lòng kiểm tra email để xác nhận tài khoản.")
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .build();
    }
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token không hợp lệ"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token đã hết hạn");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoac mat khau khong chinh xac"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản chưa được xác thực. Vui lòng kiểm tra email.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().getName()); 
        // Tạo một JWT (JSON Web Token) chứa:
        // email của user
        // role (quyền: ADMIN / CUSTOMER)
        // Token này sẽ được gửi về cho frontend để:
        // xác thực (authentication)
        // phân quyền (authorization)

        return AuthResponse.builder()
                .id(user.getId())
                .id(user.getId())
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .build();
    }

    public AuthResponse googleLogin(GoogleLoginRequest request) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google login chua duoc cau hinh");
        }

        GoogleTokenInfo tokenInfo = verifyGoogleIdToken(request.getIdToken());
        String normalizedEmail = tokenInfo.email().trim().toLowerCase(Locale.ROOT);

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("CUSTOMER").build()));

        User user = userRepository.findByEmail(normalizedEmail)
                .map(existing -> {
                    existing.setEnabled(true);
                    if (existing.getRole() == null) {
                        existing.setRole(customerRole);
                    }
                    if ((existing.getFullName() == null || existing.getFullName().isBlank()) && tokenInfo.name() != null && !tokenInfo.name().isBlank()) {
                        existing.setFullName(tokenInfo.name().trim());
                    }
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(normalizedEmail)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .fullName((tokenInfo.name() == null || tokenInfo.name().isBlank()) ? normalizedEmail : tokenInfo.name().trim())
                        .role(customerRole)
                        .enabled(true)
                        .verificationToken(null)
                        .verificationTokenExpiry(null)
                        .build()));

        String token = jwtService.generateToken(user.getEmail(), user.getRole().getName());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .build();
    }

    private GoogleTokenInfo verifyGoogleIdToken(String idToken) {
        try {
            String encodedToken = URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encodedToken))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token khong hop le");
            }

            JsonNode payload = objectMapper.readTree(response.body());
            String aud = payload.path("aud").asText("");
            String email = payload.path("email").asText("");
            String emailVerified = payload.path("email_verified").asText("");
            String name = payload.path("name").asText("");

            if (!googleClientId.equals(aud)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token khong dung client id");
            }
            if (email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Khong lay duoc email tu Google token");
            }
            if (!"true".equalsIgnoreCase(emailVerified)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email Google chua duoc xac minh");
            }

            return new GoogleTokenInfo(email, name);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Khong the xac thuc Google token", ex);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Khong the xac thuc Google token", ex);
        }
    }

    private record GoogleTokenInfo(String email, String name) {
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mat khau cu khong chinh xac");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mat khau moi va xac nhan khong khop");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung voi email nay"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        mailService.sendResetPasswordEmail(normalizedEmail, token);
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token khong hop le"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token da het han");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}
