import { request } from '../utils/auth'

/** 当前用户资料（需登录） */
export function getProfile() {
  return request('/api/profile')
}

/** 更新资料（需登录） */
export function updateProfile({ nickname, bio, city, avatar }) {
  return request('/api/profile', {
    method: 'PUT',
    body: JSON.stringify({ nickname, bio, city, avatar })
  })
}

/** 上传头像（需登录），返回 { url } */
export async function uploadAvatar(file) {
  const form = new FormData()
  form.append('file', file)
  return request('/api/profile/avatar', { method: 'POST', body: form })
}
