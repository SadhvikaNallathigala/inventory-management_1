let currentCategory = '';
let currentKeyword = '';
let editingCode = null;
let historyCode = null;
let deletingCode = null;

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('searchInput').addEventListener('input', debounce(onFilterChange, 250));
  document.getElementById('categoryFilter').addEventListener('change', onFilterChange);

  document.getElementById('addBtn').addEventListener('click', () => openAddModal());
  document.getElementById('addForm').addEventListener('submit', onAddSubmit);
  document.getElementById('editForm').addEventListener('submit', onEditSubmit);

  document.getElementById('confirmDeleteBtn').addEventListener('click', onDeleteConfirmed);

  document.querySelectorAll('[data-close]').forEach(btn =>
    btn.addEventListener('click', () => closeModal(btn.getAttribute('data-close'))));

  loadProducts();
});

function debounce(fn, ms) {
  let t;
  return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
}

function onFilterChange() {
  currentKeyword = document.getElementById('searchInput').value.trim();
  currentCategory = document.getElementById('categoryFilter').value;
  loadProducts();
}

async function loadProducts() {
  const params = new URLSearchParams();
  if (currentKeyword) params.set('keyword', currentKeyword);
  if (currentCategory) params.set('category', currentCategory);

  try {
    const res = await Api.get('/api/products?' + params.toString());
    renderStats(res.meta);
    renderCategoryFilter(res.meta.categories || []);
    renderTable(res.data);
  } catch (e) {
    showToast(e.message, 'error');
  }
}

function renderStats(meta) {
  document.getElementById('statTotal').textContent = meta.totalProducts ?? 0;
  document.getElementById('statLow').textContent = meta.lowStockCount ?? 0;
  document.getElementById('statCategories').textContent = (meta.categories || []).length;
  const totalUnits = Object.values(meta.stockByCategory || {}).reduce((a, b) => a + b, 0);
  document.getElementById('statUnits').textContent = totalUnits;
}

function renderCategoryFilter(categories) {
  const select = document.getElementById('categoryFilter');
  const prev = select.value;
  select.innerHTML = '<option value="">All categories</option>' +
    categories.map(c => `<option value="${escapeHtml(c)}">${escapeHtml(c)}</option>`).join('');
  select.value = categories.includes(prev) ? prev : '';

  const addCategoryList = document.getElementById('categoryDatalist');
  addCategoryList.innerHTML = categories.map(c => `<option value="${escapeHtml(c)}">`).join('');
}

function renderTable(products) {
  const tbody = document.getElementById('productsBody');
  const emptyState = document.getElementById('emptyState');
  const table = document.getElementById('productsTable');

  if (!products.length) {
    table.style.display = 'none';
    emptyState.style.display = 'block';
    return;
  }
  table.style.display = 'table';
  emptyState.style.display = 'none';

  tbody.innerHTML = products.map(p => `
    <tr>
      <td><span class="code-chip">${escapeHtml(p.code)}</span></td>
      <td>
        <div class="cell-name">${escapeHtml(p.name)}</div>
        <div class="cell-sub">${escapeHtml(p.category)}</div>
      </td>
      <td>$${Number(p.price).toFixed(2)}</td>
      <td>${p.quantity}</td>
      <td>${p.lowStock
          ? '<span class="badge badge-low">Low stock</span>'
          : '<span class="badge badge-ok">In stock</span>'}</td>
      <td>
        <div class="actions-cell">
          <button class="icon-btn edit" title="Edit / update stock" onclick="openEditModal('${escapeHtml(p.code)}')">${Icons.edit}</button>
          <button class="icon-btn view" title="Stock history" onclick="openHistoryModal('${escapeHtml(p.code)}')">${Icons.eye}</button>
          <button class="icon-btn delete" title="Delete product" onclick="openDeleteModal('${escapeHtml(p.code)}')">${Icons.trash}</button>
        </div>
      </td>
    </tr>
  `).join('');
}

/* ---------------- Add Product ---------------- */
function openAddModal() {
  document.getElementById('addForm').reset();
  hideFormError('addFormError');
  document.getElementById('addOverlay').classList.add('open');
}

