// Keeps the login (user details + JWT) across page refreshes.
// Trade-off: localStorage is readable by any script on the page (XSS); an httpOnly cookie would be safer.
const AUTH_STORAGE_KEY = "hrms_auth";

export function loadAuth() {
  try {
    return JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY));
  } catch {
    return null;
  }
}

export function saveAuth(loginResponse) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(loginResponse));
}

export function clearAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY);
}

export function getToken() {
  return loadAuth()?.token ?? null;
}
