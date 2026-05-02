package controller;

import util.ApiException;
import util.JsonUtil;
import util.ServletUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Base servlet xử lý exception chung cho tất cả API servlets.
 */
public abstract class BaseServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            super.service(req, resp);
        } catch (ApiException e) {
            JsonUtil.writeError(resp, e.getStatus(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage() != null ? e.getMessage() : "Internal Server Error");
        }
    }

    /** Helper: yêu cầu authenticated, nếu chưa thì throw 401 */
    protected void requireAuth(HttpServletRequest request) {
        if (!ServletUtil.isAuthenticated(request)) {
            throw ApiException.unauthorized("Vui lòng đăng nhập");
        }
    }

    /** Helper: yêu cầu role ADMIN, nếu không thì throw 403 */
    protected void requireAdmin(HttpServletRequest request) {
        requireAuth(request);
        if (!ServletUtil.isAdmin(request)) {
            throw ApiException.forbidden("Bạn không có quyền truy cập");
        }
    }

    /** Helper: yêu cầu role CUSTOMER hoặc ADMIN */
    protected void requireCustomerOrAdmin(HttpServletRequest request) {
        requireAuth(request);
        String role = ServletUtil.getCurrentUserRole(request);
        if (!"ADMIN".equalsIgnoreCase(role) && !"CUSTOMER".equalsIgnoreCase(role)) {
            throw ApiException.forbidden("Bạn không có quyền truy cập");
        }
    }
}
