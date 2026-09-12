import { request } from '../utils/auth'

/** 某地点评价列表（公开） */
export function listReviews(attractionId) {
  return request(`/api/review/list?attractionId=${attractionId}`)
}

/** 某地点评价汇总（公开） */
export function reviewSummary(attractionId) {
  return request(`/api/review/summary?attractionId=${attractionId}`)
}

/** 我发布的评价（需登录） */
export function myReviews() {
  return request('/api/review/mine')
}

/** 提交/更新评价（需登录） */
export function submitReview({ attractionId, rating, content, tags }) {
  return request('/api/review', {
    method: 'POST',
    body: JSON.stringify({ attractionId, rating, content, tags })
  })
}

/** 删除评价（需登录） */
export function deleteReview(id) {
  return request(`/api/review/${id}`, { method: 'DELETE' })
}
