import request from './request'

/** RAG 菜谱推荐 */
export function getRecipeRecommendations(data) {
  return request.post('/api/recipes/recommend', data)
}

/** 菜谱详情 */
export function getRecipeDetail(id) {
  return request.get(`/api/recipes/${id}`)
}

/** 关键词 + 菜系 + 分类分页搜索 */
export function searchRecipes(params) {
  return request.get('/api/recipes/search', { params })
}

/** 菜系列表及各菜谱数量 */
export function getCuisines() {
  return request.get('/api/recipes/cuisines')
}

/** 将菜谱食材按份数添加到饮食记录 */
export function addRecipeToMeal(id, data) {
  return request.post(`/api/recipes/${id}/add-to-meal`, data)
}
