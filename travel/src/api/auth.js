import { request, setToken, setUser, clearAuth } from '../utils/auth'

/** 注册 */
export function register({ username, password, nickname }) {
  return request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password, nickname })
  })
}

/** 登录，成功后自动保存 token 和用户信息 */
export async function login({ username, password }) {
  const data = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  })
  if (data && data.token) {
    setToken(data.token)
    setUser(data.user)
  }
  return data
}

/** 登出 */
export async function logout() {
  try {
    await request('/api/auth/logout', { method: 'POST' })
  } finally {
    clearAuth()
  }
}

/** 获取当前用户信息 */
export function getUserInfo() {
  return request('/api/user/info')
}
