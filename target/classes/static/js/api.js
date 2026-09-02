// Thin wrapper around fetch() that always expects the standard
// { success, data, error, meta } envelope from the backend.
const Api = {
  async call(method, url, body) {
    const options = { method, headers: {} };
    if (body !== undefined) {
      options.headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(body);
    }
    let res, json;
    try {
      res = await fetch(url, options);
      json = await res.json();
    } catch (e) {
      throw new Error('Could not reach the server. Is it running on localhost:8080?');
    }
    if (!json.success) {
      throw new Error((json.error && json.error.message) || 'Request failed');
    }
    return json; // { success, data, error, meta }
  },
  get(url) { return this.call('GET', url); },
  post(url, body) { return this.call('POST', url, body); },
  put(url, body) { return this.call('PUT', url, body); },
  del(url) { return this.call('DELETE', url); },
};

function showToast(message, type) {
  const el = document.getElementById('toast');
  el.textContent = message;
  el.className = 'show' + (type ? ' ' + type : '');
  clearTimeout(showToast._t);
  showToast._t = setTimeout(() => { el.className = ''; }, 3200);
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function formatDateTime(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' }) + ' · ' +
         d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
}

const Icons = {
  edit: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M13.5 3.5l3 3-8.6 8.6-3.7.7.7-3.7 8.6-8.6z" stroke-linejoin="round"/></svg>',
  eye: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M2 10s2.8-5 8-5 8 5 8 5-2.8 5-8 5-8-5-8-5z"/><circle cx="10" cy="10" r="2.2"/></svg>',
  trash: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"><path d="M4 6h12M8 6V4.5h4V6M5.5 6l.6 9a1 1 0 001 .9h5.8a1 1 0 001-.9l.6-9"/></svg>',
  search: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.6"><circle cx="9" cy="9" r="5.5"/><path d="M17 17l-3.5-3.5" stroke-linecap="round"/></svg>',
  plus: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M10 4v12M4 10h12"/></svg>',
  close: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M5 5l10 10M15 5L5 15"/></svg>',
  box: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 6l7-3 7 3-7 3-7-3z"/><path d="M3 6v8l7 3 7-3V6M10 9v8"/></svg>',
  cart: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 4h2l1.4 9.2a1.5 1.5 0 001.5 1.3h6.4a1.5 1.5 0 001.5-1.2L17 7H5.4" stroke-linecap="round" stroke-linejoin="round"/><circle cx="8" cy="17" r="1"/><circle cx="14.5" cy="17" r="1"/></svg>',
  empty: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4"><path d="M4 8l8-4 8 4-8 4-8-4z"/><path d="M4 8v8l8 4 8-4V8M12 12v8"/></svg>',
};
