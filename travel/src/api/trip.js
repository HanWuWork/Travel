import { request } from '../utils/auth'
import { createTravelPlan } from './plan'

/** 保存行程（需登录） */
export function saveTrip({ startDate, plan }) {
  return request('/api/trip/save', {
    method: 'POST',
    body: JSON.stringify({ startDate, plan })
  })
}

/** 我的行程列表（需登录） */
export function listTrips() {
  return request('/api/trip/list')
}

/** 行程详情（需登录） */
export function getTrip(id) {
  return request(`/api/trip/${id}`)
}

/** 删除行程（需登录） */
export function deleteTrip(id) {
  return request(`/api/trip/${id}`, { method: 'DELETE' })
}

/** 更新行程（微调后保存，需登录） */
export function updateTrip(id, { startDate, plan }) {
  return request(`/api/trip/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ startDate, plan })
  })
}

/** AI 行程微调（公开）：基于当前行程与一句要求返回调整后的行程 */
export function refineTrip({ plan, instruction }) {
  return request('/api/travel/refine', {
    method: 'POST',
    body: JSON.stringify({ plan, instruction })
  })
}

export { createTravelPlan }
