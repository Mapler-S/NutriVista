/**
 * 统计分析 Pinia Store
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import dayjs from 'dayjs'
import { getDashboard } from '@/api/stats'

export const useStatsStore = defineStore('stats', () => {
  const dashboard = ref(null)
  const loading   = ref(false)
  const date      = ref(dayjs().format('YYYY-MM-DD'))
  const days      = ref(30)

  async function fetchDashboard(targetDate, trendDays) {
    const d = targetDate ?? date.value
    const n = trendDays  ?? days.value
    date.value = d
    days.value = n
    loading.value = true
    try {
      const res = await getDashboard(d, n)
      dashboard.value = res.data
    } finally {
      loading.value = false
    }
  }

  return { dashboard, loading, date, days, fetchDashboard }
})
