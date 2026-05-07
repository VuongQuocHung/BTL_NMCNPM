<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Đăng ký</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="register">
  <main class="auth-page">
    <form id="registerForm" class="panel form auth-box">
      <a class="logo" href="index.jsp">LaptopStore</a>
      <h1>Đăng ký</h1>
      <p id="registerNotice" class="notice"></p>
      <label>Họ tên
        <input name="fullName" required>
      </label>
      <label>Email
        <input name="email" type="email" required>
      </label>
      <label>Số điện thoại
        <input name="phone" required>
      </label>
      <label>Mật khẩu
        <input name="password" type="password" required>
      </label>
      <button class="button primary full" type="submit">Tạo tài khoản</button>
      <div class="auth-links"><a href="login.jsp">Đã có tài khoản</a></div>
    </form>
  </main>
  <script src="js/app.js?v=2"></script>
</body>
</html>
