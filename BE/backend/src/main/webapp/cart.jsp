<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Giỏ hàng</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="cart">
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
    <div class="section-title">
      <div>
        <p class="eyebrow">Đơn hàng</p>
        <h1>Giỏ hàng</h1>
      </div>
      <button id="clearCartBtn" class="button danger">Xóa tất cả</button>
    </div>
    <p id="cartNotice" class="notice"></p>
    <div class="layout">
      <section id="cartItems" class="list"></section>
      <aside class="panel summary">
        <h2>Tổng tiền</h2>
        <div class="summary-row"><span>Số lượng</span><strong id="summaryItems">0</strong></div>
        <div class="summary-row"><span>Tạm tính</span><strong id="summarySubtotal">0đ</strong></div>
        <div class="summary-row"><span>Phí giao hàng</span><strong>Miễn phí</strong></div>
        <form id="voucherForm" class="voucher-form">
          <label>Mã giảm giá
            <span class="voucher-input">
              <input name="code" placeholder="NHAPMA" autocomplete="off">
              <button class="button" type="submit">Áp dụng</button>
            </span>
          </label>
        </form>
        <div id="availableVouchers" class="voucher-list"></div>
        <div id="voucherInfo" class="voucher-info hidden"></div>
        <button id="removeVoucherBtn" class="link-button hidden" type="button">Gỡ voucher</button>
        <div class="summary-row"><span>Giảm giá</span><strong id="summaryDiscount">0đ</strong></div>
        <div class="summary-total"><span>Tổng cộng</span><strong id="summaryTotal">0đ</strong></div>
        <a class="button primary full" href="checkout.jsp">Thanh toán</a>
      </aside>
    </div>
  </main>

  <script src="js/app.js?v=2"></script>
</body>
</html>
