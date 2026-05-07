package util;

import dao.CartDao;
import dao.ProductDao;
import dao.UserDao;
import dao.VoucherDao;
import jakarta.servlet.http.HttpSession;
import model.Cart;
import model.CartItem;
import model.Product;
import model.ProductImage;
import model.User;
import model.Voucher;
import model.dto.CartItemResponse;
import model.dto.CartResponse;
import model.dto.VoucherEligibilityResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CartService {
    private static final String SESSION_CART_KEY = "cart";

    private final CartDao cartDao;
    private final ProductDao productDao;
    private final UserDao userDao;
    private final VoucherDao voucherDao;

    public CartService(CartDao cartDao, ProductDao productDao, UserDao userDao, VoucherDao voucherDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.userDao = userDao;
        this.voucherDao = voucherDao;
    }

    /**
     * UC-2.4 - Xem chi tiet gio hang.
     * CartDetailView goi CartController, sau do controller lay gio hien tai va tinh lai gia/tong tien.
     */
    public CartResponse viewCartDetail(HttpSession session, Long userId) {
        Cart cart = getCurrentCart(session, userId);
        refreshCartItemsWithCurrentProducts(cart);
        return saveAndBuildResponse(session, userId, cart, null);
    }

    /**
     * UC-2.1 - Them san pham vao gio hang.
     * ProductListView gui productId, Cart tim CartItem cu hoac tao CartItem moi voi quantity = 1.
     */
    public CartResponse addProductToCart(HttpSession session, Long userId, Long productId, Integer quantity) {
        int addQuantity = defaultAddQuantity(quantity);
        Product product = getProductById(productId);
        Cart cart = getCurrentCart(session, userId);
        CartItem cartItem = cart.findCartItem(productId).orElse(null);
        int newQuantity = (cartItem == null ? 0 : cartItem.getQuantity()) + addQuantity;

        ensureStockAvailable(productId, newQuantity);

        if (cartItem == null) {
            cartItem = CartItem.builder()
                    .product(product)
                    .quantity(newQuantity)
                    .build();
            cart.addItem(cartItem);
        } else {
            cartItem.setProduct(product);
            cartItem.setQuantity(newQuantity);
        }

        cartItem.setUnitPrice(product.getPrice());
        cartItem.calculateLineTotal();
        return saveAndBuildResponse(session, userId, cart, "Thêm sản phẩm vào giỏ hàng thành công.");
    }

    /**
     * UC-2.3 - Cap nhat so luong san pham trong gio hang.
     * CartItem duoc kiem tra ton kho truoc khi setQuantity va tinh lai thanh tien.
     */
    public CartResponse updateCartItemQuantity(HttpSession session, Long userId, Long productId, Integer newQuantity) {
        int requestedQuantity = requireValidQuantity(newQuantity);
        Cart cart = getCurrentCart(session, userId);
        CartItem cartItem = findCartItem(cart, productId);
        Product product = getProductById(productId);
        int stock = product.getStock() != null ? product.getStock() : 0;
        if (stock <= 0) {
            throw ApiException.badRequest("Sản phẩm đã hết hàng.");
        }

        String message = null;
        int acceptedQuantity = requestedQuantity;
        if (!productDao.checkStock(productId, requestedQuantity)) {
            acceptedQuantity = stock;
            message = "Số lượng sản phẩm trong kho chỉ còn " + stock + " sản phẩm. Đã điều chỉnh về mức tối đa.";
        }

        cartItem.setProduct(product);
        cartItem.setUnitPrice(product.getPrice());
        cartItem.setQuantity(acceptedQuantity);
        cartItem.calculateLineTotal();

        return saveAndBuildResponse(session, userId, cart, message);
    }

    /**
     * UC-2.2 - Xoa mot san pham khoi gio hang.
     * Cart phai tim thay CartItem truoc khi removeItem de dung business rule chi xoa item ton tai.
     */
    public CartResponse removeProductFromCart(HttpSession session, Long userId, Long productId) {
        Cart cart = getCurrentCart(session, userId);
        CartItem cartItem = findCartItem(cart, productId);
        cart.removeItem(cartItem);
        return saveAndBuildResponse(session, userId, cart, "Đã xóa sản phẩm khỏi giỏ hàng.");
    }

    /**
     * UC-2.2 alternative flow - Xoa tat ca san pham trong gio hang.
     */
    public CartResponse clearCart(HttpSession session, Long userId) {
        Cart cart = getCurrentCart(session, userId);
        cart.getItems().clear();
        cart.setAppliedVoucherCode(null);
        return saveAndBuildResponse(session, userId, cart, "Đã xóa toàn bộ giỏ hàng.");
    }

    /**
     * UC-2.5 - Hien thi ApplyVoucherView.
     * CartController lay temporaryTotal roi danh dau voucher du/khong du dieu kien.
     */
    public List<VoucherEligibilityResponse> showAvailableVouchers(HttpSession session, Long userId) {
        if (userId == null) {
            return List.of();
        }
        Cart cart = getCurrentCart(session, userId);
        refreshCartItemsWithCurrentProducts(cart);
        cart.calculateTotal();
        if (cart.getItems().isEmpty()) {
            return List.of();
        }
        BigDecimal temporaryTotal = cart.getSubtotal();

        return voucherDao.getVouchersForCustomer(userId).stream()
                .map(voucher -> toVoucherEligibility(voucher, temporaryTotal))
                .toList();
    }

    /**
     * UC-2.5 - Ap dung ma giam gia vao gio hang.
     * Controller phai tinh temporaryTotal hien tai truoc khi validate voucher.
     */
    public CartResponse applyVoucherToCart(HttpSession session, Long userId, String voucherCode) {
        if (userId == null) {
            throw ApiException.unauthorized("Vui lòng đăng nhập để áp dụng voucher.");
        }
        if (voucherCode == null || voucherCode.isBlank()) {
            throw ApiException.badRequest("Voucher code is required");
        }

        Cart cart = getCurrentCart(session, userId);
        refreshCartItemsWithCurrentProducts(cart);
        cart.calculateTotal();
        if (cart.getItems().isEmpty()) {
            throw ApiException.badRequest("Giỏ hàng đang trống.");
        }

        Voucher voucher = voucherDao.getVoucherByCode(voucherCode.trim())
                .orElseThrow(() -> ApiException.badRequest("Mã voucher không tồn tại."));
        String invalidReason = getVoucherInvalidReason(voucher, cart.getSubtotal());
        if (invalidReason != null) {
            throw ApiException.badRequest(invalidReason);
        }

        cart.setAppliedVoucherCode(voucher.getCode());
        return saveAndBuildResponse(session, userId, cart, "Đã áp dụng voucher.");
    }

    public CartResponse removeVoucherFromCart(HttpSession session, Long userId) {
        Cart cart = getCurrentCart(session, userId);
        cart.setAppliedVoucherCode(null);
        return saveAndBuildResponse(session, userId, cart, "Đã gỡ voucher.");
    }

    /*
     * - Khach chua dang nhap: gio hang nam trong HttpSession.
     * - Khach da dang nhap: gio hang nam trong database.
     */
    private Cart getCurrentCart(HttpSession session, Long userId) {
        if (userId == null) {
            Cart sessionCart = (Cart) session.getAttribute(SESSION_CART_KEY);
            if (sessionCart == null) {
                sessionCart = Cart.builder()
                        .items(new HashSet<>())
                        .build();
                session.setAttribute(SESSION_CART_KEY, sessionCart);
            }
            return sessionCart;
        }

        return cartDao.getCartByUserId(userId).orElseGet(() -> {
            User user = userDao.findById(userId)
                    .orElseThrow(() -> ApiException.notFound("User not found"));
            Cart cart = Cart.builder()
                    .user(user)
                    .items(new HashSet<>())
                    .build();
            return cartDao.save(cart);
        });
    }

    private CartResponse saveAndBuildResponse(HttpSession session, Long userId, Cart cart, String message) {
        CartResponse response = buildCartResponse(cart, storageType(userId), message);
        saveCurrentCart(session, userId, cart);
        return response;
    }

    private void saveCurrentCart(HttpSession session, Long userId, Cart cart) {
        if (userId == null) {
            session.setAttribute(SESSION_CART_KEY, cart);
            return;
        }
        cartDao.saveOrUpdateCart(cart);
    }

    private void refreshCartItemsWithCurrentProducts(Cart cart) {
        List<CartItem> snapshot = new ArrayList<>(cart.getItems());
        for (CartItem item : snapshot) {
            Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
            if (productId == null) {
                cart.removeItem(item);
                continue;
            }

            Product currentProduct = productDao.getProductById(productId)
                    .orElse(null);
            if (currentProduct == null) {
                cart.removeItem(item);
                continue;
            }

            item.setProduct(currentProduct);
            item.setUnitPrice(currentProduct.getPrice());
            int stock = currentProduct.getStock() != null ? currentProduct.getStock() : 0;
            if (stock > 0 && item.getQuantity() != null && item.getQuantity() > stock) {
                item.setQuantity(stock);
            }
            item.calculateLineTotal();
        }
    }

    private CartResponse buildCartResponse(Cart cart, String storageType, String message) {
        cart.calculateTotal();
        BigDecimal temporaryTotal = cart.getSubtotal();

        String voucherMessage = null;
        Voucher appliedVoucher = null;
        String appliedVoucherCode = cart.getAppliedVoucherCode();
        if (appliedVoucherCode != null && !appliedVoucherCode.isBlank()) {
            Voucher voucher = voucherDao.getVoucherByCode(appliedVoucherCode)
                    .orElse(null);
            String invalidReason = voucher == null ? "Voucher không còn tồn tại." : getVoucherInvalidReason(voucher, temporaryTotal);
            if (invalidReason == null) {
                appliedVoucher = voucher;
            } else {
                cart.setAppliedVoucherCode(null);
                voucherMessage = invalidReason;
            }
        }

        cart.calculateTotal(appliedVoucher);

        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .toList();

        return CartResponse.builder()
                .items(items)
                .totalItems(cart.getTotalItems())
                .subtotal(cart.getSubtotal())
                .voucherCode(appliedVoucher != null ? appliedVoucher.getCode() : null)
                .voucherName(appliedVoucher != null ? appliedVoucher.getName() : null)
                .discountAmount(cart.getDiscountAmount())
                .totalAmount(cart.getTotalAmount())
                .empty(items.isEmpty())
                .storageType(storageType)
                .message(message)
                .voucherMessage(voucherMessage)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        int stock = product.getStock() != null ? product.getStock() : 0;
        boolean outOfStock = stock <= 0;
        boolean quantityAdjusted = !outOfStock && cartItem.getQuantity() != null && cartItem.getQuantity() > stock;
        String warning = outOfStock
                ? "Sản phẩm đã hết hàng."
                : quantityAdjusted ? "Đã điều chỉnh số lượng về " + stock + "." : null;

        return CartItemResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .imageUrl(firstImage(product))
                .unitPrice(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO)
                .stock(stock)
                .quantity(cartItem.getQuantity())
                .lineTotal(cartItem.getLineTotal())
                .outOfStock(outOfStock)
                .quantityAdjusted(quantityAdjusted)
                .warning(warning)
                .build();
    }

    private VoucherEligibilityResponse toVoucherEligibility(Voucher voucher, BigDecimal temporaryTotal) {
        String invalidReason = getVoucherInvalidReason(voucher, temporaryTotal);
        return VoucherEligibilityResponse.builder()
                .code(voucher.getCode())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderAmount(voucher.getMinOrderAmount())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .endDate(voucher.getEndDate())
                .eligible(invalidReason == null)
                .eligibilityMessage(invalidReason == null ? "Đủ điều kiện" : invalidReason)
                .build();
    }

    private Product getProductById(Long productId) {
        if (productId == null) {
            throw ApiException.badRequest("Thiếu sản phẩm.");
        }
        return productDao.getProductById(productId)
                .orElseThrow(() -> ApiException.notFound("Sản phẩm không tồn tại hoặc đã bị xóa."));
    }

    private CartItem findCartItem(Cart cart, Long productId) {
        return cart.findCartItem(productId)
                .orElseThrow(() -> ApiException.notFound("Sản phẩm không tồn tại trong giỏ hàng."));
    }

    private int defaultAddQuantity(Integer quantity) {
        return quantity == null || quantity < 1 ? 1 : quantity;
    }

    private int requireValidQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw ApiException.badRequest("Số lượng không hợp lệ.");
        }
        return quantity;
    }

    private void ensureStockAvailable(Long productId, int requestedQuantity) {
        int stock = productDao.getStock(productId);
        if (stock <= 0) {
            throw ApiException.badRequest("Sản phẩm đã hết hàng.");
        }
        if (!productDao.checkStock(productId, requestedQuantity)) {
            throw ApiException.badRequest("Số lượng sản phẩm trong kho chỉ còn " + stock + " sản phẩm.");
        }
    }

    private String getVoucherInvalidReason(Voucher voucher, BigDecimal temporaryTotal) {
        LocalDateTime now = LocalDateTime.now();
        if (!voucher.isActive()) {
            return "Voucher đã tắt.";
        }
        if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
            return "Voucher chưa đến thời gian sử dụng.";
        }
        if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now)) {
            return "Voucher đã hết hạn.";
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return "Voucher đã hết lượt sử dụng.";
        }
        if (voucher.getMinOrderAmount() != null && temporaryTotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            return "Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderAmount() + ".";
        }
        return null;
    }

    private String firstImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(product.getImages().iterator().next().getImageUrl());
    }

    private String storageType(Long userId) {
        return userId == null ? "SESSION" : "DATABASE";
    }
}
