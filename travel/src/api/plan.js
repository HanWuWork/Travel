/**
 * 旅游规划接口
 */

/**
 * 生成旅游行程规划
 * @param {Object} params
 * @param {string} params.destination 目的地
 * @param {number} params.budget      预算（元）
 * @param {number} params.days        天数
 * @returns {Promise<Object>} Result<TravelPlanVO>
 */
export async function createTravelPlan({ destination, budget, days }) {
  const response = await fetch('/api/travel/plan', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ destination, budget, days })
  })

  const data = await response.json()
  if (!response.ok || !data || !data.success) {
    throw new Error((data && data.message) || `生成行程失败（HTTP ${response.status}）`)
  }
  return data.data
}
