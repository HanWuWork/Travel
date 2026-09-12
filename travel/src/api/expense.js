import { request } from '../utils/auth'

/** 记一笔（需登录） */
export function addExpense({ tripId, category, amount, note, expenseDate }) {
  return request('/api/expense', {
    method: 'POST',
    body: JSON.stringify({ tripId, category, amount, note, expenseDate })
  })
}

/** 记账列表（需登录） */
export function listExpenses(tripId) {
  return request(`/api/expense/list${tripId ? `?tripId=${tripId}` : ''}`)
}

/** 记账统计（需登录） */
export function expenseStats(tripId) {
  return request(`/api/expense/stats${tripId ? `?tripId=${tripId}` : ''}`)
}

/** 删除记录（需登录） */
export function deleteExpense(id) {
  return request(`/api/expense/${id}`, { method: 'DELETE' })
}
