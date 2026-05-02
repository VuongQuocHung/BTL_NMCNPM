package filter;

import util.HibernateUtil;
import model.User;
import util.JwtService;
import util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.IOException;
import java.util.List;

/**
 * JWT filter — đọc Authorization header, verify token, set user info vào request attributes.
 */
public class JwtFilter implements Filter {

    private final JwtService jwtService = JwtService.getInstance();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getPathInfo();
        if (path == null) path = "";
        String servletPath = request.getServletPath(); // e.g. /api/auth

        // Luôn cho qua — token chỉ dùng để enrich request attributes
        String jwt = parseJwt(request);
        if (jwt != null) {
            try {
                String email = jwtService.extractUsername(jwt);
                String role = jwtService.extractRole(jwt);
                if (email != null) {
                    // Lookup userId từ DB
                    Long userId = findUserIdByEmail(email);
                    if (userId != null) {
                        request.setAttribute("userEmail", email);
                        request.setAttribute("userRole", role);
                        request.setAttribute("userId", userId);
                    }
                }
            } catch (Exception ignored) {
                // Token không hợp lệ — vẫn cho request qua, chỉ không set user info
            }
        }

        chain.doFilter(req, res);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    private Long findUserIdByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT u.id FROM User u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            query.setMaxResults(1);
            List<Long> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}
