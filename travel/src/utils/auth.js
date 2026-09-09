/**
 * 登录态管理：token、当前用户信息持久化到 localStorage
 */
const TOKEN_KEY = 'travel_token'
const USER_KEY = 'travel_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function isLoggedIn() {
  return !!getToken()
}

/** 统一请求封装：自动携带 token，401 自动清除登录态 */
export async function request(url, options = {}) {
  const headers = { ...(options.headers || {}) }
  const token = getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`
  if (options.body && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
  }

  const response = await fetch(url, { ...options, headers })

  // 401 未登录：清除本地登录态
  if (response.status === 401) {
    clearAuth()
    throw new Error('登录已过期，请重新登录')
  }

  let data = null
  try {
    data = await response.json()
  } catch (e) {
    // 忽略解析失败
  }

  if (!response.ok || (data && data.success === false)) {
    throw new Error((data && data.message) || `请求失败（HTTP ${response.status}）`)
  }
  return data && data.data !== undefined ? data.data : data
}
