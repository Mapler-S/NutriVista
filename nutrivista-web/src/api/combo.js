/**
 * 常用组合模块 API
 */

import request from './request'

export function getCombos() {
  return request.get('/api/combos')
}

export function getComboById(id) {
  return request.get(`/api/combos/${id}`)
}

export function createCombo(data) {
  return request.post('/api/combos', data)
}

export function updateCombo(id, data) {
  return request.put(`/api/combos/${id}`, data)
}

export function deleteCombo(id) {
  return request.delete(`/api/combos/${id}`)
}
