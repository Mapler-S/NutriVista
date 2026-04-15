/**
 * 数据管理模块 API
 */
import request from './request'

/**
 * 导出饮食数据为文件（blob）
 * @param {string} from   - YYYY-MM-DD
 * @param {string} to     - YYYY-MM-DD
 * @param {string} format - 'csv' | 'json'
 */
export function exportData(from, to, format = 'csv') {
  return request.get('/api/data/export', {
    params: { from, to, format },
    responseType: 'blob',
    hideLoading: true,
  })
}

/**
 * 上传 CSV 文件批量导入
 * @param {File} file
 */
export function importData(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/data/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 下载导入模板 CSV */
export function downloadTemplate() {
  return request.get('/api/data/template', {
    responseType: 'blob',
    hideLoading: true,
  })
}
