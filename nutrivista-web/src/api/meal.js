/**
 * 饮食记录模块 API
 */

import request from './request'

/** 获取某天饮食汇总（所有餐次 + 营养合计） */
export function getDailySummary(date) {
  return request.get('/api/meals/daily-summary', { params: { date }, hideLoading: true })
}

/** 向餐次添加食物（自动创建 MealRecord） */
export function addMealItem(data) {
  return request.post('/api/meals/items', data)
}

/** 更新食物克重 */
export function updateMealItem(id, data) {
  return request.patch(`/api/meals/items/${id}`, data, { hideLoading: true })
}

/** 删除单条食物明细 */
export function deleteMealItem(id) {
  return request.delete(`/api/meals/items/${id}`)
}

/** 删除整个餐次记录 */
export function deleteMeal(id) {
  return request.delete(`/api/meals/${id}`)
}
