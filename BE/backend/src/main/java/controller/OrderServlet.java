package controller;

import util.ServiceLocator;
import model.Order;
import model.OrderStatus;
import util.OrderService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class OrderServlet extends BaseServlet {
    private OrderService orderService;
    @Override public void init() { orderService = ServiceLocator.getInstance().orderService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String subPath = ServletUtil.getSubPath(req);
        if ("track".equals(subPath)) {
            String phone = req.getParameter("phone");
            if (phone == null) {
                JsonUtil.writeError(resp, 400, "Missing phone number");
                return;
            }
            List<Order> orders = orderService.getFilteredOrders(null, null, phone, 0, 100, null, null);
            if (!orders.isEmpty()) {
                JsonUtil.writeJson(resp, 200, orders);
            } else {
                JsonUtil.writeError(resp, 404, "Không tìm thấy đơn hàng cho số điện thoại này");
            }
            return;
        }

        requireAuth(req);
        Long id = ServletUtil.getPathId(req);
        if (id != null) { JsonUtil.writeJson(resp, 200, orderService.getOrderById(id)); return; }
        int page = ServletUtil.getIntParam(req, "page", 0);
        int size = ServletUtil.getIntParam(req, "size", 10);
        String sortBy = ServletUtil.getStringParam(req, "sortBy");
        String sortDir = ServletUtil.getStringParam(req, "sortDir");
        List<Order> orders;
        if (ServletUtil.isAdmin(req)) {
            String statusStr = ServletUtil.getStringParam(req, "status");
            OrderStatus status = statusStr != null ? OrderStatus.valueOf(statusStr) : null;
            orders = orderService.getFilteredOrders(status, null, null, page, size, sortBy, sortDir);
        } else {
            orders = orderService.getOrdersByUserId(ServletUtil.getCurrentUserId(req));
        }
        JsonUtil.writeJson(resp, 200, PageResponse.of(orders, page, size));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        requireAuth(req);
        Order order = JsonUtil.readBody(req, Order.class);
        Order createdOrder = orderService.createOrder(order, ServletUtil.getCurrentUserId(req));
        
        if ("VNPAY".equalsIgnoreCase(order.getPaymentMethod())) {
            try {
                String paymentUrl = VnPayService.createPaymentUrl(
                    createdOrder.getId(), 
                    createdOrder.getTotalAmount().longValue(), 
                    "Thanh toan don hang " + createdOrder.getId(), 
                    req.getRemoteAddr()
                );
                JsonUtil.writeJson(resp, 200, Collections.singletonMap("paymentUrl", paymentUrl));
            } catch (Exception e) {
                JsonUtil.writeError(resp, 500, "Loi tao link thanh toan");
            }
        } else {
            JsonUtil.writeJson(resp, 200, createdOrder);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        requireAuth(req);
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        String subPath = ServletUtil.getSubPath(req);
        if ("status".equals(subPath)) {
            String statusParam = req.getParameter("status");
            if (statusParam == null) { JsonUtil.writeError(resp, 400, "Missing status"); return; }
            OrderStatus status = OrderStatus.valueOf(statusParam);
            JsonUtil.writeJson(resp, 200, orderService.updateOrderStatus(id, status));
        } else {
            JsonUtil.writeError(resp, 404, "Not found");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        requireAuth(req);
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        orderService.deleteOrder(id); resp.setStatus(204);
    }
}
