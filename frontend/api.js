/**
 * SkillSwap API Client — FIXED VERSION
 * Changes vs original:
 *   - Auth.updateUser() added (dashboard called this; it didn't exist)
 *   - Auth.clear() now also clears ss_user (was only clearing token)
 *   - AdminAPI endpoints aligned to actual backend routes
 */

// ── CONFIG ────────────────────────────────────────────────────────────
const API_BASE = 'http://localhost:8080/api';

// ── TOKEN HELPERS ─────────────────────────────────────────────────────
const Auth = {
  getToken()  { return localStorage.getItem('ss_token'); },
  getUser()   { const u = localStorage.getItem('ss_user'); return u ? JSON.parse(u) : null; },

  setSession(data) {
    localStorage.setItem('ss_token', data.token);
    localStorage.setItem('ss_user', JSON.stringify({
      id:    data.userId,
      email: data.email,
      name:  data.fullName,
      role:  data.role,
    }));
  },

  // ── NEW: update stored user fields without touching the token ──────
  updateUser(profileData) {
    const stored = this.getUser();
    if (!stored) return;
    const merged = {
      ...stored,
      name:  profileData.fullName  || stored.name,
      email: profileData.email     || stored.email,
      role:  profileData.role      || stored.role,
    };
    localStorage.setItem('ss_user', JSON.stringify(merged));
  },

  // ── FIX: clear both keys ──────────────────────────────────────────
  clear() {
    localStorage.removeItem('ss_token');
    localStorage.removeItem('ss_user');
  },

  isLoggedIn()  { return !!this.getToken(); },
  isAdmin()     { const u = this.getUser(); return u && u.role === 'ADMIN'; },
  requireAuth() { if (!this.isLoggedIn()) { window.location.href = 'login.html'; } },
  requireAdmin(){ if (!this.isAdmin())    { window.location.href = 'admin-login.html'; } },
};

// ── BASE FETCH WRAPPER ────────────────────────────────────────────────
async function apiFetch(path, options = {}) {
  const token   = Auth.getToken();
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res  = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const json = await res.json().catch(() => null);

  if (!res.ok) {
    if (res.status === 401) { Auth.clear(); window.location.href = 'login.html'; return; }
    const msg = json?.message || `Request failed (${res.status})`;
    throw new Error(msg);
  }
  return json?.data ?? json;
}

const GET    = (path)       => apiFetch(path, { method: 'GET' });
const POST   = (path, body) => apiFetch(path, { method: 'POST',  body: JSON.stringify(body) });
const PUT    = (path, body) => apiFetch(path, { method: 'PUT',   body: JSON.stringify(body) });
const PATCH  = (path, body) => apiFetch(path, { method: 'PATCH', body: JSON.stringify(body) });
const DELETE = (path)       => apiFetch(path, { method: 'DELETE' });

// ── AUTH ENDPOINTS ────────────────────────────────────────────────────
const AuthAPI = {
  register: (data) => POST('/auth/register', data),
  login:    (data) => POST('/auth/login',    data),
};

// ── USER ENDPOINTS ────────────────────────────────────────────────────
const UserAPI = {
  me:      ()     => GET('/users/me'),
  update:  (data) => PUT('/users/me', data),
  getById: (id)   => GET(`/users/${id}`),
};

// ── SKILL LISTING ENDPOINTS ───────────────────────────────────────────
const SkillAPI = {
  browse: (params = {}) => {
    const q = new URLSearchParams(
      Object.fromEntries(Object.entries(params).filter(([, v]) => v))
    ).toString();
    return GET(`/skills/listings${q ? '?' + q : ''}`);
  },
  getById:    (id)        => GET(`/skills/listings/${id}`),
  myListings: ()          => GET('/skills/my'),
  create:     (data)      => POST('/skills', data),
  update:     (id, data)  => PUT(`/skills/${id}`, data),
  delete:     (id)        => DELETE(`/skills/${id}`),
  toggle:     (id)        => PATCH(`/skills/${id}/toggle`),
  flag:       (id, reason)=> POST(`/skills/${id}/flag?reason=${encodeURIComponent(reason)}`),
};

// ── SESSION REQUEST ENDPOINTS ─────────────────────────────────────────
const RequestAPI = {
  send:     (data)       => POST('/requests', data),
  incoming: ()           => GET('/requests/incoming'),
  outgoing: ()           => GET('/requests/outgoing'),
  respond:  (id, data)   => PATCH(`/requests/${id}/respond`, data),
  withdraw: (id)         => PATCH(`/requests/${id}/withdraw`),
};

// ── SESSION ENDPOINTS ─────────────────────────────────────────────────
const SessionAPI = {
  all:      () => GET('/sessions'),
  upcoming: () => GET('/sessions/upcoming'),
  history:  () => GET('/sessions/history'),
  getById:  (id) => GET(`/sessions/${id}`),
  complete: (id) => PATCH(`/sessions/${id}/complete`),
  cancel:   (id) => PATCH(`/sessions/${id}/cancel`),
};

