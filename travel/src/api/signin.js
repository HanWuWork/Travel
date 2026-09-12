import { request } from '../utils/auth'

/** 签到状态（需登录） */
export function signInStatus() {
  return request('/api/signin/status')
}

/** 执行签到（需登录） */
export function doSignIn() {
  return request('/api/signin', { method: 'POST' })
}
