<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Sản phẩm - VPH Laptop</title>
  <link rel="stylesheet" href="assets/css/style.css?v=1">
</head>
<body data-page="products">
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
        <p class="eyebrow">Cửa hàng</p>
        <h1>Danh sách sản phẩm</h1>
      </div>
      <span id="productTotal" class="muted"></span>
    </div>

    <div class="layout">
      <aside class="panel">
        <h2>Bộ lọc</h2>
        <form id="productFilterForm" class="form">
          <label>Tìm theo tên
            <input name="name" placeholder="VD: Dell, Asus, gaming">
          </label>
          <label>Danh mục
            <select name="categoryId" id="categoryFilter"></select>
          </label>
          <label>Thương hiệu
            <select name="brandId" id="brandFilter"></select>
          </label>
          <label>Giá từ
            <input name="minPrice" type="number" min="0">
          </label>
          <label>Giá đến
            <input name="maxPrice" type="number" min="0">
          </label>
          <button class="button primary" type="submit">Lọc sản phẩm</button>
          <a class="button center" href="products.jsp">Xóa lọc</a>
        </form>
      </aside>

      <section>
        <div id="compareBar" class="panel compare-bar">
          <div>
            <strong>So sánh sản phẩm</strong>
            <p id="compareNotice" class="notice"></p>
          </div>
          <div class="actions">
            <span id="compareCount" class="muted">0/3 đã chọn</span>
            <button id="compareBtn" class="button" type="button" disabled>So sánh</button>
            <button id="compareResetBtn" class="button" type="button">Xóa chọn</button>
          </div>
        </div>
        <div class="panel" style="margin-bottom: 14px;">
          <form id="productSortForm" class="form" style="grid-template-columns: repeat(2, minmax(0, 1fr));">
            <label>Sắp xếp theo giá
              <select name="sortDir" id="priceSort">
                <option value="">Mặc định</option>
                <option value="asc">Từ thấp đến cao</option>
                <option value="desc">Từ cao đến thấp</option>
              </select>
            </label>
          </form>
        </div>
        <p id="productNotice" class="notice"></p>
        <div id="productList" class="product-grid"></div>
        <div class="pager">
          <button id="prevPage" class="button">Trang trước</button>
          <span id="pageInfo"></span>
          <button id="nextPage" class="button">Trang sau</button>
        </div>
      </section>
    </div>
  </main>

  <footer class="footer">VPH Laptop Store</footer>
  <script src="js/app.js?v=2"></script>
</body>
</html>
