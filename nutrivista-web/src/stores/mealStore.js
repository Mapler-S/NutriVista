/**
 * 饮食记录 Pinia Store
 * 服务端驱动：每次操作后重新拉取当天汇总，确保数据一致
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import { getDailySummary, addMealItem, updateMealItem, deleteMealItem, deleteMeal } from '@/api/meal'
import { ElMessage } from 'element-plus'

export const useMealStore = defineStore('meal', () => {
  // ===== State =====
  const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
  const dailySummary = ref(null)   // DailySummaryDto
  const loading = ref(false)
  const submitting = ref(false)

  // ===== Computed =====

  const meals = computed(() => dailySummary.value?.meals ?? [])

  const dayTotals = computed(() => ({
    energyKcal:    dailySummary.value?.totalEnergyKcal    ?? 0,
    protein:       dailySummary.value?.totalProtein        ?? 0,
    fat:           dailySummary.value?.totalFat            ?? 0,
    carbohydrate:  dailySummary.value?.totalCarbohydrate   ?? 0,
    fiber:         dailySummary.value?.totalFiber          ?? 0,
  }))

  function getMealByType(mealType) {
    return meals.value.find(m => m.mealType === mealType) ?? null
  }

  // ===== Actions =====

  async function fetchDay(date) {
    const target = date ?? selectedDate.value
    loading.value = true
    try {
      const res = await getDailySummary(target)
      dailySummary.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function addItem(mealType, foodId, weightGram) {
    submitting.value = true
    try {
      await addMealItem({
        mealDate: selectedDate.value,
        mealType,
        foodId,
        weightGram,
      })
      ElMessage.success('已添加')
      await fetchDay()
    } finally {
      submitting.value = false
    }
  }

  async function updateItem(itemId, weightGram) {
    await updateMealItem(itemId, { weightGram })
    await fetchDay()
  }

  async function removeItem(itemId) {
    await deleteMealItem(itemId)
    ElMessage.success('已删除')
    await fetchDay()
  }

  async function removeMeal(mealId) {
    await deleteMeal(mealId)
    ElMessage.success('餐次已清除')
    await fetchDay()
  }

  function setDate(date) {
    selectedDate.value = date
    fetchDay(date)
  }

  return {
    selectedDate,
    dailySummary,
    loading,
    submitting,
    meals,
    dayTotals,
    getMealByType,
    fetchDay,
    addItem,
    updateItem,
    removeItem,
    removeMeal,
    setDate,
  }
})
