<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Đơn hàng</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="orders">
  <header class="site-header">
    <a class="logo" href="index.jsp">LaptopStore</a>
    <nav class="nav">
      <a href="index.jsp">Trang chủ</a>
      <a href="products.jsp">Sản phẩm</a>
      <a href="profile.jsp">Tài khoản</a>
      <a href="cart.jsp">Giỏ hàng <span id="cartCount" class="badge">0</span></a>
      <a id="accountLink" href="login.jsp">Đăng nhập</a>
      <button id="logoutBtn" class="link-button hidden">Đăng xuất</button>
    </nav>
  </header>

  <main class="page">
    <h1>Đơn hàng của tôi</h1>
    <p id="ordersNotice" class="notice"></p>
    <section id="ordersList" class="list"></section>
  </main>
  <script src="js/app.js?v=2"></script>
</body>
</html>
