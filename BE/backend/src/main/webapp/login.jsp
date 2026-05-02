<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Đăng nhập</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body data-page="login">
  <main class="auth-page">
    <form id="loginForm" class="panel form auth-box">
      <a class="logo" href="index.jsp">LaptopStore</a>
      <h1>Đăng nhập</h1>
      <p id="loginNotice" class="notice"></p>
      <label>Email
        <input name="email" type="email" required>
      </label>
      <label>Mật khẩu
        <input name="password" type="password" required>
      </label>
      <button class="button primary full" type="submit">Đăng nhập</button>
      <div class="auth-links">
        <a href="register.jsp">Tạo tài khoản</a>
        <a href="forgot-password.jsp">Quên mật khẩu</a>
      </div>
    </form>
  </main>
  <script src="js/app.js"></script>
</body>
</html>
