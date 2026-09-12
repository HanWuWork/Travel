import { request } from '../utils/auth'

/** 清单（分组 + 完成度，需登录） */
export function packingList(tripId) {
  return request(`/api/packing/list${tripId ? `?tripId=${tripId}` : ''}`)
}

/** 新增条目（需登录） */
export function addPackingItem({ tripId, name, category }) {
  const params = new URLSearchParams({ name })
  if (tripId) params.set('tripId', tripId)
  if (category) params.set('category', category)
  return request(`/api/packing?${params.toString()}`, { method: 'POST' })
}

/** 勾选/取消（需登录） */
export function togglePackingItem(id) {
  return request(`/api/packing/${id}/toggle`, { method: 'POST' })
}

/** 一键套用模板（需登录） */
export function applyPackingTemplate(tripId) {
  return request(`/api/packing/template${tripId ? `?tripId=${tripId}` : ''}`, { method: 'POST' })
}

/** 删除条目（需登录） */
export function deletePackingItem(id) {
  return request(`/api/packing/${id}`, { method: 'DELETE' })
}

/** 清空清单（需登录） */
export function clearPackingList(tripId) {
  return request(`/api/packing/clear${tripId ? `?tripId=${tripId}` : ''}`, { method: 'DELETE' })
}