// ── FEEDBACK ENDPOINTS ────────────────────────────────────────────────
const FeedbackAPI = {
  leave:    (data)       => POST('/feedback', data),
  received: ()           => GET('/feedback/received'),
  given:    ()           => GET('/feedback/given'),
  forUser:  (id)         => GET(`/feedback/user/${id}`),
  reply:    (id, text)   => PATCH(`/feedback/${id}/reply?text=${encodeURIComponent(text)}`),
  report:   (id)         => PATCH(`/feedback/${id}/report`),
};

// ── ADMIN ENDPOINTS ───────────────────────────────────────────────────
const AdminAPI = {
  stats:            ()       => GET('/admin/stats'),
  users:            ()       => GET('/admin/users'),
  searchUsers:      (q)      => GET(`/admin/users/search?query=${encodeURIComponent(q)}`),
  getUser:          (id)     => GET(`/admin/users/${id}`),
  suspendUser:      (id)     => PATCH(`/admin/users/${id}/suspend`),
  activateUser:     (id)     => PATCH(`/admin/users/${id}/activate`),
  deleteUser:       (id)     => DELETE(`/admin/users/${id}`),
  resetPassword:    (id, pw) => PATCH(`/admin/users/${id}/reset-password`, { newPassword: pw }),
  listings:         ()       => GET('/admin/listings'),
  flaggedListings:  ()       => GET('/admin/listings/flagged'),
  clearFlag:        (id)     => PATCH(`/admin/listings/${id}/clear-flag`),
  removeListing:    (id)     => DELETE(`/admin/listings/${id}`),
  sessions:         ()       => GET('/admin/sessions'),
  reportedFeedback: ()       => GET('/admin/feedback/reported'),
};

// ── UI HELPERS ────────────────────────────────────────────────────────

function showToast(msg, type = 'success') {
  const toast = document.getElementById('toast');
  if (!toast) return;
  const msgEl = document.getElementById('toastMsg');
  if (msgEl) msgEl.textContent = msg;
  toast.classList.remove('show');
  toast.style.background = type === 'error' ? 'var(--coral)' : 'var(--primary)';
  void toast.offsetWidth;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 3500);
}

function populateSidebarUser() {
  const user = Auth.getUser();
  if (!user) return;
  const nameEl   = document.querySelector('.sidebar-user-name');
  const avatarEl = document.querySelector('.sidebar-avatar');
  const deptEl   = document.querySelector('.sidebar-user-dept');
  if (nameEl)   nameEl.textContent   = user.name || '';
  if (deptEl)   deptEl.textContent   = '';   // filled by page after profile load
  if (avatarEl) avatarEl.textContent = initials(user.name);
}

function populateTopbarAvatar() {
  const user = Auth.getUser();
  if (!user) return;
  const av = document.querySelector('.topbar-avatar');
  if (av) av.textContent = initials(user.name);
}

function fmtDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('en-GB', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric',
  });
}

function fmtDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-GB', {
    weekday: 'short', day: 'numeric', month: 'short',
    hour: '2-digit', minute: '2-digit',
  });
}

function cap(str) {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

function categoryBadge(cat) {
  const map = {
    PROGRAMMING: 'badge-teal',  DESIGN:      'badge-amber',
    MATHEMATICS: 'badge-coral', LANGUAGES:   'badge-purple',
    BUSINESS:    'badge-blue',  SCIENCE:     'badge-green',
    ARTS:        'badge-amber', OTHER:       'badge-gray',
  };
  return map[cat] || 'badge-gray';
}

function statusBadge(status) {
  const map = {
    ACTIVE: 'badge-teal', PENDING: 'badge-amber', SUSPENDED: 'badge-coral',
    CONFIRMED: 'badge-teal', COMPLETED: 'badge-green',
    CANCELLED: 'badge-coral', ACCEPTED: 'badge-teal',
    DECLINED: 'badge-coral', WITHDRAWN: 'badge-gray',
  };
  return map[status] || 'badge-gray';
}

function stars(rating, max = 5) {
  const full = Math.round(rating || 0);
  return '★'.repeat(full) + '☆'.repeat(Math.max(0, max - full));
}

const AV_COLORS = ['av-teal', 'av-amber', 'av-coral', 'av-purple', 'av-blue', 'av-green'];
function avColor(id) { return AV_COLORS[(id || 0) % AV_COLORS.length]; }

function initials(name) {
  if (!name) return '?';
  const p = name.trim().split(' ');
  return ((p[0]?.[0] ?? '') + (p[1]?.[0] ?? '')).toUpperCase();
}
