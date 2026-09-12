import { request } from '../utils/auth'

/** 通知列表（需登录） */
export function listNotifications(unreadOnly = false) {
  return request(`/api/notify/list?unreadOnly=${unreadOnly}`)
}

/** 未读数量（需登录） */
export function unreadCount() {
  return request('/api/notify/unread-count')
}

/** 标记单条已读（需登录） */
export function markRead(id) {
  return request(`/api/notify/${id}/read`, { method: 'POST' })
}

/** 全部已读（需登录） */
export function markAllRead() {
  return request('/api/notify/read-all', { method: 'POST' })
}

/** 删除通知（需登录） */
export function deleteNotification(id) {
  return request(`/api/notify/${id}`, { method: 'DELETE' })
}

/** 清空通知（需登录） */
export function clearNotifications() {
  return request('/api/notify/clear', { method: 'DELETE' })
}
