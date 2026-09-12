/**
 * 进程内简单缓存（带 TTL），用于城市列表等不常变化的静态数据，
 * 避免每次进入页面都重复请求。适合 SPA 单页生命周期内复用。
 */
const store = new Map()

/**
 * 带过期时间的缓存读取
 * @param {string} key     缓存键
 * @param {number} ttlMs   有效期（毫秒）
 * @param {() => Promise<any>} loader 未命中/已过期时的加载函数
 * @returns {Promise<any>}
 */
export async function cached(key, ttlMs, loader) {
  const hit = store.get(key)
  if (hit && Date.now() - hit.ts < ttlMs) {
    return hit.value
  }
  const value = await loader()
  store.set(key, { value, ts: Date.now() })
  return value
}

/** 清除指定缓存（数据变更后调用） */
export function invalidate(key) {
  store.delete(key)
}