<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>VPH Laptop Store</title>
  <link rel="stylesheet" href="assets/css/style.css?v=1">
</head>
<body data-page="home">
  <header class="site-header">
    <a class="logo" href="index.jsp">LaptopStore</a>
    <nav class="nav">
      <a href="index.jsp">Trang chủ</a>
      <a href="products.jsp">Sản phẩm</a>
      <a href="cart.jsp">Giỏ hàng <span id="cartCount" class="badge">0</span></a>
      <a id="accountLink" href="login.jsp">Đăng nhập</a>
      <button id="logoutBtn" class="link-button hidden">Đăng xuất</button>
    </nav>
  </header>

  <main>
    <section class="hero">
      <div>
        <p class="eyebrow">Laptop chính hãng</p>
        <h1>Mua laptop đơn giản, rõ ràng, dễ chọn.</h1>
        <p>Danh sách sản phẩm lấy trực tiếp từ backend Servlet. Frontend này dùng JSP, CSS và JavaScript thuần.</p>
        <div class="actions">
          <a class="button primary" href="products.jsp">Xem sản phẩm</a>
          <a class="button" href="cart.jsp">Xem giỏ hàng</a>
        </div>
      </div>
      <div class="hero-box">
        <strong id="homeProductTotal">...</strong>
        <span>sản phẩm đang có</span>
      </div>
    </section>

    <section class="section">
      <div class="section-title">
        <h2>Danh mục</h2>
        <a href="products.jsp">Xem tất cả</a>
      </div>
      <div id="homeCategories" class="simple-grid"></div>
    </section>

    <section class="section">
      <div class="section-title">
        <h2>Sản phẩm nổi bật</h2>
        <a href="products.jsp">Xem thêm</a>
      </div>
      <div id="homeProducts" class="product-grid"></div>
    </section>
  </main>

  <footer class="footer">VPH Laptop Store - Static frontend</footer>
  <script src="js/app.js?v=1"></script>
</body>
</html>
