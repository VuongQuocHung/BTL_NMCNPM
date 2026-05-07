<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>So sánh sản phẩm</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="compare">
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
        <p class="eyebrow">So sánh</p>
        <h1>So sánh sản phẩm</h1>
      </div>
      <a class="button" href="products.jsp">Quay lại danh sách</a>
    </div>
    <p id="compareNotice" class="notice"></p>
    <div id="compareTable" class="panel"></div>
  </main>

  <script src="js/app.js"></script>
</body>
</html>
