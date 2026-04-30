package com.ttcs.backend.controller;

import com.ttcs.backend.auth.dto.*;
import com.ttcs.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "Quản lý đăng nhập & đăng ký")
public class AuthController {

    private final AuthService authService;

    @Value("${front-end.url}")
    private String frontendUrl;

    private String normalizeFrontendUrl() {
        if (frontendUrl == null || frontendUrl.isBlank()) {
            return "http://localhost:3000";
        }
        return frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo một tài khoản người dùng mới cho Laptop Shop")
    @ApiResponse(responseCode = "201", description = "Đăng ký thành công")
    @ApiResponse(responseCode = "400", description = "Lỗi dữ liệu đầu vào hoặc email đã tồn tại")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Xác thực người dùng và nhận JWT token")
    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công")
    @ApiResponse(responseCode = "401", description = "Sai email hoặc mật khẩu")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    @Operation(summary = "Đăng nhập bằng Google", description = "Xác thực bằng Google idToken và trả về JWT nội bộ")
    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công")
    @ApiResponse(responseCode = "401", description = "Google token không hợp lệ")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Đăng xuất khỏi hệ thống (Front-end sẽ xóa token)")
    @ApiResponse(responseCode = "200", description = "Đăng xuất thành công")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu", description = "Thay đổi mật khẩu cho người dùng hiện tại (Yêu cầu đăng nhập)")
    @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công")
    @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không chính xác hoặc mật khẩu mới không khớp")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                 @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi email chứa reset token")
    @ApiResponse(responseCode = "200", description = "Email đã được gửi (Check console log trong mock service)")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng với email này")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Reset password email sent successfully");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu", description = "Sử dụng token để đặt lại mật khẩu mới")
    @ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công")
    @ApiResponse(responseCode = "400", description = "Token không hợp lệ hoặc đã hết hạn")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }
    @GetMapping("/verify-email")
    @Operation(summary = "Xác nhận email", description = "Xác thực tài khoản qua token trong email")
    @ApiResponse(responseCode = "302", description = "Xác thực thành công và chuyển hướng về trang đăng nhập")
    @ApiResponse(responseCode = "400", description = "Token không hợp lệ hoặc đã hết hạn")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        String baseUrl = normalizeFrontendUrl();
        try {
            authService.verifyEmail(token);
            URI location = URI.create(baseUrl + "/login.html?verified=1");
            return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
        } catch (ResponseStatusException ex) {
            URI location = URI.create(baseUrl + "/login.html?verified=0");
            return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
        }
    }
}
