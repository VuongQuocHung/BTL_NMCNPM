<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Admin</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="admin">
  <header class="site-header">
    <a class="logo" href="index.jsp">LaptopStore</a>
    <nav class="nav">
      <a href="products.jsp">Sản phẩm</a>
      <a href="orders.jsp">Đơn hàng</a>
      <a id="accountLink" href="login.jsp">Đăng nhập</a>
      <button id="logoutBtn" class="link-button hidden">Đăng xuất</button>
    </nav>
  </header>

  <main class="page admin-page">
    <div class="section-title">
      <div>
        <p class="eyebrow">Quản trị</p>
        <h1>Admin</h1>
      </div>
    </div>
    <p id="adminNotice" class="notice"></p>

    <div class="tabs">
      <button class="tab active" data-tab="products">Sản phẩm</button>
      <button class="tab" data-tab="brands">Thương hiệu</button>
      <button class="tab" data-tab="categories">Danh mục</button>
      <button class="tab" data-tab="vouchers">Voucher</button>
      <button class="tab" data-tab="orders">Đơn hàng</button>
      <button class="tab" data-tab="users">Người dùng</button>
    </div>

    <section class="admin-section" data-panel="products">
      <div class="actions">
        <a href="admin-product.jsp" class="button primary">Thêm sản phẩm mới</a>
      </div>
      <div id="adminProducts" class="table-wrap"></div>
    </section>

    <section class="admin-section hidden" data-panel="brands">
      <form id="adminBrandForm" class="panel form admin-form small-form">
        <h2>Thương hiệu</h2>
        <input type="hidden" name="id">
        <label>Tên <input name="name" required></label>
        <label>Logo URL <input name="logoUrl"></label>
        <button class="button primary" type="submit">Lưu</button>
      </form>
      <div id="adminBrands" class="table-wrap"></div>
    </section>

    <section class="admin-section hidden" data-panel="categories">
      <form id="adminCategoryForm" class="panel form admin-form small-form">
        <h2>Danh mục</h2>
        <input type="hidden" name="id">
        <label>Tên <input name="name" required></label>
        <label>Mô tả <textarea name="description" rows="3"></textarea></label>
        <button class="button primary" type="submit">Lưu</button>
      </form>
      <div id="adminCategories" class="table-wrap"></div>
    </section>

    <section class="admin-section hidden" data-panel="vouchers">
      <form id="adminVoucherForm" class="panel form admin-form">
        <h2>Voucher</h2>
        <input type="hidden" name="id">
        <label>Mã <input name="code" required></label>
        <label>Tên <input name="name" required></label>
        <label>Loại giảm
          <select name="discountType" required>
            <option value="PERCENTAGE">Phần trăm</option>
            <option value="FIXED_AMOUNT">Số tiền cố định</option>
          </select>
        </label>
        <label>Giá trị giảm <input name="discountValue" type="number" min="0" step="0.01" required></label>
        <label>Đơn tối thiểu <input name="minOrderAmount" type="number" min="0" step="1000"></label>
        <label>Giảm tối đa <input name="maxDiscountAmount" type="number" min="0" step="1000"></label>
        <label>Giới hạn lượt dùng <input name="usageLimit" type="number" min="0"></label>
        <label>Đã dùng <input name="usedCount" type="number" min="0" value="0"></label>
        <label>Bắt đầu <input name="startDate" type="datetime-local"></label>
        <label>Kết thúc <input name="endDate" type="datetime-local"></label>
        <label>Trạng thái
          <select name="active">
            <option value="true">Đang bật</option>
            <option value="false">Tắt</option>
          </select>
        </label>
        <label>Mô tả <textarea name="description" rows="3"></textarea></label>
        <button class="button primary" type="submit">Lưu voucher</button>
        <button class="button" type="reset">Nhập mới</button>
      </form>
      <div id="adminVouchers" class="table-wrap"></div>
    </section>

    <section class="admin-section hidden" data-panel="orders">
      <div id="adminOrders" class="table-wrap"></div>
    </section>

    <section class="admin-section hidden" data-panel="users">
      <div id="adminUsers" class="table-wrap"></div>
    </section>
  </main>
  <script src="js/app.js"></script>
</body>
</html>
