import { request } from '../utils/auth'
import { cached } from '../utils/cache'

/** 城市列表（热门优先；进程内缓存 10 分钟） */
export function listCities() {
  return cached('cities', 10 * 60 * 1000, () => request('/api/dest/cities'))
}

/** 城市详情 */
export function getCity(id) {
  return request(`/api/dest/cities/${id}`)
}

/** 城市下的地点列表（地图总览/景点库共用） */
export function listAttractions({ cityId, type, keyword } = {}) {
  const params = new URLSearchParams({ cityId })
  if (type) params.set('type', type)
  if (keyword) params.set('keyword', keyword)
  return request(`/api/dest/attractions?${params.toString()}`)
}

/** 地点详情 */
export function getAttraction(id) {
  return request(`/api/dest/attractions/${id}`)
}

/** 附近地点（按距离升序） */
export function nearby({ lat, lng, radiusKm = 20, type } = {}) {
  const params = new URLSearchParams({ lat, lng, radiusKm })
  if (type) params.set('type', type)
  return request(`/api/dest/nearby?${params.toString()}`)
}

/** 热门景点（全库按评分排序，用于首页推荐位） */
export function hotSpots(limit = 6) {
  return request(`/api/dest/hot-spots?limit=${limit}`)
}
