import { request } from '../utils/auth'

/** 汇率换算（公开） */
export function convertCurrency({ from, to, amount }) {
  const params = new URLSearchParams({ from, to, amount })
  return request(`/api/exchange?${params.toString()}`)
}
