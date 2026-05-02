<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Chi tiết sản phẩm</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="product-detail">
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
    <a href="products.jsp" class="back-link">← Quay lại sản phẩm</a>
    <p id="detailNotice" class="notice"></p>
    <section id="productDetail" class="detail"></section>
    <section class="section">
      <div class="section-title">
        <h2>Đánh giá</h2>
      </div>
      <form id="reviewForm" class="panel form">
        <label>Số sao
          <select name="rating">
            <option value="5">5 sao</option>
            <option value="4">4 sao</option>
            <option value="3">3 sao</option>
            <option value="2">2 sao</option>
            <option value="1">1 sao</option>
          </select>
        </label>
        <label>Nội dung
          <textarea name="comment" rows="3" placeholder="Viết đánh giá ngắn"></textarea>
        </label>
        <button class="button primary" type="submit">Gửi đánh giá</button>
      </form>
      <div id="reviewList" class="list"></div>
    </section>
  </main>

  <script src="js/app.js"></script>
</body>
</html>
