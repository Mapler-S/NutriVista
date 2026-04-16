/**
 * AI 饮食分析模块 API
 */
import request from './request'

// AI 接口调用 LLM，耗时较长，使用独立的超时时间
const AI_TIMEOUT = 120000 // 120s

/**
 * AI 饮食综合分析
 * @param {string} startDate - YYYY-MM-DD
 * @param {string} endDate   - YYYY-MM-DD
 * @param {number} userId    - 用户 ID（默认 1）
 */
export function analyzeDiet(startDate, endDate, userId = 1) {
  return request.post('/api/ai/analyze', { userId, startDate, endDate }, { timeout: AI_TIMEOUT })
}

/**
 * AI 个性化饮食建议
 * @param {number} userId - 用户 ID（默认 1）
 */
export function getRecommendation(userId = 1) {
  return request.post('/api/ai/recommend', { userId }, { timeout: AI_TIMEOUT })
}

/**
 * AI 营养趋势预测
 * @param {string} nutrient - 营养素名称（如 energyKcal, protein 等）
 * @param {number} days     - 预测天数（1-30）
 * @param {number} userId   - 用户 ID（默认 1）
 */
export function predictTrend(nutrient, days = 7, userId = 1) {
  return request.post('/api/ai/predict', { userId, nutrient, days }, { timeout: AI_TIMEOUT })
}
