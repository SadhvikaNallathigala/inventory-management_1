let currentCustomer = '';
let currentPremiumFilter = '';

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('searchInput').addEventListener('input', debounce(onFilterChange, 250));
  document.getElementById('premiumFilter').addEventListener('change', onFilterChange);

  document.getElementById('placeBtn').addEventListener('click', openPlaceModal);
  document.getElementById('placeForm').addEventListener('submit', onPlaceSubmit);
  document.getElementById('premiumToggle').addEventListener('change', onPremiumToggle);

  document.querySelectorAll('[data-close]').forEach(btn =>
    btn.addEventListener('click', () => closeModal(btn.getAttribute('data-close'))));

  loadProductOptions();
  loadOrders();
});

function debounce(fn, ms) {
  let t;
  return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
}

function onFilterChange() {
  currentCustomer = document.getElementById('searchInput').value.trim();
  currentPremiumFilter = document.getElementById('premiumFilter').value;
  loadOrders();
}

async function loadOrders() {
  const params = new URLSearchParams();
  if (currentCustomer) params.set('customer', currentCustomer);
  if (currentPremiumFilter) params.set('premium', currentPremiumFilter);

  try {
    const res = await Api.get('/api/orders?' + params.toString());
    renderStats(res.meta);
    renderTable(res.data);
  } catch (e) {
    showToast(e.message, 'error');
  }
}

function renderStats(meta) {
  document.getElementById('statTotal').textContent = meta.totalOrders ?? 0;
  document.getElementById('statPremium').textContent = meta.premiumOrders ?? 0;
  document.getElementById('statRegular').textContent = meta.regularOrders ?? 0;
  document.getElementById('statCustomers').textContent = meta.uniqueCustomers ?? 0;
}

function renderTable(orders) {
  const tbody = document.getElementById('ordersBody');
  const emptyState = document.getElementById('emptyState');
  const table = document.getElementById('ordersTable');

  if (!orders.length) {
    table.style.display = 'none';
    emptyState.style.display = 'block';
    return;
  }
  table.style.display = 'table';
  emptyState.style.display = 'none';

  tbody.innerHTML = orders.map(o => `
    <tr>
      <td><span class="code-chip">${escapeHtml(o.orderCode)}</span></td>
      <td>
        <div class="cell-name">${escapeHtml(o.customerName)}</div>
      </td>
      <td>
        <div class="cell-name">${escapeHtml(o.productName)}</div>
        <div class="cell-sub">${escapeHtml(o.productCode)}</div>
      </td>
      <td>${o.quantity}</td>
      <td>${o.premium
          ? '<span class="badge badge-premium">Premium</span>'
          : '<span class="badge badge-regular">Regular</span>'}</td>
      <td>#${o.queuePosition}</td>
      <td>${formatDateTime(o.placedAt)}</td>
    </tr>
  `).join('');
}

/* ---------------- Place order ---------------- */
async function loadProductOptions() {
  try {
    const res = await Api.get('/api/products');
    const select = document.getElementById('placeProduct');
    select.innerHTML = '<option value="">Select a product…</option>' +
      res.data.map(p => `<option value="${escapeHtml(p.code)}" data-stock="${p.quantity}">
        ${escapeHtml(p.code)} · ${escapeHtml(p.name)} (${p.quantity} in stock)
      </option>`).join('');
  } catch (e) {
    showToast(e.message, 'error');
  }
}

function openPlaceModal() {
  document.getElementById('placeForm').reset();
  hideFormError('placeFormError');
  document.getElementById('priorityField').style.display = 'none';
  loadProductOptions();
  document.getElementById('placeOverlay').classList.add('open');
}

function onPremiumToggle(e) {
  document.getElementById('priorityField').style.display = e.target.checked ? 'block' : 'none';
}

async function onPlaceSubmit(e) {
  e.preventDefault();
  hideFormError('placeFormError');

  const premium = document.getElementById('premiumToggle').checked;
  const priority = document.getElementById('placePriority').value;

  const payload = {
    customerName: document.getElementById('placeCustomer').value.trim(),
    productCode: document.getElementById('placeProduct').value,
    quantity: parseInt(document.getElementById('placeQuantity').value, 10),
    premium: premium,
  };
  if (premium && priority !== '') payload.priorityLevel = parseInt(priority, 10);

  try {
    await Api.post('/api/orders', payload);
    closeModal('placeOverlay');
    showToast('Order placed', 'success');
    loadOrders();
    loadProductOptions();
  } catch (err) {
    showFormError('placeFormError', err.message);
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
