import { request } from '../utils/auth'

/** 添加收藏 */
export function addFavorite({ targetType, targetId, title, image, description }) {
  return request('/api/favorite/add', {
    method: 'POST',
    body: JSON.stringify({ targetType, targetId: targetId || 0, title, image, description })
  })
}

/** 我的收藏列表 */
export function listFavorites() {
  return request('/api/favorite/list')
}

/** 取消收藏 */
export function deleteFavorite(id) {
  return request(`/api/favorite/${id}`, { method: 'DELETE' })
}

/** 检查是否已收藏 */
export function checkFavorite(targetId, targetType) {
  return request(`/api/favorite/check?targetId=${targetId || 0}&targetType=${targetType}`)
}
