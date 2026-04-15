/**
 * 食物数据 Pinia Store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getFoods, getFoodById, getFoodCategories, getSearchSuggestions } from '@/api/food'

export const useFoodStore = defineStore('food', () => {
  // ===== State =====
  const foods = ref([])
  const totalFoods = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)
  const totalPages = ref(0)

  const categories = ref([])
  const selectedCategoryId = ref(null)
  const searchKeyword = ref('')

  const currentFood = ref(null)  // 当前查看的食物详情
  const suggestions = ref([])    // 搜索联想结果
  const loading = ref(false)

  // ===== Computed =====
  const hasMore = computed(() => currentPage.value < totalPages.value)

  // ===== Actions =====

  async function fetchFoods(params = {}) {
    loading.value = true
    try {
      const res = await getFoods({
        page: currentPage.value,
        pageSize: pageSize.value,
        keyword: searchKeyword.value || undefined,
        categoryId: selectedCategoryId.value || undefined,
        ...params,
      })
      foods.value = res.data.records
      totalFoods.value = res.data.total
      totalPages.value = res.data.totalPages
    } finally {
      loading.value = false
    }
  }

  async function fetchFoodById(id) {
    loading.value = true
    try {
      const res = await getFoodById(id)
      currentFood.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchCategories() {
    if (categories.value.length > 0) return  // 已加载则跳过
    const res = await getFoodCategories()
    categories.value = res.data
  }

  async function fetchSuggestions(keyword) {
    if (!keyword?.trim()) {
      suggestions.value = []
      return
    }
    const res = await getSearchSuggestions(keyword)
    suggestions.value = res.data || []
  }

  function setCategory(categoryId) {
    selectedCategoryId.value = categoryId
    currentPage.value = 1
    fetchFoods()
  }

  function setKeyword(keyword) {
    searchKeyword.value = keyword
    currentPage.value = 1
    fetchFoods()
  }

  function setPage(page) {
    currentPage.value = page
    fetchFoods()
  }

  function reset() {
    foods.value = []
    currentPage.value = 1
    totalFoods.value = 0
    selectedCategoryId.value = null
    searchKeyword.value = ''
    currentFood.value = null
    suggestions.value = []
  }

  return {
    // state
    foods,
    totalFoods,
    currentPage,
    pageSize,
    totalPages,
    categories,
    selectedCategoryId,
    searchKeyword,
    currentFood,
    suggestions,
    loading,
    // computed
    hasMore,
    // actions
    fetchFoods,
    fetchFoodById,
    fetchCategories,
    fetchSuggestions,
    setCategory,
    setKeyword,
    setPage,
    reset,
  }
})
