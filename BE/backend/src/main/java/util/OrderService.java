package util;

import dao.OrderDao;
import dao.ProductDao;
import dao.UserDao;
import model.*;
import util.ApiException;
import java.util.List;

public class OrderService {
    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final UserDao userDao;

    public OrderService(OrderDao orderDao, ProductDao productDao, UserDao userDao) {
        this.orderDao = orderDao; this.productDao = productDao; this.userDao = userDao;
    }

    public List<Order> getFilteredOrders(OrderStatus status, Long userId, String phone,
                                          int page, int size, String sortBy, String sortDir) {
        return orderDao.findFiltered(status, userId, phone, null, null, page, size, sortBy, sortDir);
    }

    public List<Order> getOrdersByUserId(Long userId) { return orderDao.findByUserId(userId); }

    public Order getOrderById(Long id) {
        return orderDao.findWithDetailsById(id).orElseThrow(() -> ApiException.notFound("Order not found"));
    }

    public Order createOrder(Order order, Long userId) {
        User user = userDao.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(order.getPaymentMethod());
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) { detail.setOrder(order); }
        }
        return orderDao.save(order);
    }

    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = getOrderById(id);
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        // Trừ kho khi duyệt đơn
        if (status == OrderStatus.APPROVED && oldStatus != OrderStatus.APPROVED && order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Product product = productDao.findWithDetailsById(detail.getProduct().getId())
                        .orElseThrow(() -> ApiException.notFound("Product not found"));
                int newStock = (product.getStock() != null ? product.getStock() : 0) - detail.getQuantity();
                if (newStock < 0) throw ApiException.badRequest("Sản phẩm " + product.getName() + " không đủ hàng trong kho");
                product.setStock(newStock);
                productDao.merge(product);
            }
        }
        return orderDao.merge(order);
    }

    public void deleteOrder(Long id) { orderDao.delete(getOrderById(id)); }
}
