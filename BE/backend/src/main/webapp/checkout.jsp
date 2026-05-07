<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Thanh toán</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="checkout">
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

  <main class="page">
    <h1>Thanh toán</h1>
    <p id="checkoutNotice" class="notice"></p>
    <div class="layout">
      <form id="checkoutForm" class="panel form">
        <h2>Thông tin giao hàng</h2>
        <label>Họ tên
          <input name="fullName" required>
        </label>
        <label>Email
          <input name="email" type="email" required>
        </label>
        <label>Số điện thoại
          <input name="phone" required>
        </label>
        <label>Địa chỉ nhận hàng
          <textarea name="address" rows="3" required></textarea>
        </label>
        <label>Ghi chú
          <textarea name="note" rows="3"></textarea>
        </label>
        <button class="button primary" type="submit">Xác nhận đặt hàng</button>
      </form>
      <aside class="panel summary">
        <h2>Đơn hàng</h2>
        <div id="checkoutItems" class="mini-list"></div>
        <div class="summary-row"><span>Tạm tính</span><strong id="checkoutSubtotal">0đ</strong></div>
        <div class="summary-row"><span>Giảm giá</span><strong id="checkoutDiscount">0đ</strong></div>
        <div class="summary-total"><span>Tổng cộng</span><strong id="checkoutTotal">0đ</strong></div>
      </aside>
    </div>
  </main>

  <script src="js/app.js?v=2"></script>
</body>
</html>
