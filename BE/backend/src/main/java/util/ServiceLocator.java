package util;

import dao.*;
import util.*;
import util.JwtService;

/**
 * Poor-man's DI container — singleton quản lý tất cả DAO và Service instances.
 */
public class ServiceLocator {
    private static final ServiceLocator INSTANCE = new ServiceLocator();

    // DAOs
    public final UserDao userDao = new UserDao();
    public final RoleDao roleDao = new RoleDao();
    public final ProductDao productDao = new ProductDao();
    public final BrandDao brandDao = new BrandDao();
    public final CategoryDao categoryDao = new CategoryDao();
    public final CartDao cartDao = new CartDao();
    public final OrderDao orderDao = new OrderDao();
    public final ReviewDao reviewDao = new ReviewDao();
    public final VoucherDao voucherDao = new VoucherDao();

    // Services
    public final JwtService jwtService = JwtService.getInstance();
    public final MailService mailService = new MailService();
    public final FileStorageService fileStorageService = new FileStorageService();
    public final AuthService authService;
    public final ProductService productService;
    public final BrandService brandService;
    public final CategoryService categoryService;
    public final CartService cartService;
    public final OrderService orderService;
    public final UserService userService;
    public final ReviewService reviewService;
    public final VoucherService voucherService;
    public final RoleService roleService;

    private ServiceLocator() {
        authService = new AuthService(userDao, roleDao, jwtService, mailService);
        productService = new ProductService(productDao);
        brandService = new BrandService(brandDao);
        categoryService = new CategoryService(categoryDao);
        cartService = new CartService(cartDao, productDao, userDao, voucherDao);
        orderService = new OrderService(orderDao, productDao, userDao);
        userService = new UserService(userDao, orderDao, reviewDao);
        reviewService = new ReviewService(reviewDao);
        voucherService = new VoucherService(voucherDao);
        roleService = new RoleService(roleDao);
    }

    public static ServiceLocator getInstance() {
        return INSTANCE;
    }
}
