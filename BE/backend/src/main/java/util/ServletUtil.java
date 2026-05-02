package util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility để extract thông tin từ request.
 */
public class ServletUtil {

    /**
     * Lấy path info phía sau servlet mapping.
     * Ví dụ: request tới /api/products/5 → trả về "/5"
     */
    public static String getPathInfo(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return pathInfo == null ? "/" : pathInfo;
    }

    /**
     * Lấy ID từ path, ví dụ: /5 → 5
     * Trả về null nếu path không chứa ID hợp lệ.
     */
    public static Long getPathId(HttpServletRequest request) {
        String path = getPathInfo(request);
        if (path.equals("/") || path.isEmpty()) return null;
        String idStr = path.startsWith("/") ? path.substring(1) : path;
        // Nếu có thêm sub-path thì bỏ qua (ví dụ: /5/status → lấy 5)
        int slash = idStr.indexOf('/');
        if (slash > 0) idStr = idStr.substring(0, slash);
        try {
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Lấy sub-path sau ID. Ví dụ: /5/status → "status"
     */
    public static String getSubPath(HttpServletRequest request) {
        String path = getPathInfo(request);
        if (path.equals("/") || path.isEmpty()) return null;
        String rest = path.startsWith("/") ? path.substring(1) : path;
        int slash = rest.indexOf('/');
        if (slash < 0) return null;
        return rest.substring(slash + 1);
    }

    /** Lấy param dạng int từ request */
    public static int getIntParam(HttpServletRequest request, String name, int defaultValue) {
        String val = request.getParameter(name);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Lấy param dạng Long (nullable) từ request */
    public static Long getLongParam(HttpServletRequest request, String name) {
        String val = request.getParameter(name);
        if (val == null || val.isBlank()) return null;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Lấy param dạng String (nullable, trimmed) từ request */
    public static String getStringParam(HttpServletRequest request, String name) {
        String val = request.getParameter(name);
        if (val == null || val.isBlank()) return null;
        return val.trim();
    }

    /** Lấy user email từ request attribute (set bởi JwtFilter) */
    public static String getCurrentUserEmail(HttpServletRequest request) {
        Object email = request.getAttribute("userEmail");
        return email != null ? email.toString() : null;
    }

    /** Lấy user role từ request attribute (set bởi JwtFilter) */
    public static String getCurrentUserRole(HttpServletRequest request) {
        Object role = request.getAttribute("userRole");
        return role != null ? role.toString() : null;
    }

    /** Lấy user ID từ request attribute (set bởi JwtFilter) */
    public static Long getCurrentUserId(HttpServletRequest request) {
        Object id = request.getAttribute("userId");
        if (id instanceof Long) return (Long) id;
        if (id instanceof Number) return ((Number) id).longValue();
        return null;
    }

    /** Kiểm tra user có role ADMIN không */
    public static boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "ADMIN".equalsIgnoreCase(role);
    }

    /** Kiểm tra user đã authenticated chưa */
    public static boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUserEmail(request) != null;
    }
}
