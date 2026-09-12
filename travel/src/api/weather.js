import { request } from '../utils/auth'

/** 按城市名查询天气（公开） */
export function weatherByCity(city, days = 7) {
  return request(`/api/weather?city=${encodeURIComponent(city)}&days=${days}`)
}

/** 按坐标查询天气（公开） */
export function weatherByCoords(lat, lng, days = 7) {
  return request(`/api/weather/coords?lat=${lat}&lng=${lng}&days=${days}`)
}
