import { request } from '../utils/auth'

/** 创建/获取分享码（需登录，仅行程创建者） */
export function createShare(tripId) {
  return request(`/api/collab/share?tripId=${tripId}`, { method: 'POST' })
}

/** 分享信息（需登录，凭分享码） */
export function collabInfo(code) {
  return request(`/api/collab/info?code=${encodeURIComponent(code)}`)
}

/** 加入协作（需登录） */
export function joinCollab(code) {
  return request(`/api/collab/join?code=${encodeURIComponent(code)}`, { method: 'POST' })
}

/** 我参与协作的行程（需登录） */
export function listCollabTrips() {
  return request('/api/collab/trips')
}

/** 协作行程详情（需登录，含 plan 与 version） */
export function collabTripDetail(tripId) {
  return request(`/api/collab/trips/${tripId}`)
}

/** 协作编辑保存（需登录） */
export function updateCollabTrip(tripId, plan) {
  return request(`/api/collab/trips/${tripId}`, {
    method: 'POST',
    body: JSON.stringify({ plan })
  })
}

/** 成员列表（需登录） */
export function collabMembers(tripId) {
  return request(`/api/collab/trips/${tripId}/members`)
}

/** 移除成员 / 退出协作（需登录） */
export function removeCollabMember(tripId, memberId) {
  return request(`/api/collab/trips/${tripId}/members/${memberId}`, { method: 'DELETE' })
}
