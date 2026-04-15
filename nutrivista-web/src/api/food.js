/**
 * 食物模块 API
 */

import request from './request'

/**
 * 分页查询食物列表
 * @param {Object} params - { keyword, categoryId, page, pageSize, sortBy, sortDir }
 */
export function getFoods(params) {
  return request.get('/api/foods', { params, hideLoading: true })
}

/**
 * 获取食物详情（含完整营养成分）
 * @param {number} id - 食物 ID
 */
export function getFoodById(id) {
  return request.get(`/api/foods/${id}`)
}

/**
 * 获取食物分类树
 */
export function getFoodCategories() {
  return request.get('/api/foods/categories', { hideLoading: true })
}

/**
 * 搜索联想建议（轻量接口，防抖调用）
 * @param {string} keyword - 搜索关键词
 */
export function getSearchSuggestions(keyword) {
  return request.get('/api/foods/search/suggestions', {
    params: { keyword },
    hideLoading: true,
  })
}
