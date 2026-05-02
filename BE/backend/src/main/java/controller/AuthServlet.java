package controller;

import model.dto.*;
import util.ServiceLocator;
import util.AuthService;
import util.JsonUtil;
import util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthServlet extends BaseServlet {
    private AuthService authService;

    @Override
    public void init() { authService = ServiceLocator.getInstance().authService; }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = ServletUtil.getPathInfo(req);
        switch (path) {
            case "/login" -> JsonUtil.writeJson(resp, 200, authService.login(JsonUtil.readBody(req, LoginRequest.class)));
            case "/register" -> JsonUtil.writeJson(resp, 200, authService.register(JsonUtil.readBody(req, RegisterRequest.class)));
            case "/forgot-password" -> JsonUtil.writeJson(resp, 200, authService.forgotPassword(JsonUtil.readBody(req, ForgotPasswordRequest.class)));
            case "/reset-password" -> JsonUtil.writeJson(resp, 200, authService.resetPassword(JsonUtil.readBody(req, ResetPasswordRequest.class)));
            case "/change-password" -> {
                requireAuth(req);
                Long userId = ServletUtil.getCurrentUserId(req);
                JsonUtil.writeJson(resp, 200, authService.changePassword(userId, JsonUtil.readBody(req, ChangePasswordRequest.class)));
            }
            case "/google" -> JsonUtil.writeJson(resp, 200, authService.googleLogin(JsonUtil.readBody(req, GoogleLoginRequest.class)));
            default -> JsonUtil.writeError(resp, 404, "Not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = ServletUtil.getPathInfo(req);
        if ("/verify-email".equals(path)) {
            String token = req.getParameter("token");
            String redirect = authService.verifyEmail(token);
            if (redirect.startsWith("redirect:")) {
                resp.sendRedirect(redirect.substring("redirect:".length()));
            } else {
                JsonUtil.writeMessage(resp, 200, redirect);
            }
        } else {
            JsonUtil.writeError(resp, 404, "Not found");
        }
    }
}
