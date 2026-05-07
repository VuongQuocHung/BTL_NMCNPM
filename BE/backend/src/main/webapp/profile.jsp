<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Tài khoản</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="profile">
  <header class="site-header">
    <a class="logo" href="index.jsp">LaptopStore</a>
    <nav class="nav">
      <a href="index.jsp">Trang chủ</a>
      <a href="products.jsp">Sản phẩm</a>
      <a href="orders.jsp">Đơn hàng</a>
      <a href="cart.jsp">Giỏ hàng <span id="cartCount" class="badge">0</span></a>
      <a id="accountLink" href="login.jsp">Đăng nhập</a>
      <button id="logoutBtn" class="link-button hidden">Đăng xuất</button>
    </nav>
  </header>

  <main class="page">
    <h1>Tài khoản của tôi</h1>
    <p id="profileNotice" class="notice"></p>
    <div class="layout">
      <section id="profileInfo" class="panel"></section>
      <form id="changePasswordForm" class="panel form">
        <h2>Đổi mật khẩu</h2>
        <label>Mật khẩu cũ
          <input name="oldPassword" type="password" required>
        </label>
        <label>Mật khẩu mới
          <input name="newPassword" type="password" required>
        </label>
        <label>Nhập lại mật khẩu mới
          <input name="confirmPassword" type="password" required>
        </label>
        <button class="button primary" type="submit">Đổi mật khẩu</button>
      </form>
    </div>
  </main>
  <script src="js/app.js?v=2"></script>
</body>
</html>
