const App = (() => {
  const fallbackImage = "data:image/svg+xml;charset=utf-8," + encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="480" height="360">
      <rect width="100%" height="100%" fill="#eef2f7"/>
      <rect x="92" y="92" width="296" height="176" rx="12" fill="#ffffff" stroke="#cbd5e1" stroke-width="8"/>
      <rect x="132" y="124" width="216" height="96" rx="6" fill="#dbeafe"/>
      <rect x="106" y="280" width="268" height="22" rx="8" fill="#cbd5e1"/>
      <text x="240" y="330" text-anchor="middle" font-family="Arial" font-size="22" fill="#64748b">Laptop</text>
    </svg>
  `);

  const contextPath = () => {
    const script = document.currentScript
      || Array.from(document.scripts).find((item) => item.src && item.src.includes("/js/app.js"));

    // The app can be deployed as /backend, so derive the context from /backend/js/app.js.
    if (script?.src) {
      const scriptPath = new URL(script.src, location.href).pathname;
      const marker = "/js/app.js";
      const markerIndex = scriptPath.lastIndexOf(marker);
      if (markerIndex >= 0) {
        return scriptPath.substring(0, markerIndex);
      }
    }

    const path = location.pathname;
    const jspIndex = path.lastIndexOf(".jsp");
    if (jspIndex >= 0) {
      return path.substring(0, path.lastIndexOf("/", jspIndex));
    }
    return path.replace(/\/$/, "");
  };

  const apiBase = () => {
    const detectedContext = contextPath();
    const detectedBase = `${location.origin}${detectedContext}`;
    const saved = localStorage.getItem("api.base");

    if (!saved) return detectedBase;

    try {
      const savedUrl = new URL(saved, location.origin);
      const savedPath = savedUrl.pathname.replace(/\/$/, "");

      // Ignore stale same-origin values like http://localhost:8080 or /api when the JSP runs under /backend.
      if (savedUrl.origin === location.origin && savedPath !== detectedContext) {
        localStorage.setItem("api.base", detectedBase);
        return detectedBase;
      }

      return `${savedUrl.origin}${savedPath}`;
    } catch {
      localStorage.removeItem("api.base");
      return detectedBase;
    }
  };
  console.log("API Base URL:", apiBase());

  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));
  const params = new URLSearchParams(location.search);
  let currentCartState = null;
  let activeVouchers = [];
  const compareState = { ids: new Set(), categoryId: null };
  const compareStorageKey = "compare.ids";
  const money = (value) => new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(Number(value || 0));

  const html = (value) => String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");

  const token = () => localStorage.getItem("auth.token");
  const currentUser = () => {
    try {
      return JSON.parse(localStorage.getItem("auth.user") || "null");
    } catch {
      return null;
    }
  };

  async function request(path, options = {}) {
    const headers = new Headers(options.headers || {});
    const body = options.body;

    if (body && !(body instanceof FormData)) {
      headers.set("Content-Type", "application/json");
    }

    if (token()) {
      headers.set("Authorization", `Bearer ${token()}`);
    }

    const response = await fetch(`${apiBase()}${path}`, {
      credentials: "include",
      ...options,
      body: body instanceof FormData ? body : body ? JSON.stringify(body) : undefined,
      headers
    });

    const text = await response.text();
    let data = null;
    try {
      data = text ? JSON.parse(text) : null;
    } catch {
      data = text;
    }

    if (!response.ok) {
      const fieldErrors = data && typeof data === "object" ? data.fieldErrors : null;
      const firstFieldError = fieldErrors ? Object.values(fieldErrors).find(Boolean) : null;
      throw new Error(firstFieldError || data?.message || data?.error || response.statusText || "Có lỗi xảy ra");
    }

    return data;
  }

  const pageContent = (data) => Array.isArray(data) ? data : (data?.content || []);
  const firstImage = (product) => product?.images?.find((x) => x.isPrimary)?.imageUrl || product?.images?.[0]?.imageUrl;
  const imageUrl = (url) => {
    if (!url) return fallbackImage;
    if (/^https?:\/\//i.test(url) || /^\/\//.test(url) || url.startsWith("data:") || url.startsWith("blob:")) return url;
    if (url.startsWith("/")) return `${apiBase()}${url}`;
    return `${apiBase()}/${url}`;
  };

  function setNotice(id, message, type = "") {
    const el = typeof id === "string" ? $(id) : id;
    if (!el) return;
    el.textContent = message || "";
    el.className = `notice ${type}`.trim();
  }

  function formData(form) {
    return Object.fromEntries(new FormData(form).entries());
  }

  function loadCompareIds() {
    const raw = localStorage.getItem(compareStorageKey);
    if (!raw) return [];
    return raw.split(",").map((x) => Number(x)).filter((x) => Number.isFinite(x) && x > 0);
  }

  function saveCompareIds(ids) {
    const value = Array.from(ids).join(",");
    localStorage.setItem(compareStorageKey, value);
  }

  function requireLogin() {
    if (!token()) {
      location.href = "login.jsp";
      return false;
    }
    return true;
  }

  function updateHeader() {
    const user = currentUser();
    const accountLink = $("#accountLink");
    const logoutBtn = $("#logoutBtn");

    if (accountLink && user) {
      const isAdmin = user.role === "ADMIN" || user.role === "ROLE_ADMIN";
      accountLink.textContent = isAdmin ? "Admin" : "Tài khoản";
      accountLink.href = isAdmin ? "admin.jsp" : "profile.jsp";
    }

    if (logoutBtn) {
      logoutBtn.classList.toggle("hidden", !user);
      logoutBtn.addEventListener("click", () => {
        localStorage.removeItem("auth.token");
        localStorage.removeItem("auth.user");
        location.href = "index.jsp";
      });
    }
  }

  async function refreshCartCount() {
    const badge = $("#cartCount");
    if (!badge) return;
    try {
      const cart = await request("/api/cart");
      badge.textContent = cart?.totalItems || 0;
    } catch {
      badge.textContent = "0";
    }
  }

  function productCard(product) {
    const specs = product.specification || {};
    return `
      <article class="card">
        <a class="product-image" href="product.jsp?id=${product.id}">
          <img src="${imageUrl(firstImage(product))}" alt="${html(product.name || "Sản phẩm")}">
        </a>
        <label class="compare-toggle">
          <input type="checkbox" data-compare-id="${product.id}" data-compare-category="${product.category?.id || ""}">
          <span>So sánh</span>
        </label>
        <div>
          <h3><a href="product.jsp?id=${product.id}">${html(product.name || "Không tên")}</a></h3>
          <p class="muted">${html(product.brand?.name || "")} ${product.category?.name ? "- " + html(product.category.name) : ""}</p>
        </div>
        <div class="specs">
          ${specs.cpu ? `<span class="chip">${html(specs.cpu)}</span>` : ""}
          ${specs.ram ? `<span class="chip">${html(specs.ram)}</span>` : ""}
          ${specs.storage ? `<span class="chip">${html(specs.storage)}</span>` : ""}
        </div>
        <div class="card-footer">
          <div class="price">${money(product.price)}</div>
          <button class="button primary full" data-add-cart="${product.id}">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 8px;"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>
            Thêm vào giỏ
          </button>
        </div>
      </article>
    `;
  }

  // UC-2.1: ProductListView/ProductDetailView gui request them san pham voi quantity mac dinh = 1.
  function bindAddProductToCart(root = document) {
    if (!root) return;
    $$("[data-add-cart]", root).forEach((button) => {
      button.addEventListener("click", async () => {
        button.disabled = true;
        try {
          const updatedCart = await request("/api/cart/items", {
            method: "POST",
            body: { productId: Number(button.dataset.addCart), quantity: 1 }
          });
          await refreshCartCount();
          button.textContent = updatedCart?.message || "Đã thêm";
          setTimeout(() => button.textContent = "Thêm vào giỏ", 1200);
        } catch (error) {
          alert(error.message);
        } finally {
          button.disabled = false;
        }
      });
    });
  }

  async function initHome() {
    const [products, categories] = await Promise.all([
      request("/api/products?size=8&sortBy=id&sortDir=desc"),
      request("/api/categories?size=3")
    ]);

    $("#homeProductTotal").textContent = products.totalElements ?? pageContent(products).length;
    $("#homeCategories").innerHTML = pageContent(categories).map((category) => `
      <a class="card" href="products.jsp?categoryId=${category.id}">
        <h3>${html(category.name)}</h3>
        <p class="muted">${html(category.description || "Khám phá sản phẩm")}</p>
      </a>
    `).join("");
    $("#homeProducts").innerHTML = pageContent(products).slice(0, 8).map(productCard).join("");
    bindAddProductToCart($("#homeProducts"));
  }

  async function loadFilterOptions() {
    const [categories, brands] = await Promise.all([
      request("/api/categories?size=100"),
      request("/api/brands?size=100")
    ]);
    const categoryFilter = $("#categoryFilter");
    const brandFilter = $("#brandFilter");
    if (categoryFilter) {
      categoryFilter.innerHTML = `<option value="">Tất cả</option>` + pageContent(categories).map((x) =>
        `<option value="${x.id}">${html(x.name)}</option>`
      ).join("");
      categoryFilter.value = params.get("categoryId") || "";
    }
    if (brandFilter) {
      brandFilter.innerHTML = `<option value="">Tất cả</option>` + pageContent(brands).map((x) =>
        `<option value="${x.id}">${html(x.name)}</option>`
      ).join("");
      brandFilter.value = params.get("brandId") || "";
    }
  }

  async function initProducts() {
    await loadFilterOptions();
    const page = Number(params.get("page") || 0);
    const query = new URLSearchParams();
    ["name", "brandId", "categoryId", "minPrice", "maxPrice"].forEach((key) => {
      if (params.get(key)) query.set(key, params.get(key));
    });
    const sortDir = params.get("sortDir");
    if (sortDir) {
      query.set("sortBy", "price");
      query.set("sortDir", sortDir);
    }
    query.set("page", page);
    query.set("size", "8");

    const form = $("#productFilterForm");
    if (form) {
      form.name.value = params.get("name") || "";
      form.minPrice.value = params.get("minPrice") || "";
      form.maxPrice.value = params.get("maxPrice") || "";
      form.addEventListener("submit", (event) => {
        event.preventDefault();
        const data = formData(form);
        const next = new URLSearchParams();
        Object.entries(data).forEach(([key, value]) => {
          if (String(value).trim()) next.set(key, value);
        });
        const selectedSort = params.get("sortDir");
        if (selectedSort) {
          next.set("sortBy", "price");
          next.set("sortDir", selectedSort);
        }
        location.href = `products.jsp?${next.toString()}`;
      });
    }

    const sortSelect = $("#priceSort");
    if (sortSelect) {
      sortSelect.value = params.get("sortDir") || "";
      sortSelect.addEventListener("change", () => {
        const next = new URLSearchParams(location.search);
        if (sortSelect.value) {
          next.set("sortBy", "price");
          next.set("sortDir", sortSelect.value);
        } else {
          next.delete("sortBy");
          next.delete("sortDir");
        }
        next.set("page", "0");
        location.href = `products.jsp?${next.toString()}`;
      });
    }

    setNotice("#productNotice", "Đang tải sản phẩm...");
    try {
      const result = await request(`/api/products?${query.toString()}`);
      $("#productTotal").textContent = `${result.totalElements || 0} sản phẩm`;
      $("#productList").innerHTML = pageContent(result).map(productCard).join("") || `<div class="panel">Không có sản phẩm phù hợp.</div>`;
      $("#pageInfo").textContent = `Trang ${page + 1}/${Math.max(result.totalPages || 1, 1)}`;
      $("#prevPage").disabled = page <= 0;
      $("#nextPage").disabled = page >= (result.totalPages || 1) - 1;
      $("#prevPage").onclick = () => changePage(page - 1);
      $("#nextPage").onclick = () => changePage(page + 1);
      bindAddProductToCart($("#productList"));
      bindCompare($("#productList"));
      setNotice("#productNotice", "");
    } catch (error) {
      setNotice("#productNotice", error.message, "error");
    }
  }

  function bindCompare(root = document) {
    const compareNotice = $("#compareNotice");
    const compareCount = $("#compareCount");
    const compareBtn = $("#compareBtn");
    const compareResetBtn = $("#compareResetBtn");

    if (compareState.ids.size === 0) {
      loadCompareIds().forEach((id) => compareState.ids.add(id));
    }

    const updateCompareUi = () => {
      const count = compareState.ids.size;
      if (compareCount) compareCount.textContent = `${count}/3 đã chọn`;
      if (compareBtn) compareBtn.disabled = count < 2;
      if (compareNotice && count === 0) setNotice(compareNotice, "");
    };

    $$(`[data-compare-id]`, root).forEach((input) => {
      const id = Number(input.dataset.compareId);
      const categoryId = Number(input.dataset.compareCategory || 0);
      input.checked = compareState.ids.has(id);
      if (input.checked && !compareState.categoryId) {
        compareState.categoryId = categoryId || null;
      }
      input.addEventListener("change", () => {
        if (input.checked) {
          if (compareState.ids.size >= 3) {
            input.checked = false;
            setNotice(compareNotice, "Chỉ được phép so sánh tối đa 3 sản phẩm cùng lúc", "error");
            return;
          }
          if (compareState.categoryId && compareState.categoryId !== categoryId) {
            input.checked = false;
            setNotice(compareNotice, "Vui lòng chọn các sản phẩm cùng loại để thực hiện so sánh", "error");
            return;
          }
          compareState.ids.add(id);
          compareState.categoryId = compareState.categoryId || categoryId;
        } else {
          compareState.ids.delete(id);
          if (compareState.ids.size === 0) compareState.categoryId = null;
        }
        setNotice(compareNotice, "");
        saveCompareIds(compareState.ids);
        updateCompareUi();
      });
    });

    if (compareBtn) {
      compareBtn.onclick = () => {
        if (compareState.ids.size < 2) {
          setNotice(compareNotice, "Vui lòng chọn ít nhất 2 sản phẩm", "error");
          return;
        }
        saveCompareIds(compareState.ids);
        const ids = Array.from(compareState.ids).join(",");
        location.href = `compare.jsp?ids=${ids}`;
      };
    }

    if (compareResetBtn) {
      compareResetBtn.onclick = () => {
        compareState.ids.clear();
        compareState.categoryId = null;
        saveCompareIds(compareState.ids);
        $$(`[data-compare-id]`, root).forEach((input) => {
          input.checked = false;
        });
        setNotice(compareNotice, "Đã xóa lựa chọn.", "success");
        updateCompareUi();
      };
    }

    updateCompareUi();
  }

  async function initCompare() {
    let ids = params.get("ids");
    if (!ids) {
      const stored = loadCompareIds();
      if (stored.length >= 2) {
        ids = stored.join(",");
      } else {
        setNotice("#compareNotice", "Thiếu danh sách sản phẩm để so sánh.", "error");
        return;
      }
    }
    setNotice("#compareNotice", "Đang tải dữ liệu so sánh...");
    try {
      const result = await request(`/api/products/compare?ids=${encodeURIComponent(ids)}`);
      const idList = ids.split(",").map((x) => Number(x)).filter((x) => Number.isFinite(x) && x > 0);
      const products = Array.isArray(result) ? result : pageContent(result);
      const filtered = idList.length ? products.filter((p) => idList.includes(p.id)) : products;
      saveCompareIds(idList);
      renderCompare(filtered);
      setNotice("#compareNotice", "");
    } catch (error) {
      setNotice("#compareNotice", error.message, "error");
    }
  }

  function renderCompare(products) {
    const container = $("#compareTable");
    if (!container) return;
    if (!products?.length) {
      container.innerHTML = `<div class="panel">Không có sản phẩm để so sánh.</div>`;
      return;
    }

    const specs = [
      { key: "cpu", label: "CPU" },
      { key: "ram", label: "RAM" },
      { key: "storage", label: "Ổ cứng" },
      { key: "vga", label: "Card đồ họa" },
      { key: "screen", label: "Màn hình" },
      { key: "os", label: "Hệ điều hành" },
      { key: "battery", label: "Pin" },
      { key: "weight", label: "Trọng lượng" }
    ];

    const header = `
      <tr>
        <th>Thông tin</th>
        ${products.map((p) => `<th>${html(p.name || "Sản phẩm")}</th>`).join("")}
      </tr>
    `;

    const rows = [
      { label: "Giá", values: products.map((p) => money(p.price)) },
      { label: "Thương hiệu", values: products.map((p) => html(p.brand?.name || "")) },
      { label: "Danh mục", values: products.map((p) => html(p.category?.name || "")) }
    ].map((row) => compareRow(row.label, row.values));

    specs.forEach((spec) => {
      const values = products.map((p) => html(p.specification?.[spec.key] || ""));
      rows.push(compareRow(spec.label, values));
    });

    container.innerHTML = `
      <div class="table-wrap compare-table">
        <table>
          <thead>${header}</thead>
          <tbody>${rows.join("")}</tbody>
        </table>
      </div>
    `;
  }

  function compareRow(label, values) {
    const normalized = values.map((v) => String(v || "").trim());
    const unique = new Set(normalized.filter((v) => v));
    const hasDiff = unique.size > 1;
    return `
      <tr class="${hasDiff ? "compare-diff" : ""}">
        <td><strong>${html(label)}</strong></td>
        ${values.map((v) => `<td>${v || "-"}</td>`).join("")}
      </tr>
    `;
  }

  function changePage(page) {
    params.set("page", Math.max(page, 0));
    location.href = `products.jsp?${params.toString()}`;
  }

  async function initProductDetail() {
    const id = params.get("id");
    if (!id) {
      setNotice("#detailNotice", "Thiếu ID sản phẩm.", "error");
      return;
    }

    const product = await request(`/api/products/${id}`);
    const specs = product.specification || {};
    $("#productDetail").innerHTML = `
      <div class="detail-image">
        <img src="${imageUrl(firstImage(product))}" alt="${html(product.name)}">
      </div>
      <div class="detail-info">
        <p class="eyebrow">${html(product.brand?.name || "Laptop")}</p>
        <h1>${html(product.name)}</h1>
        <p class="price">${money(product.price)}</p>
        <p class="muted">Còn ${product.stock ?? 0} sản phẩm trong kho</p>
        <p>${html(product.description || "Chưa có mô tả.")}</p>
        <div class="specs">
          ${Object.entries(specs).filter(([, v]) => v).map(([k, v]) => `<span class="chip">${html(k)}: ${html(v)}</span>`).join("")}
        </div>
        <div class="actions">
          <button class="button primary" data-add-cart="${product.id}">Thêm vào giỏ</button>
          <a class="button" href="cart.jsp">Đến giỏ hàng</a>
        </div>
      </div>
    `;
    bindAddProductToCart($("#productDetail"));
    await loadReviews(id);

    const reviewForm = $("#reviewForm");
    if (reviewForm) {
      reviewForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!requireLogin()) return;
        const data = formData(event.currentTarget);
        try {
          await request("/api/reviews", {
            method: "POST",
            body: {
              product: { id: Number(id) },
              rating: Number(data.rating),
              comment: data.comment
            }
          });
          event.currentTarget.reset();
          await loadReviews(id);
          setNotice("#detailNotice", "Đã gửi đánh giá.", "success");
        } catch (error) {
          setNotice("#detailNotice", error.message, "error");
        }
      });
    }
  }

  async function loadReviews(productId) {
    const result = await request(`/api/reviews?productId=${productId}&size=20&sortBy=id&sortDir=desc`);
    $("#reviewList").innerHTML = pageContent(result).map((review) => `
      <article class="panel">
        <strong>${"★".repeat(review.rating || 0)}${"☆".repeat(5 - (review.rating || 0))}</strong>
        <p>${html(review.comment || "")}</p>
        <p class="muted">${html(review.userFullName || review.user?.fullName || "Khách hàng")}</p>
      </article>
    `).join("") || `<div class="panel">Chưa có đánh giá.</div>`;
  }

  // UC-2.4: CartDetailView lay gio hang hien tai va hien thi lai toan bo CartItem.
  async function initCartDetailView() {
    const cart = await request("/api/cart");
    renderCartDetailView(cart);
    await showApplyVoucherView();
    const clearCartBtn = $("#clearCartBtn");
    if (clearCartBtn) {
      clearCartBtn.addEventListener("click", async () => {
        if (!confirm("Bạn chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?")) return;
        const updated = await request("/api/cart/items", { method: "DELETE" });
        renderCartDetailView(updated);
        await showApplyVoucherView();
        await refreshCartCount();
      });
    }
    const voucherForm = $("#voucherForm");
    if (voucherForm) {
      voucherForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const code = String(formData(event.currentTarget).code || "").trim();
        if (!code) {
          setNotice("#cartNotice", "Hãy nhập hoặc chọn một mã giảm giá.", "error");
          return;
        }
        await applySelectedVoucher(code);
      });
    }
    const removeVoucherBtn = $("#removeVoucherBtn");
    if (removeVoucherBtn) {
      removeVoucherBtn.addEventListener("click", async () => {
        const updated = await request("/api/cart/voucher", { method: "DELETE" });
        renderCartDetailView(updated);
        await showApplyVoucherView();
        setNotice("#cartNotice", updated.message || "Đã gỡ voucher.", "success");
      });
    }
  }

  // UC-2.5: ApplyVoucherView hien thi voucher kem trang thai du/khong du dieu kien.
  async function showApplyVoucherView() {
    const box = $("#availableVouchers");
    if (!box) return;
    try {
      const result = await request("/api/cart/vouchers");
      activeVouchers = pageContent(result);
      renderApplyVoucherView();
    } catch {
      activeVouchers = [];
      box.innerHTML = "";
    }
  }

  function renderApplyVoucherView() {
    const box = $("#availableVouchers");
    if (!box) return;
    const cart = currentCartState || {};
    const subtotal = Number(cart.subtotal || 0);
    box.innerHTML = activeVouchers.map((voucher) => {
      const minAmount = Number(voucher.minOrderAmount || 0);
      const disabled = voucher.eligible === false || minAmount > subtotal;
      const selected = cart.voucherCode === voucher.code;
      const meta = [
        voucherDiscountText(voucher),
        minAmount ? `đơn từ ${money(minAmount)}` : "không yêu cầu đơn tối thiểu",
        voucher.endDate ? `hết hạn ${formatDateTime(voucher.endDate)}` : "",
        voucher.eligible === false ? voucher.eligibilityMessage : ""
      ].filter(Boolean).join(" • ");
      return `
        <div class="voucher-option ${selected ? "selected" : ""}">
          <div>
            <strong>${html(voucher.code)}${selected ? " - đang áp dụng" : ""}</strong>
            <span>${html(meta)}</span>
          </div>
          <button class="button" type="button" data-use-voucher="${html(voucher.code)}" ${disabled || selected ? "disabled" : ""}>
            ${disabled ? "Không đủ điều kiện" : selected ? "Đã dùng" : "Áp dụng"}
          </button>
        </div>
      `;
    }).join("") || `<p class="muted">Hiện chưa có voucher đang bật.</p>`;
    $$("[data-use-voucher]", box).forEach((button) => {
      button.onclick = () => applySelectedVoucher(button.dataset.useVoucher);
    });
  }

  async function applySelectedVoucher(code) {
    try {
      const updated = await request("/api/cart/voucher", { method: "POST", body: { code } });
      renderCartDetailView(updated);
      await showApplyVoucherView();
      setNotice("#cartNotice", updated.message || "Đã áp dụng voucher.", "success");
    } catch (error) {
      setNotice("#cartNotice", error.message, "error");
    }
  }

  function renderCartDetailView(cart) {
    currentCartState = cart;
    $("#summaryItems").textContent = cart.totalItems || 0;
    $("#summarySubtotal").textContent = money(cart.subtotal);
    $("#summaryDiscount").textContent = `-${money(cart.discountAmount || 0)}`;
    $("#summaryTotal").textContent = money(cart.totalAmount);
    const voucherForm = $("#voucherForm");
    const voucherInfo = $("#voucherInfo");
    const removeVoucherBtn = $("#removeVoucherBtn");
    if (voucherForm) {
      voucherForm.code.value = cart.voucherCode || "";
    }
    if (voucherInfo) {
      const hasVoucher = Boolean(cart.voucherCode);
      voucherInfo.classList.toggle("hidden", !hasVoucher && !cart.voucherMessage);
      voucherInfo.textContent = hasVoucher
        ? `${cart.voucherCode}${cart.voucherName ? " - " + cart.voucherName : ""}`
        : (cart.voucherMessage || "");
    }
    if (removeVoucherBtn) {
      removeVoucherBtn.classList.toggle("hidden", !cart.voucherCode);
    }
    renderApplyVoucherView();

    const items = cart.items || [];
    $("#cartItems").innerHTML = items.map((item) => `
      <article class="list-item">
        <a class="product-image" href="product.jsp?id=${item.productId}">
          <img src="${imageUrl(item.imageUrl)}" alt="${html(item.productName)}">
        </a>
        <div>
          <h3><a href="product.jsp?id=${item.productId}">${html(item.productName)}</a></h3>
          <p class="muted">${money(item.unitPrice)} - còn ${item.stock} sản phẩm</p>
          ${item.warning ? `<p class="notice error">${html(item.warning)}</p>` : ""}
        </div>
        <div class="item-actions">
          <div class="qty">
            <button class="button" data-cart-minus="${item.productId}">-</button>
            <input data-cart-qty="${item.productId}" type="number" min="1" value="${item.quantity}">
            <button class="button" data-cart-plus="${item.productId}">+</button>
          </div>
          <p class="price">${money(item.lineTotal)}</p>
          <button class="button danger" data-cart-remove="${item.productId}">Xóa</button>
        </div>
      </article>
    `).join("") || `<div class="panel">Giỏ hàng đang trống. <a href="products.jsp">Mua hàng ngay</a></div>`;

    bindCartDetailEvents();
  }

  function bindCartDetailEvents() {
    $$("[data-cart-minus]").forEach((button) => button.onclick = () => changeCartItemQuantity(button.dataset.cartMinus, -1));
    $$("[data-cart-plus]").forEach((button) => button.onclick = () => changeCartItemQuantity(button.dataset.cartPlus, 1));
    $$("[data-cart-remove]").forEach((button) => button.onclick = () => deleteCartItemFromDetailView(button.dataset.cartRemove));
    $$("[data-cart-qty]").forEach((input) => input.onchange = () => updateCartItemQuantity(input.dataset.cartQty, input.value));
  }

  // UC-2.3: CartDetailView gui productId va newQuantity de CartItem kiem tra ton kho roi tinh lai thanh tien.
  async function updateCartItemQuantity(productId, quantity) {
    try {
      const updated = await request(`/api/cart/items/${productId}`, { method: "PUT", body: { quantity: Number(quantity) } });
      renderCartDetailView(updated);
      await showApplyVoucherView();
      await refreshCartCount();
      if (updated.message) setNotice("#cartNotice", updated.message, "success");
    } catch (error) {
      setNotice("#cartNotice", error.message, "error");
      renderCartDetailView(await request("/api/cart"));
    }
  }

  async function changeCartItemQuantity(productId, change) {
    const input = $(`[data-cart-qty="${productId}"]`);
    await updateCartItemQuantity(productId, Math.max(1, Number(input.value || 1) + change));
  }

  // UC-2.2: CartDetailView gui productId, Cart tim CartItem va xoa khoi danh sach.
  async function deleteCartItemFromDetailView(productId) {
    const updated = await request(`/api/cart/items/${productId}`, { method: "DELETE" });
    renderCartDetailView(updated);
    await showApplyVoucherView();
    await refreshCartCount();
  }

  async function initCheckout() {
    if (!requireLogin()) return;
    const user = currentUser() || {};
    const cart = await request("/api/cart");
    const items = cart.items || [];
    $("#checkoutItems").innerHTML = items.map((item) => `
      <div class="mini-row"><span>${html(item.productName)} x ${item.quantity}</span><strong>${money(item.lineTotal)}</strong></div>
    `).join("") || `<p class="muted">Giỏ hàng trống.</p>`;
    $("#checkoutSubtotal").textContent = money(cart.subtotal);
    $("#checkoutDiscount").textContent = `-${money(cart.discountAmount || 0)}`;
    $("#checkoutTotal").textContent = money(cart.totalAmount);

    const checkoutForm = $("#checkoutForm");
    if (checkoutForm) {
      checkoutForm.fullName.value = user.fullName || "";
      checkoutForm.email.value = user.email || "";
      checkoutForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!items.length) {
          setNotice("#checkoutNotice", "Giỏ hàng đang trống.", "error");
          return;
        }
        const data = formData(checkoutForm);
        const orderDetails = items.map((item) => ({
          product: { id: item.productId },
          quantity: item.quantity,
          unitPrice: item.unitPrice
        }));
        try {
          await request("/api/orders", {
            method: "POST",
            body: {
              phoneNumber: data.phone,
              shippingAddress: data.address,
              totalAmount: cart.totalAmount,
              orderDetails
            }
          });
          await request("/api/cart/items", { method: "DELETE" });
          setNotice("#checkoutNotice", "Đặt hàng thành công. Bạn có thể xem lại trong trang đơn hàng.", "success");
          checkoutForm.reset();
          await refreshCartCount();
        } catch (error) {
          setNotice("#checkoutNotice", error.message, "error");
        }
      });
    }
  }

  function initLogin() {
    const verified = params.get("verified");
    if (verified === "1") setNotice("#loginNotice", "Xác thực email thành công. Bạn có thể đăng nhập.", "success");
    if (verified === "0") setNotice("#loginNotice", "Xác thực email thất bại hoặc token hết hạn.", "error");

    const loginForm = $("#loginForm");
    if (loginForm) {
      loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
          const data = await request("/api/auth/login", { method: "POST", body: formData(event.currentTarget) });
          localStorage.setItem("auth.token", data.token);
          localStorage.setItem("auth.user", JSON.stringify(data));
          location.href = data.role === "ADMIN" || data.role === "ROLE_ADMIN" ? "admin.jsp" : "index.jsp";
        } catch (error) {
          setNotice("#loginNotice", error.message, "error");
        }
      });
    }
  }

  function initRegister() {
    const registerForm = $("#registerForm");
    if (registerForm) {
      registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
          const data = await request("/api/auth/register", { method: "POST", body: formData(event.currentTarget) });
          if (data?.token) {
            localStorage.setItem("auth.token", data.token);
            localStorage.setItem("auth.user", JSON.stringify(data));
          }
          setNotice("#registerNotice", data?.message || "Đăng ký thành công. Hãy kiểm tra email nếu hệ thống yêu cầu xác thực.", "success");
        } catch (error) {
          setNotice("#registerNotice", error.message, "error");
        }
      });
    }
  }

  function initForgotPassword() {
    const forgotForm = $("#forgotForm");
    if (forgotForm) {
      forgotForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
          await request("/api/auth/forgot-password", { method: "POST", body: formData(event.currentTarget) });
          setNotice("#forgotNotice", "Đã gửi yêu cầu đặt lại mật khẩu.", "success");
        } catch (error) {
          setNotice("#forgotNotice", error.message, "error");
        }
      });
    }
    const resetForm = $("#resetForm");
    if (resetForm) {
      resetForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
          await request("/api/auth/reset-password", { method: "POST", body: formData(event.currentTarget) });
          setNotice("#forgotNotice", "Đặt lại mật khẩu thành công.", "success");
        } catch (error) {
          setNotice("#forgotNotice", error.message, "error");
        }
      });
    }
  }

  async function initProfile() {
    if (!requireLogin()) return;
    const profile = await request("/api/users/me");
    $("#profileInfo").innerHTML = `
      <h2>Thông tin cá nhân</h2>
      <p><strong>Họ tên:</strong> ${html(profile.fullName || "")}</p>
      <p><strong>Email:</strong> ${html(profile.email || "")}</p>
      <p><strong>Số điện thoại:</strong> ${html(profile.phone || "")}</p>
      <p><strong>Vai trò:</strong> ${html(profile.role || profile.roleName || "")}</p>
      <a class="button" href="orders.jsp">Xem đơn hàng</a>
    `;

    const changePasswordForm = $("#changePasswordForm");
    if (changePasswordForm) {
      changePasswordForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
          await request("/api/auth/change-password", { method: "POST", body: formData(event.currentTarget) });
          event.currentTarget.reset();
          setNotice("#profileNotice", "Đổi mật khẩu thành công.", "success");
        } catch (error) {
          setNotice("#profileNotice", error.message, "error");
        }
      });
    }
  }

  async function initOrders() {
    if (!requireLogin()) return;
    const result = await request("/api/orders?size=50&sortBy=id&sortDir=desc");
    $("#ordersList").innerHTML = pageContent(result).map(orderView).join("") || `<div class="panel">Chưa có đơn hàng.</div>`;
  }

  function orderView(order) {
    const details = order.orderDetails || [];
    return `
      <article class="panel">
        <div class="section-title">
          <h2>Đơn #${order.id}</h2>
          <span class="chip">${html(order.status || "PENDING")}</span>
        </div>
        <p><strong>Tổng tiền:</strong> ${money(order.totalAmount)}</p>
        <p><strong>Địa chỉ:</strong> ${html(order.shippingAddress || "")}</p>
        <p><strong>Điện thoại:</strong> ${html(order.phoneNumber || "")}</p>
        <div class="mini-list">
          ${details.map((item) => `<div class="mini-row"><span>${html(item.product?.name || "Sản phẩm")} x ${item.quantity}</span><strong>${money(item.unitPrice)}</strong></div>`).join("")}
        </div>
      </article>
    `;
  }

  async function initAdmin() {
    if (!requireLogin()) return;
    $$(".tab").forEach((tab) => {
      tab.onclick = () => {
        $$(".tab").forEach((x) => x.classList.remove("active"));
        $$(".admin-section").forEach((x) => x.classList.add("hidden"));
        tab.classList.add("active");
        $(`[data-panel="${tab.dataset.tab}"]`).classList.remove("hidden");
      };
    });

    bindAdminForms();
    await Promise.all([loadAdminProducts(), loadAdminBrands(), loadAdminCategories(), loadAdminVouchers(), loadAdminOrders(), loadAdminUsers()]);
  }

  function bindAdminForms() {
    const brandForm = $("#adminBrandForm");
    if (brandForm) {
      brandForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = formData(event.currentTarget);
        await saveAdmin("/api/brands", data.id, { name: data.name, logoUrl: data.logoUrl }, loadAdminBrands);
        event.currentTarget.reset();
      });
    }

    const categoryForm = $("#adminCategoryForm");
    if (categoryForm) {
      categoryForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = formData(event.currentTarget);
        await saveAdmin("/api/categories", data.id, { name: data.name, description: data.description }, loadAdminCategories);
        event.currentTarget.reset();
      });
    }

    const voucherForm = $("#adminVoucherForm");
    if (voucherForm) {
      voucherForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = formData(event.currentTarget);
        const payload = {
          code: data.code,
          name: data.name,
          description: data.description,
          discountType: data.discountType,
          discountValue: Number(data.discountValue || 0),
          minOrderAmount: data.minOrderAmount ? Number(data.minOrderAmount) : null,
          maxDiscountAmount: data.maxDiscountAmount ? Number(data.maxDiscountAmount) : null,
          usageLimit: data.usageLimit ? Number(data.usageLimit) : null,
          usedCount: Number(data.usedCount || 0),
          startDate: data.startDate || null,
          endDate: data.endDate || null,
          active: data.active === "true"
        };
        await saveAdmin("/api/vouchers", data.id, payload, loadAdminVouchers);
        event.currentTarget.reset();
      });
    }
  }

  async function initAdminProduct() {
    if (!requireLogin()) return;
    const id = params.get("id");
    if (id) {
      setNotice("#adminNotice", "Đang tải sản phẩm...");
      try {
        const product = await request(`/api/products/${id}`);
        fillProductForm(product);
        setNotice("#adminNotice", "");
      } catch (error) {
        setNotice("#adminNotice", error.message, "error");
      }
    }

    const form = $("#adminProductForm");
    if (form) {
      bindAdminProductImageUpload(form);
      form.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
          const data = formData(event.currentTarget);
          const uploadedImageUrl = await uploadAdminProductImage(event.currentTarget);
          const selectedImageUrl = uploadedImageUrl || String(data.imageUrl || "").trim();
          const payload = {
            name: data.name,
            price: Number(data.price || 0),
            importPrice: Number(data.importPrice || 0),
            stock: Number(data.stock || 0),
            description: data.description,
            brand: data.brandId ? { id: Number(data.brandId) } : null,
            category: data.categoryId ? { id: Number(data.categoryId) } : null,
            images: selectedImageUrl ? [{ imageUrl: selectedImageUrl, isPrimary: true }] : [],
            specification: {
              cpu: data.cpu,
              ram: data.ram,
              storage: data.storage,
              screen: data.screen
            }
          };
          await request(data.id ? `/api/products/${data.id}` : "/api/products", {
            method: data.id ? "PUT" : "POST",
            body: payload
          });
          alert("Đã lưu sản phẩm thành công.");
          location.href = "admin.jsp";
        } catch (error) {
          setNotice("#adminNotice", error.message, "error");
        }
      });
    }
  }

  function bindAdminProductImageUpload(form) {
    const imageFile = form.imageFile;
    const imageUrlInput = form.imageUrl;
    const preview = $("#adminProductImagePreview");
    if (!imageFile || !preview) return;

    imageFile.addEventListener("change", () => {
      const file = imageFile.files?.[0];
      if (!file) {
        renderAdminProductImagePreview(imageUrlInput?.value || "");
        return;
      }
      const localUrl = URL.createObjectURL(file);
      renderAdminProductImagePreview(localUrl);
    });

    if (imageUrlInput) {
      imageUrlInput.addEventListener("input", () => {
        if (!imageFile.files?.length) renderAdminProductImagePreview(imageUrlInput.value);
      });
    }

    form.addEventListener("reset", () => {
      setTimeout(() => renderAdminProductImagePreview(""), 0);
    });
  }

  async function uploadAdminProductImage(form) {
    const file = form.imageFile?.files?.[0];
    if (!file) return "";

    if (!file.type.startsWith("image/")) {
      throw new Error("File được chọn không phải ảnh.");
    }

    setNotice("#adminNotice", "Đang tải ảnh lên...");
    const uploadData = new FormData();
    uploadData.append("file", file);
    const result = await request("/api/files", { method: "POST", body: uploadData });
    form.imageUrl.value = result.imageUrl || result.url || "";
    renderAdminProductImagePreview(form.imageUrl.value);
    return form.imageUrl.value;
  }

  function renderAdminProductImagePreview(url) {
    const preview = $("#adminProductImagePreview");
    if (!preview) return;
    preview.classList.toggle("hidden", !url);
    preview.innerHTML = url ? `<img src="${imageUrl(url)}" alt="Ảnh sản phẩm">` : "";
  }

  async function saveAdmin(base, id, payload, reload) {
    try {
      await request(id ? `${base}/${id}` : base, { method: id ? "PUT" : "POST", body: payload });
      setNotice("#adminNotice", "Đã lưu dữ liệu.", "success");
      await reload();
    } catch (error) {
      setNotice("#adminNotice", error.message, "error");
    }
  }

  async function removeAdmin(path, reload) {
    if (!confirm("Bạn chắc chắn muốn xóa?")) return;
    try {
      await request(path, { method: "DELETE" });
      setNotice("#adminNotice", "Đã xóa dữ liệu.", "success");
      await reload();
    } catch (error) {
      setNotice("#adminNotice", error.message, "error");
    }
  }

  async function loadAdminProducts() {
    const result = await request("/api/products?size=100");
    $("#adminProducts").innerHTML = table(["ID", "Tên", "Giá", "Kho", "Thương hiệu", "Danh mục", ""], pageContent(result).map((p) => [
      p.id,
      html(p.name),
      money(p.price),
      p.stock ?? 0,
      html(p.brand?.name || ""),
      html(p.category?.name || ""),
      `<div style="display: flex; gap: 8px; justify-content: flex-end;">
        <a class="button" href="admin-product.jsp?id=${p.id}">Sửa</a>
        <button class="button danger" data-delete-product="${p.id}">Xóa</button>
      </div>`
    ]));
    $$("[data-delete-product]").forEach((btn) => btn.onclick = () => removeAdmin(`/api/products/${btn.dataset.deleteProduct}`, loadAdminProducts));
  }

  function fillProductForm(product) {
    const form = $("#adminProductForm");
    if (!form) return;
    form.id.value = product.id || "";
    form.name.value = product.name || "";
    form.price.value = product.price || "";
    form.importPrice.value = product.importPrice || "";
    form.stock.value = product.stock || "";
    form.brandId.value = product.brand?.id || "";
    form.categoryId.value = product.category?.id || "";
    form.imageUrl.value = firstImage(product) || "";
    renderAdminProductImagePreview(form.imageUrl.value);
    form.cpu.value = product.specification?.cpu || "";
    form.ram.value = product.specification?.ram || "";
    form.storage.value = product.specification?.storage || "";
    form.screen.value = product.specification?.screen || "";
    form.description.value = product.description || "";
    form.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  async function loadAdminBrands() {
    const result = await request("/api/brands?size=100");
    $("#adminBrands").innerHTML = table(["ID", "Tên", "Logo", ""], pageContent(result).map((b) => [
      b.id,
      html(b.name),
      html(b.logoUrl || ""),
      `<button class="button" data-edit-brand="${b.id}">Sửa</button> <button class="button danger" data-delete-brand="${b.id}">Xóa</button>`
    ]));
    $$("[data-delete-brand]").forEach((btn) => btn.onclick = () => removeAdmin(`/api/brands/${btn.dataset.deleteBrand}`, loadAdminBrands));
    $$("[data-edit-brand]").forEach((btn) => btn.onclick = async () => {
      const brand = await request(`/api/brands/${btn.dataset.editBrand}`);
      const form = $("#adminBrandForm");
      form.id.value = brand.id || "";
      form.name.value = brand.name || "";
      form.logoUrl.value = brand.logoUrl || "";
    });
  }

  async function loadAdminCategories() {
    const result = await request("/api/categories?size=100");
    $("#adminCategories").innerHTML = table(["ID", "Tên", "Mô tả", ""], pageContent(result).map((c) => [
      c.id,
      html(c.name),
      html(c.description || ""),
      `<button class="button" data-edit-category="${c.id}">Sửa</button> <button class="button danger" data-delete-category="${c.id}">Xóa</button>`
    ]));
    $$("[data-delete-category]").forEach((btn) => btn.onclick = () => removeAdmin(`/api/categories/${btn.dataset.deleteCategory}`, loadAdminCategories));
    $$("[data-edit-category]").forEach((btn) => btn.onclick = async () => {
      const category = await request(`/api/categories/${btn.dataset.editCategory}`);
      const form = $("#adminCategoryForm");
      form.id.value = category.id || "";
      form.name.value = category.name || "";
      form.description.value = category.description || "";
    });
  }

  async function loadAdminVouchers() {
    const result = await request("/api/vouchers?size=100&sortBy=id&sortDir=desc");
    $("#adminVouchers").innerHTML = table(["ID", "Mã", "Tên", "Giảm", "Đơn tối thiểu", "Hạn dùng", "Lượt dùng", "Trạng thái", ""], pageContent(result).map((v) => [
      v.id,
      html(v.code),
      html(v.name),
      voucherDiscountText(v),
      money(v.minOrderAmount || 0),
      `${formatDateTime(v.startDate)} - ${formatDateTime(v.endDate)}`,
      `${v.usedCount || 0}/${v.usageLimit ?? "∞"}`,
      v.active ? "Bật" : "Tắt",
      `<button class="button" data-edit-voucher="${v.id}">Sửa</button> <button class="button danger" data-delete-voucher="${v.id}">Xóa</button>`
    ]));
    $$("[data-delete-voucher]").forEach((btn) => btn.onclick = () => removeAdmin(`/api/vouchers/${btn.dataset.deleteVoucher}`, loadAdminVouchers));
    $$("[data-edit-voucher]").forEach((btn) => btn.onclick = async () => fillVoucherForm(await request(`/api/vouchers/${btn.dataset.editVoucher}`)));
  }

  function voucherDiscountText(voucher) {
    if (voucher.discountType === "PERCENTAGE") {
      const cap = voucher.maxDiscountAmount ? ` (tối đa ${money(voucher.maxDiscountAmount)})` : "";
      return `${Number(voucher.discountValue || 0)}%${cap}`;
    }
    return money(voucher.discountValue);
  }

  function formatDateTime(value) {
    if (!value) return "";
    return String(value).replace("T", " ").slice(0, 16);
  }

  function toDateTimeLocal(value) {
    if (!value) return "";
    return String(value).slice(0, 16);
  }

  function fillVoucherForm(voucher) {
    const form = $("#adminVoucherForm");
    form.id.value = voucher.id || "";
    form.code.value = voucher.code || "";
    form.name.value = voucher.name || "";
    form.discountType.value = voucher.discountType || "PERCENTAGE";
    form.discountValue.value = voucher.discountValue || "";
    form.minOrderAmount.value = voucher.minOrderAmount || "";
    form.maxDiscountAmount.value = voucher.maxDiscountAmount || "";
    form.usageLimit.value = voucher.usageLimit || "";
    form.usedCount.value = voucher.usedCount || 0;
    form.startDate.value = toDateTimeLocal(voucher.startDate);
    form.endDate.value = toDateTimeLocal(voucher.endDate);
    form.active.value = String(voucher.active !== false);
    form.description.value = voucher.description || "";
    form.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  async function loadAdminOrders() {
    const result = await request("/api/orders?size=100&sortBy=id&sortDir=desc");
    $("#adminOrders").innerHTML = table(["ID", "Khách", "Tổng", "Trạng thái", "Đổi trạng thái"], pageContent(result).map((o) => [
      o.id,
      html(o.user?.email || ""),
      money(o.totalAmount),
      html(o.status),
      `<select data-order-status="${o.id}">
        ${["PENDING", "APPROVED", "CANCELLED", "DELIVERED"].map((s) => `<option value="${s}" ${s === o.status ? "selected" : ""}>${s}</option>`).join("")}
      </select>`
    ]));
    $$("[data-order-status]").forEach((select) => {
      select.onchange = async () => {
        await request(`/api/orders/${select.dataset.orderStatus}/status?status=${select.value}`, { method: "PUT" });
        setNotice("#adminNotice", "Đã cập nhật trạng thái.", "success");
      };
    });
  }

  async function loadAdminUsers() {
    const result = await request("/api/users?size=100");
    $("#adminUsers").innerHTML = table(["ID", "Họ tên", "Email", "SĐT", "Vai trò"], pageContent(result).map((u) => [
      u.id,
      html(u.fullName || ""),
      html(u.email || ""),
      html(u.phone || ""),
      html(u.role?.name || "")
    ]));
  }

  function table(headers, rows) {
    return `
      <table>
        <thead><tr>${headers.map((h) => `<th>${h}</th>`).join("")}</tr></thead>
        <tbody>${rows.map((row) => `<tr>${row.map((cell) => `<td>${cell}</td>`).join("")}</tr>`).join("")}</tbody>
      </table>
    `;
  }

  async function boot() {
    updateHeader();
    await refreshCartCount();
    const page = document.body.dataset.page;
    try {
      if (page === "home") await initHome();
      if (page === "products") await initProducts();
      if (page === "compare") await initCompare();
      if (page === "product-detail") await initProductDetail();
      if (page === "cart") await initCartDetailView();
      if (page === "checkout") await initCheckout();
      if (page === "login") initLogin();
      if (page === "register") initRegister();
      if (page === "forgot-password") initForgotPassword();
      if (page === "profile") await initProfile();
      if (page === "orders") await initOrders();
      if (page === "admin") await initAdmin();
      if (page === "admin-product") await initAdminProduct();
    } catch (error) {
      console.error("App Boot Error:", error);
      const notice = $(".notice");
      if (notice) setNotice(notice, error.message, "error");
    }
  }

  return { boot };
})();

document.addEventListener("DOMContentLoaded", App.boot);
