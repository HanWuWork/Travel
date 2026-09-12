import { request } from '../utils/auth'

/** 个性化推荐（需登录） */
export function getRecommend() {
  return request('/api/recommend')
}
