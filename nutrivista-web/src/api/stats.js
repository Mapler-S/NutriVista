/**
 * 统计分析模块 API
 */
import request from './request'

/**
 * 仪表盘全量数据（一次请求，所有图表）
 * @param {string} date    - YYYY-MM-DD，默认今天
 * @param {number} days    - 趋势天数：7 / 14 / 30
 */
export function getDashboard(date, days = 30) {
  return request.get('/api/stats/dashboard', {
    params: { date, days },
    hideLoading: true,
  })
}

/**
 * 菜谱使用统计（推荐次数、菜系分布、采用率趋势、Top-10 菜谱）
 * @param {string} startDate - YYYY-MM-DD
 * @param {string} endDate   - YYYY-MM-DD
 */
export function getRecipeUsageStats(startDate, endDate) {
  return request.get('/api/stats/recipe-usage', {
    params: { startDate, endDate },
    hideLoading: true,
  })
}
