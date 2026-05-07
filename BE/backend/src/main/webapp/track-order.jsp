<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tra cứu đơn hàng</title>
    <link rel="stylesheet" href="assets/css/style.css">
</head>
<body>
<div class="container">
    <h2>Tra cứu đơn hàng</h2>
    <form id="trackForm">
        <input type="text" id="phone" placeholder="Số điện thoại" required>
        <button type="submit">Tra cứu</button>
    </form>
    <div id="orderResult"></div>
</div>

<script>
    document.getElementById('trackForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const phone = document.getElementById('phone').value;
        const resultDiv = document.getElementById('orderResult');
        resultDiv.innerHTML = 'Đang tra cứu...';
        
        try {
            console.log('Fetching:', `/api/orders/track?phone=${phone}`);
            const res = await fetch(`/api/orders/track?phone=${phone}`);
            const data = await res.json();
            console.log('Response:', data);
            
            if (!res.ok) throw new Error(data.message || 'Lỗi hệ thống');
            
            resultDiv.innerHTML = data.map(order => `
                <div class="order-item" style="border:1px solid #ccc; padding:10px; margin:10px 0;">
                    <h3>Đơn hàng #${order.id}</h3>
                    <p>Trạng thái: ${order.status}</p>
                    <p>Tổng tiền: ${order.totalAmount} VNĐ</p>
                </div>
            `).join('');
        } catch (err) {
            console.error('Error:', err);
            resultDiv.innerHTML = `<p style="color:red">Lỗi: ${err.message}</p>`;
        }
    });
</script>
</body>
</html>
