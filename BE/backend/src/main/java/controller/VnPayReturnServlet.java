package controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.OrderStatus;
import util.OrderService;
import util.ServiceLocator;
import java.io.IOException;

public class VnPayReturnServlet extends HttpServlet {
    private OrderService orderService;

    @Override
    public void init() {
        orderService = ServiceLocator.getInstance().orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String vnp_ResponseCode = req.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = req.getParameter("vnp_TxnRef");
        
        System.out.println("VNPay Callback received. Code: " + vnp_ResponseCode + ", TxnRef: " + vnp_TxnRef);

        if (vnp_TxnRef == null) {
            resp.getWriter().write("Invalid request");
            return;
        }

        String contextPath = req.getContextPath();
        if ("00".equals(vnp_ResponseCode)) {
            orderService.updateOrderStatus(Long.valueOf(vnp_TxnRef), OrderStatus.PAID);
            resp.sendRedirect(contextPath + "/orders.jsp?id=" + vnp_TxnRef + "&status=success");
        } else {
            resp.sendRedirect(contextPath + "/orders.jsp?id=" + vnp_TxnRef + "&status=failed");
        }
    }
}
