<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Quản lý sản phẩm - Admin</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="admin-product">
  <header class="site-header">
    <a class="logo" href="index.jsp">LaptopStore</a>
    <nav class="nav">
      <a href="products.jsp">Sản phẩm</a>
      <a href="orders.jsp">Đơn hàng</a>
      <a id="accountLink" href="login.jsp">Admin</a>
      <button id="logoutBtn" class="link-button">Đăng xuất</button>
    </nav>
  </header>

  <main class="page admin-page">
    <div class="section-title">
      <div>
        <p class="eyebrow">Quản trị</p>
        <h1>Sản phẩm</h1>
      </div>
      <a href="admin.jsp" class="button">Quay lại danh sách</a>
    </div>

    <p id="adminNotice" class="notice"></p>

    <section class="admin-section">
      <form id="adminProductForm" class="panel form admin-form">
        <h2>Thêm/sửa sản phẩm</h2>
        <input type="hidden" name="id">
        <label>Tên <input name="name" required></label>
        <label>Giá bán <input name="price" type="number" min="0" required></label>
        <label>Giá nhập <input name="importPrice" type="number" min="0"></label>
        <label>Tồn kho <input name="stock" type="number" min="0" required></label>
        <label>ID thương hiệu <input name="brandId" type="number"></label>
        <label>ID danh mục <input name="categoryId" type="number"></label>
        <label>Ảnh <input name="imageUrl" placeholder="/uploads/file.jpg hoặc URL"></label>
        <label>CPU <input name="cpu"></label>
        <label>RAM <input name="ram"></label>
        <label>Ổ cứng <input name="storage"></label>
        <label>Màn hình <input name="screen"></label>
        <label>Mô tả <textarea name="description" rows="3"></textarea></label>
        <div class="form-actions" style="grid-column: 1 / -1; display: flex; gap: 10px;">
          <button class="button primary" type="submit">Lưu sản phẩm</button>
          <button class="button" type="reset">Nhập mới</button>
        </div>
      </form>
    </section>
  </main>
  <script src="js/app.js"></script>
</body>
</html>
