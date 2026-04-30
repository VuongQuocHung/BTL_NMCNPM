package com.ttcs.backend.service;

import com.ttcs.backend.cart.dto.CartItemResponse;
import com.ttcs.backend.cart.dto.CartResponse;
import com.ttcs.backend.entity.Cart;
import com.ttcs.backend.entity.CartItem;
import com.ttcs.backend.entity.Product;
import com.ttcs.backend.entity.ProductImage;
import com.ttcs.backend.entity.User;
import com.ttcs.backend.entity.Voucher;
import com.ttcs.backend.entity.VoucherDiscountType;
import com.ttcs.backend.repository.CartRepository;
import com.ttcs.backend.repository.ProductRepository;
import com.ttcs.backend.repository.UserRepository;
import com.ttcs.backend.repository.VoucherRepository;
import com.ttcs.backend.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String SESSION_CART_KEY = "SESSION_CART_ITEMS";
    private static final String SESSION_VOUCHER_KEY = "SESSION_CART_VOUCHER";

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;

    @Transactional
    public CartResponse getCurrentCart(HttpServletRequest request) {
        Optional<Long> userId = SecurityUtils.getCurrentUserId();
        if (userId.isPresent()) {
            Cart cart = cartRepository.findWithItemsByUserId(userId.get()).orElse(null);
            return buildDatabaseCartResponse(cart, "database", null);
        }

        return buildSessionCartResponse(request, sessionItems(request, false), "session", null);
    }

    @Transactional
    public CartResponse addItem(Long productId, Integer quantity, HttpServletRequest request) {
        int safeQuantity = normalizeQuantity(quantity);
        Product product = getExistingProduct(productId);

        Optional<Long> userId = SecurityUtils.getCurrentUserId();
        if (userId.isPresent()) {
            Cart cart = getOrCreateDatabaseCart(userId.get());
            CartItem item = findCartItem(cart, productId).orElse(null);
            int newQuantity = safeQuantity + (item == null ? 0 : item.getQuantity());
            validateStock(product, newQuantity);

            if (item == null) {
                cart.addItem(CartItem.builder()
                        .product(product)
                        .quantity(newQuantity)
                        .build());
            } else {
                item.setQuantity(newQuantity);
            }

            return buildDatabaseCartResponse(
                    cartRepository.save(cart),
                    "database",
                    "Product added to cart successfully."
            );
        }

        Map<Long, Integer> items = sessionItems(request, true);
        int newQuantity = items.getOrDefault(productId, 0) + safeQuantity;
        validateStock(product, newQuantity);
        items.put(productId, newQuantity);
        return buildSessionCartResponse(request, items, "session", "Product added to cart successfully.");
    }

    @Transactional
    public CartResponse updateItem(Long productId, Integer quantity, HttpServletRequest request) {
        if (quantity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity");
        }

        if (quantity < 1) {
            return removeItem(productId, request);
        }

        Product product = getExistingProduct(productId);
        validateStock(product, quantity);

        Optional<Long> userId = SecurityUtils.getCurrentUserId();
        if (userId.isPresent()) {
            Cart cart = getOrCreateDatabaseCart(userId.get());
            CartItem item = findCartItem(cart, productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product is not in cart."));
            item.setQuantity(quantity);
            return buildDatabaseCartResponse(cartRepository.save(cart), "database", "Cart quantity updated successfully.");
        }

        Map<Long, Integer> items = sessionItems(request, true);
        if (!items.containsKey(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product is not in cart.");
        }
        items.put(productId, quantity);
        return buildSessionCartResponse(request, items, "session", "Cart quantity updated successfully.");
    }

    @Transactional
    public CartResponse removeItem(Long productId, HttpServletRequest request) {
        Optional<Long> userId = SecurityUtils.getCurrentUserId();
        if (userId.isPresent()) {
            Cart cart = getOrCreateDatabaseCart(userId.get());
            findCartItem(cart, productId).ifPresent(cart::removeItem);
            return buildDatabaseCartResponse(cartRepository.save(cart), "database", "Product removed from cart.");
        }

        Map<Long, Integer> items = sessionItems(request, true);
        items.remove(productId);
        return buildSessionCartResponse(request, items, "session", "Product removed from cart.");
    }

    @Transactional
    public CartResponse clearCart(HttpServletRequest request) {
        Optional<Long> userId = SecurityUtils.getCurrentUserId();
        if (userId.isPresent()) {
            Cart cart = getOrCreateDatabaseCart(userId.get());
            cart.getItems().clear();
            cart.setAppliedVoucherCode(null);
            return buildDatabaseCartResponse(cartRepository.save(cart), "database", "Cart cleared successfully.");
        }

        sessionItems(request, true).clear();
        clearSessionVoucher(request);
        return buildSessionCartResponse(request, sessionItems(request, true), "session", "Cart cleared successfully.");
    }

    @Transactional
    public CartResponse applyVoucher(String code, HttpServletRequest request) {
        String normalizedCode = normalizeVoucherCode(code);
        Optional<Long> userId = SecurityUtils.getCurrentUserId();
        if (userId.isPresent()) {
            Cart cart = getOrCreateDatabaseCart(userId.get());
            CartResponse current = buildDatabaseCartResponse(cart, "database", null);
            if (current.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
            }
            Voucher voucher = getApplicableVoucher(normalizedCode, current.getSubtotal());
            cart.setAppliedVoucherCode(voucher.getCode());
            return buildDatabaseCartResponse(cartRepository.save(cart), "database", "Voucher applied successfully.");
        }

        Map<Long, Integer> items = sessionItems(request, true);
        CartResponse current = buildSessionCartResponse(request, items, "session", null);
        if (current.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        Voucher voucher = getApplicableVoucher(normalizedCode, current.getSubtotal());
        request.getSession(true).setAttribute(SESSION_VOUCHER_KEY, voucher.getCode());
        return buildSessionCartResponse(request, items, "session", "Voucher applied successfully.");
    }

    @Transactional
    public CartResponse removeVoucher(HttpServletRequest request) {
        Optional<Long> userId = SecurityUtils.getCurrentUserId();
        if (userId.isPresent()) {
            Cart cart = getOrCreateDatabaseCart(userId.get());
            cart.setAppliedVoucherCode(null);
            return buildDatabaseCartResponse(cartRepository.save(cart), "database", "Voucher removed successfully.");
        }

        clearSessionVoucher(request);
        return buildSessionCartResponse(request, sessionItems(request, true), "session", "Voucher removed successfully.");
    }

    private Product getExistingProduct(Long productId) {
        return productRepository.findWithDetailsById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product does not exist or was deleted."));
    }

    private Cart getOrCreateDatabaseCart(Long userId) {
        return cartRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid customer."));
                    return cartRepository.save(Cart.builder()
                            .user(user)
                            .active(true)
                            .build());
                });
    }

    private Optional<CartItem> findCartItem(Cart cart, Long productId) {
        if (cart == null || cart.getItems() == null) {
            return Optional.empty();
        }

        return cart.getItems().stream()
                .filter(item -> item.getProduct() != null && productId.equals(item.getProduct().getId()))
                .findFirst();
    }

    private int normalizeQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity");
        }
        return quantity;
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStock() == null || product.getStock() <= 0 || quantity > product.getStock()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough product stock.");
        }
    }

    private String normalizeVoucherCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher code is required");
        }
        return code.trim().toUpperCase();
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> sessionItems(HttpServletRequest request, boolean create) {
        HttpSession session = create ? request.getSession(true) : request.getSession(false);
        if (session == null) {
            return new LinkedHashMap<>();
        }

        Object current = session.getAttribute(SESSION_CART_KEY);
        if (current instanceof Map<?, ?>) {
            return (Map<Long, Integer>) current;
        }

        Map<Long, Integer> items = new LinkedHashMap<>();
        session.setAttribute(SESSION_CART_KEY, items);
        return items;
    }

    private String sessionVoucherCode(HttpServletRequest request, boolean create) {
        HttpSession session = create ? request.getSession(true) : request.getSession(false);
        if (session == null) {
            return null;
        }
        Object current = session.getAttribute(SESSION_VOUCHER_KEY);
        return current == null ? null : current.toString();
    }

    private void clearSessionVoucher(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_VOUCHER_KEY);
        }
    }

    private CartResponse buildDatabaseCartResponse(Cart cart, String storageType, String message) {
        List<CartItemResponse> items = new ArrayList<>();
        boolean changed = false;
        if (cart != null && cart.getItems() != null) {
            List<CartItem> invalidItems = new ArrayList<>();
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                if (product == null) {
                    invalidItems.add(item);
                    continue;
                }

                CartItemResponse response = buildItemResponse(product, item.getQuantity());
                if (response.isQuantityAdjusted()) {
                    if (response.getQuantity() < 1) {
                        invalidItems.add(item);
                    } else {
                        item.setQuantity(response.getQuantity());
                    }
                    changed = true;
                }
                if (response.getQuantity() > 0) {
                    items.add(response);
                }
            }
            if (!invalidItems.isEmpty()) {
                invalidItems.forEach(cart::removeItem);
                changed = true;
            }
            if (changed) {
                cartRepository.save(cart);
            }
        }

        AppliedVoucher appliedVoucher = resolveAppliedVoucher(
                cart == null ? null : cart.getAppliedVoucherCode(),
                subtotal(items)
        );
        if (cart != null && appliedVoucher.removeStoredCode()) {
            cart.setAppliedVoucherCode(null);
            cartRepository.save(cart);
        }
        return buildCartResponse(items, storageType, message, appliedVoucher);
    }

    private CartResponse buildSessionCartResponse(HttpServletRequest request, Map<Long, Integer> sessionItems, String storageType, String message) {
        List<CartItemResponse> items = new ArrayList<>();
        Map<Long, Integer> normalizedItems = new LinkedHashMap<>();

        for (Map.Entry<Long, Integer> entry : sessionItems.entrySet()) {
            productRepository.findWithDetailsById(entry.getKey()).ifPresent(product -> {
                CartItemResponse response = buildItemResponse(product, entry.getValue());
                if (response.getQuantity() > 0) {
                    items.add(response);
                    normalizedItems.put(product.getId(), response.getQuantity());
                }
            });
        }

        sessionItems.clear();
        sessionItems.putAll(normalizedItems);
        AppliedVoucher appliedVoucher = resolveAppliedVoucher(sessionVoucherCode(request, false), subtotal(items));
        if (appliedVoucher.removeStoredCode()) {
            clearSessionVoucher(request);
        }
        return buildCartResponse(items, storageType, message, appliedVoucher);
    }

    private CartItemResponse buildItemResponse(Product product, int requestedQuantity) {
        int stock = product.getStock() == null ? 0 : product.getStock();
        int actualQuantity = Math.max(0, Math.min(requestedQuantity, stock));
        boolean outOfStock = stock <= 0;
        boolean adjusted = actualQuantity != requestedQuantity;
        BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(actualQuantity));

        String warning = null;
        if (outOfStock) {
            warning = "Product is out of stock.";
        } else if (adjusted) {
            warning = "Only " + stock + " product(s) left in stock.";
        }

        return CartItemResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .imageUrl(primaryImage(product))
                .unitPrice(unitPrice)
                .stock(stock)
                .quantity(actualQuantity)
                .lineTotal(lineTotal)
                .outOfStock(outOfStock)
                .quantityAdjusted(adjusted)
                .warning(warning)
                .build();
    }

    private BigDecimal subtotal(List<CartItemResponse> items) {
        return items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartResponse buildCartResponse(List<CartItemResponse> items, String storageType, String message, AppliedVoucher appliedVoucher) {
        int totalItems = items.stream().mapToInt(CartItemResponse::getQuantity).sum();
        BigDecimal subtotal = subtotal(items);
        BigDecimal discountAmount = appliedVoucher.discountAmount();
        BigDecimal totalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        return CartResponse.builder()
                .items(items)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .voucherCode(appliedVoucher.code())
                .voucherName(appliedVoucher.name())
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .empty(items.isEmpty())
                .storageType(storageType)
                .message(message)
                .voucherMessage(appliedVoucher.message())
                .build();
    }

    private Voucher getApplicableVoucher(String code, BigDecimal subtotal) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher does not exist"));
        String error = validateVoucher(voucher, subtotal);
        if (error != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
        }
        return voucher;
    }

    private AppliedVoucher resolveAppliedVoucher(String code, BigDecimal subtotal) {
        if (code == null || code.trim().isEmpty()) {
            return AppliedVoucher.empty();
        }
        if (subtotal.signum() <= 0) {
            return AppliedVoucher.removed(null);
        }
        Optional<Voucher> voucher = voucherRepository.findByCodeIgnoreCase(code.trim());
        if (voucher.isEmpty()) {
            return AppliedVoucher.removed("Voucher is no longer available.");
        }
        String error = validateVoucher(voucher.get(), subtotal);
        if (error != null) {
            return AppliedVoucher.removed(error);
        }
        BigDecimal discountAmount = calculateDiscount(voucher.get(), subtotal);
        return new AppliedVoucher(
                voucher.get().getCode(),
                voucher.get().getName(),
                discountAmount,
                "Voucher applied successfully.",
                false
        );
    }

    private String validateVoucher(Voucher voucher, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();
        if (!voucher.isActive()) {
            return "Voucher is inactive.";
        }
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            return "Voucher is not active yet.";
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            return "Voucher has expired.";
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return "Voucher usage limit has been reached.";
        }
        if (voucher.getMinOrderAmount() != null && subtotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            return "Cart subtotal does not meet the voucher minimum order amount.";
        }
        if (calculateDiscount(voucher, subtotal).signum() <= 0) {
            return "Voucher discount is not valid for this cart.";
        }
        return null;
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal subtotal) {
        BigDecimal discount;
        if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
            discount = subtotal.multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = voucher.getDiscountValue();
        }

        if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().signum() > 0) {
            discount = discount.min(voucher.getMaxDiscountAmount());
        }
        return discount.min(subtotal).max(BigDecimal.ZERO);
    }

    private String primaryImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .findFirst()
                .or(() -> product.getImages().stream().findFirst())
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    private record AppliedVoucher(
            String code,
            String name,
            BigDecimal discountAmount,
            String message,
            boolean removeStoredCode
    ) {
        static AppliedVoucher empty() {
            return new AppliedVoucher(null, null, BigDecimal.ZERO, null, false);
        }

        static AppliedVoucher removed(String message) {
            return new AppliedVoucher(null, null, BigDecimal.ZERO, message, true);
        }
    }
}
