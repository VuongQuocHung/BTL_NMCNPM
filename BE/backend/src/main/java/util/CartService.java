package util;

import model.dto.CartItemResponse;
import model.dto.CartResponse;
import dao.CartDao;
import dao.ProductDao;
import dao.UserDao;
import dao.VoucherDao;
import model.*;
import util.ApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartService {
    private final CartDao cartDao;
    private final ProductDao productDao;
    private final UserDao userDao;
    private final VoucherDao voucherDao;

    public CartService(CartDao cartDao, ProductDao productDao, UserDao userDao, VoucherDao voucherDao) {
        this.cartDao = cartDao; this.productDao = productDao;
        this.userDao = userDao; this.voucherDao = voucherDao;
    }

    public CartResponse getCart(Long userId) {
        if (userId == null) return emptyCart("SESSION");
        Cart cart = getOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    public CartResponse addItem(Long userId, Long productId, int quantity) {
        if (userId == null) throw ApiException.unauthorized("Vui lòng đăng nhập");
        Product product = productDao.findWithDetailsById(productId)
                .orElseThrow(() -> ApiException.notFound("Sản phẩm không tồn tại"));
        Cart cart = getOrCreateCart(userId);
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId)).findFirst();
        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem item = CartItem.builder().cart(cart).product(product).quantity(quantity).build();
            cart.getItems().add(item);
        }
        cartDao.merge(cart);
        return buildCartResponse(getOrCreateCart(userId));
    }

    public CartResponse updateItem(Long userId, Long productId, int quantity) {
        if (userId == null) throw ApiException.unauthorized("Vui lòng đăng nhập");
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Item not found in cart"));
        item.setQuantity(Math.max(1, quantity));
        cartDao.merge(cart);
        return buildCartResponse(getOrCreateCart(userId));
    }

    public CartResponse removeItem(Long userId, Long productId) {
        if (userId == null) throw ApiException.unauthorized("Vui lòng đăng nhập");
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        cartDao.merge(cart);
        return buildCartResponse(getOrCreateCart(userId));
    }

    public CartResponse clearCart(Long userId) {
        if (userId == null) throw ApiException.unauthorized("Vui lòng đăng nhập");
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cart.setAppliedVoucherCode(null);
        cartDao.merge(cart);
        return emptyCart("DATABASE");
    }

    public CartResponse applyVoucher(Long userId, String code) {
        if (userId == null) throw ApiException.unauthorized("Vui lòng đăng nhập");
        Cart cart = getOrCreateCart(userId);
        Voucher voucher = voucherDao.findByCodeIgnoreCase(code)
                .orElseThrow(() -> ApiException.badRequest("Mã voucher không tồn tại"));
        if (!voucher.isActive()) throw ApiException.badRequest("Voucher đã tắt");
        if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(LocalDateTime.now())) throw ApiException.badRequest("Voucher đã hết hạn");
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) throw ApiException.badRequest("Voucher đã hết lượt sử dụng");
        cart.setAppliedVoucherCode(voucher.getCode());
        cartDao.merge(cart);
        CartResponse resp = buildCartResponse(getOrCreateCart(userId));
        return resp;
    }

    public CartResponse removeVoucher(Long userId) {
        if (userId == null) throw ApiException.unauthorized("Vui lòng đăng nhập");
        Cart cart = getOrCreateCart(userId);
        cart.setAppliedVoucherCode(null);
        cartDao.merge(cart);
        return buildCartResponse(getOrCreateCart(userId));
    }

    private Cart getOrCreateCart(Long userId) {
        return cartDao.findWithItemsByUserId(userId).orElseGet(() -> {
            User user = userDao.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
            Cart cart = Cart.builder().user(user).items(new java.util.HashSet<>()).build();
            return cartDao.save(cart);
        });
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            BigDecimal unitPrice = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            int qty = ci.getQuantity();
            int stock = p.getStock() != null ? p.getStock() : 0;
            boolean outOfStock = stock <= 0;
            boolean adjusted = qty > stock && stock > 0;
            if (adjusted) { ci.setQuantity(stock); qty = stock; }
            String warning = outOfStock ? "Sản phẩm hết hàng" : adjusted ? "Đã điều chỉnh số lượng về " + stock : null;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            subtotal = subtotal.add(lineTotal);
            totalItems += qty;
            String imageUrl = p.getImages() != null ? p.getImages().stream()
                    .filter(i -> Boolean.TRUE.equals(i.getIsPrimary())).map(ProductImage::getImageUrl).findFirst()
                    .orElse(p.getImages().isEmpty() ? null : p.getImages().iterator().next().getImageUrl()) : null;
            items.add(CartItemResponse.builder()
                    .productId(p.getId()).productName(p.getName()).imageUrl(imageUrl)
                    .unitPrice(unitPrice).stock(stock).quantity(qty).lineTotal(lineTotal)
                    .outOfStock(outOfStock).quantityAdjusted(adjusted).warning(warning).build());
        }
        BigDecimal discount = BigDecimal.ZERO;
        String voucherName = null;
        String voucherCode = cart.getAppliedVoucherCode();
        if (voucherCode != null) {
            Optional<Voucher> vo = voucherDao.findByCodeIgnoreCase(voucherCode);
            if (vo.isPresent()) {
                Voucher v = vo.get();
                voucherName = v.getName();
                if (v.getMinOrderAmount() != null && subtotal.compareTo(v.getMinOrderAmount()) < 0) {
                    voucherCode = null;
                } else {
                    if (v.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
                        discount = subtotal.multiply(v.getDiscountValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
                        if (v.getMaxDiscountAmount() != null && discount.compareTo(v.getMaxDiscountAmount()) > 0) discount = v.getMaxDiscountAmount();
                    } else {
                        discount = v.getDiscountValue();
                    }
                }
            }
        }
        BigDecimal totalAmount = subtotal.subtract(discount).max(BigDecimal.ZERO);
        return CartResponse.builder()
                .items(items).totalItems(totalItems).subtotal(subtotal)
                .voucherCode(voucherCode).voucherName(voucherName).discountAmount(discount)
                .totalAmount(totalAmount).empty(items.isEmpty()).storageType("DATABASE").build();
    }

    private CartResponse emptyCart(String storageType) {
        return CartResponse.builder().items(List.of()).totalItems(0)
                .subtotal(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO).empty(true).storageType(storageType).build();
    }
}
