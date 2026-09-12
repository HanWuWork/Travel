/**
 * 坐标系转换：WGS-84（GPS 原始坐标）→ GCJ-02（火星坐标，高德/腾讯地图使用）
 * 地图底图使用高德瓦片（GCJ-02），因此 WGS-84 的标记点需要先转换再落图。
 */
const PI = 3.1415926535897932384626
const A = 6378245.0
const EE = 0.00669342162296594323

function transformLat(x, y) {
  let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
  ret += ((20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0) / 3.0
  ret += ((20.0 * Math.sin(y * PI) + 40.0 * Math.sin((y / 3.0) * PI)) * 2.0) / 3.0
  ret += ((160.0 * Math.sin((y / 12.0) * PI) + 320 * Math.sin((y * PI) / 30.0)) * 2.0) / 3.0
  return ret
}

function transformLng(x, y) {
  let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
  ret += ((20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0) / 3.0
  ret += ((20.0 * Math.sin(x * PI) + 40.0 * Math.sin((x / 3.0) * PI)) * 2.0) / 3.0
  ret += ((150.0 * Math.sin((x / 12.0) * PI) + 300.0 * Math.sin((x / 30.0) * PI)) * 2.0) / 3.0
  return ret
}

function outOfChina(lng, lat) {
  return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
}

/**
 * WGS-84 转 GCJ-02
 * @param {number} lat 纬度
 * @param {number} lng 经度
 * @returns {[number, number]} [gcjLat, gcjLng]
 */
export function wgs84ToGcj02(lat, lng) {
  if (outOfChina(lng, lat)) {
    return [lat, lng]
  }
  let dLat = transformLat(lng - 105.0, lat - 35.0)
  let dLng = transformLng(lng - 105.0, lat - 35.0)
  const radLat = (lat / 180.0) * PI
  let magic = Math.sin(radLat)
  magic = 1 - EE * magic * magic
  const sqrtMagic = Math.sqrt(magic)
  dLat = (dLat * 180.0) / (((A * (1 - EE)) / (magic * sqrtMagic)) * PI)
  dLng = (dLng * 180.0) / ((A / sqrtMagic) * Math.cos(radLat) * PI)
  return [lat + dLat, lng + dLng]
}

/**
 * 批量转换 [{lat, lng}] → 追加 gcjLat/gcjLng 字段的新数组
 */
export function wgs84ToGcj02Batch(points) {
  return points.map((p) => {
    const [gcjLat, gcjLng] = wgs84ToGcj02(p.lat, p.lng)
    return { ...p, gcjLat, gcjLng }
  })
}
