import { request } from '../utils/auth'

/** 我的足迹列表（需登录） */
export function listCheckins() {
  return request('/api/checkin/list')
}

/** 打卡/更新状态（需登录） */
export function markCheckin(cityId, status, note) {
  const params = new URLSearchParams({ cityId, status })
  if (note) params.set('note', note)
  return request(`/api/checkin/mark?${params.toString()}`, { method: 'POST' })
}

/** 取消打卡（需登录） */
export function removeCheckin(cityId) {
  return request(`/api/checkin/remove?cityId=${cityId}`, { method: 'DELETE' })
}

/** 足迹统计（需登录） */
export function checkinStats() {
  return request('/api/checkin/stats')
}