async function onAddSubmit(e) {
  e.preventDefault();
  hideFormError('addFormError');
  const payload = {
    code: document.getElementById('addCode').value.trim().toUpperCase(),
    name: document.getElementById('addName').value.trim(),
    category: document.getElementById('addCategory').value.trim(),
    price: parseFloat(document.getElementById('addPrice').value),
    quantity: parseInt(document.getElementById('addQuantity').value, 10),
  };
  try {
    await Api.post('/api/products', payload);
    closeModal('addOverlay');
    showToast('Product added', 'success');
    loadProducts();
  } catch (err) {
    showFormError('addFormError', err.message);
  }
}

/* ---------------- Edit Product / Update Stock ---------------- */
async function openEditModal(code) {
  editingCode = code;
  hideFormError('editFormError');
  try {
    const res = await Api.get('/api/products?keyword=' + encodeURIComponent(code));
    const product = res.data.find(p => p.code === code) || res.data[0];
    if (!product) throw new Error('Product not found');
    document.getElementById('editModalTitle').textContent = 'Edit ' + product.code;
    document.getElementById('editName').value = product.name;
    document.getElementById('editCategory').value = product.category;
    document.getElementById('editPrice').value = product.price;
    document.getElementById('editCurrentQty').textContent = product.quantity;
    document.getElementById('editQuantityChange').value = '';
    document.getElementById('editReason').value = '';
    document.getElementById('editOverlay').classList.add('open');
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function onEditSubmit(e) {
  e.preventDefault();
  hideFormError('editFormError');

  const payload = {};
  const name = document.getElementById('editName').value.trim();
  const category = document.getElementById('editCategory').value.trim();
  const price = document.getElementById('editPrice').value;
  const qtyChange = document.getElementById('editQuantityChange').value;
  const reason = document.getElementById('editReason').value.trim();

  if (name) payload.name = name;
  if (category) payload.category = category;
  if (price !== '') payload.price = parseFloat(price);
  if (qtyChange !== '') payload.quantityChange = parseInt(qtyChange, 10);
  if (reason) payload.reason = reason;

  try {
    await Api.put('/api/products/' + encodeURIComponent(editingCode), payload);
    closeModal('editOverlay');
    showToast('Product updated', 'success');
    loadProducts();
  } catch (err) {
    showFormError('editFormError', err.message);
  }
}

/* ---------------- Stock History ---------------- */
async function openHistoryModal(code) {
  historyCode = code;
  document.getElementById('historyModalTitle').textContent = 'Stock history · ' + code;
  const body = document.getElementById('historyBody');
  body.innerHTML = '<div class="empty-state"><p>Loading…</p></div>';
  document.getElementById('historyOverlay').classList.add('open');

  try {
    const res = await Api.get('/api/products/' + encodeURIComponent(code) + '/history');
    renderHistory(res.data);
  } catch (e) {
    body.innerHTML = `<div class="empty-state"><p>${escapeHtml(e.message)}</p></div>`;
  }
}

function renderHistory(entries) {
  const body = document.getElementById('historyBody');
  if (!entries.length) {
    body.innerHTML = '<div class="empty-state"><p>No history yet for this product.</p></div>';
    return;
  }
  body.innerHTML = entries.map(h => {
    const delta = h.changeAmount;
    const deltaClass = delta > 0 ? 'up' : (delta < 0 ? 'down' : 'flat');
    const deltaText = delta > 0 ? '+' + delta : String(delta);
    return `
      <div class="history-row">
        <div class="history-dot"></div>
        <div class="history-main">
          <div class="history-note">${escapeHtml(h.note)}</div>
          <div class="history-meta">${formatDateTime(h.when)} · ${h.previousQuantity} → ${h.newQuantity} units</div>
        </div>
        <div class="history-delta ${deltaClass}">${deltaText}</div>
      </div>`;
  }).join('');
}

/* ---------------- Delete Product ---------------- */
function openDeleteModal(code) {
  deletingCode = code;
  document.getElementById('deleteCodeLabel').textContent = code;
  document.getElementById('deleteOverlay').classList.add('open');
}

async function onDeleteConfirmed() {
  try {
    await Api.del('/api/products/' + encodeURIComponent(deletingCode));
    closeModal('deleteOverlay');
    showToast('Product deleted', 'success');
    loadProducts();
  } catch (e) {
    closeModal('deleteOverlay');
    showToast(e.message, 'error');
  }
}

/* ---------------- Modal helpers ---------------- */
function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}
function showFormError(id, message) {
  const el = document.getElementById(id);
  el.textContent = message;
  el.classList.add('show');
}
function hideFormError(id) {
  document.getElementById(id).classList.remove('show');
}
