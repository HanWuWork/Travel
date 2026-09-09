import { request } from '../utils/auth'

/** 创建订单（保存行程规划） */
export function createOrder({ destination, days, budget, planJson }) {
  return request('/api/order/create', {
    method: 'POST',
    body: JSON.stringify({ destination, days, budget, planJson })
  })
}

/** 我的订单列表 */
export function listOrders() {
  return request('/api/order/list')
}

/** 删除订单 */
export function deleteOrder(id) {
  return request(`/api/order/${id}`, { method: 'DELETE' })
}
