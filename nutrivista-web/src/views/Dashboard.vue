<template>
  <div class="dashboard" :class="{ 'dashboard--loading': store.loading }">

    <!-- ===== Header ===== -->
    <div class="db-header">
      <div class="db-header__left">
        <h1 class="db-header__title">数据概览</h1>
        <p class="db-header__sub">饮食营养可视化分析 · {{ displayDate }}</p>
      </div>
      <div class="db-header__controls">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :clearable="false"
          size="default"
          style="width:160px"
          @change="reload"
        />
        <el-radio-group v-model="selectedDays" size="default" @change="reload">
          <el-radio-button :value="7">7天</el-radio-button>
          <el-radio-button :value="14">14天</el-radio-button>
          <el-radio-button :value="30">30天</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- ===== Overview Cards ===== -->
    <div class="overview-cards" v-loading="store.loading">
      <div v-for="card in overviewCards" :key="card.key" class="ov-card" :style="{ '--accent': card.color }">
        <div class="ov-card__icon">{{ card.icon }}</div>
        <div class="ov-card__body">
          <div class="ov-card__label">{{ card.label }}</div>
          <div class="ov-card__value">
            <span class="ov-card__num">{{ card.value }}</span>
            <span class="ov-card__unit">{{ card.unit }}</span>
          </div>
          <div class="ov-card__sub">{{ card.sub }}</div>
        </div>
        <div class="ov-card__ring" v-if="card.pct !== undefined">
          <svg viewBox="0 0 40 40">
            <circle cx="20" cy="20" r="16" class="ring-bg"/>
            <circle cx="20" cy="20" r="16" class="ring-fg"
              :stroke="card.color"
              :stroke-dasharray="`${Math.min(card.pct,100) * 1.005} 100.5`"/>
          </svg>
          <span class="ov-card__pct">{{ Math.round(card.pct) }}%</span>
        </div>
        <div class="ov-card__badge" v-else>
          <span :style="{ color: card.color }">{{ card.badge }}</span>
        </div>
      </div>
    </div>

    <!-- ===== Insight strip ===== -->
    <div class="insights" v-if="insights.length">
      <div v-for="(ins,i) in insights" :key="i" class="insight-chip" :class="`insight-chip--${ins.type}`">
        <span>{{ ins.icon }}</span>{{ ins.text }}
      </div>
    </div>

    <!-- ===== Charts grid ===== -->
    <div class="charts-grid">

      <!-- Row 1: Trend (full width) -->
      <div class="chart-card span-full">
        <div class="chart-card__header">
          <span class="chart-card__title">热量摄入趋势</span>
          <span class="chart-card__sub">过去 {{ selectedDays }} 天 · 橙色虚线为目标热量</span>
        </div>
        <div ref="trendEl" class="chart-area chart-area--tall" />
      </div>

      <!-- Row 2: Macro donut + Nutrient adequacy -->
      <div class="chart-card span-5">
        <div class="chart-card__header">
          <span class="chart-card__title">今日宏量分布</span>
          <span class="chart-card__sub">热量来源占比</span>
        </div>
        <div ref="macroEl" class="chart-area" />
      </div>

      <div class="chart-card span-7">
        <div class="chart-card__header">
          <span class="chart-card__title">今日营养充足度</span>
          <span class="chart-card__sub">实际摄入 / 膳食参考值 (DRI)，绿色≥100%</span>
        </div>
        <div ref="adequacyEl" class="chart-area chart-area--tall" />
      </div>

      <!-- Row 3: Meal distribution + Top foods -->
      <div class="chart-card span-6">
        <div class="chart-card__header">
          <span class="chart-card__title">各餐次平均热量</span>
          <span class="chart-card__sub">该时段内各餐次平均摄入</span>
        </div>
        <div ref="mealDistEl" class="chart-area" />
      </div>

      <div class="chart-card span-6">
        <div class="chart-card__header">
          <span class="chart-card__title">最常吃食物 Top 10</span>
          <span class="chart-card__sub">按出现次数排行</span>
        </div>
        <div ref="topFoodsEl" class="chart-area" />
      </div>

    </div>

    <!-- ===== Recipe preference section ===== -->
    <div class="section-title">
      <span class="section-title__text">菜谱偏好分析</span>
      <span class="section-title__sub">基于 AI 推荐使用记录 · {{ selectedDays }} 天</span>
    </div>

    <!-- Overview stats strip -->
    <div class="recipe-stats-strip" v-loading="recipeStatsLoading">
      <div class="rst-item">
        <div class="rst-item__val">{{ recipeStats?.totalRecommendations ?? 0 }}</div>
        <div class="rst-item__label">推荐展示次数</div>
      </div>
      <div class="rst-divider" />
      <div class="rst-item">
        <div class="rst-item__val">{{ recipeStats?.totalAdopted ?? 0 }}</div>
        <div class="rst-item__label">实际采用次数</div>
      </div>
      <div class="rst-divider" />
      <div class="rst-item">
        <div class="rst-item__val rst-item__val--accent">{{ recipeStats?.adoptionRatePct ?? 0 }}%</div>
        <div class="rst-item__label">整体采用率</div>
      </div>
      <div class="rst-divider" />
      <div class="rst-item">
        <div class="rst-item__val">{{ recipeStats?.topRecipes?.length ?? 0 }}</div>
        <div class="rst-item__label">涉及菜谱种数</div>
      </div>
    </div>

    <div class="charts-grid" v-loading="recipeStatsLoading">

      <!-- Cuisine radar -->
      <div class="chart-card span-6">
        <div class="chart-card__header">
          <span class="chart-card__title">菜系偏好雷达图</span>
          <span class="chart-card__sub">推荐次数 vs 实际采用次数</span>
        </div>
        <div v-if="hasCuisineData" ref="cuisineRadarEl" class="chart-area chart-area--tall" />
        <div v-else class="chart-empty">暂无菜系数据，使用 AI 推荐菜谱后将在此显示</div>
      </div>

      <!-- Adoption trend -->
      <div class="chart-card span-6">
        <div class="chart-card__header">
          <span class="chart-card__title">推荐采用率趋势</span>
          <span class="chart-card__sub">每日采用率（%）= 当日采用数 / 推荐数</span>
        </div>
        <div v-if="hasTrendData" ref="adoptionTrendEl" class="chart-area chart-area--tall" />
        <div v-else class="chart-empty">暂无趋势数据，使用 AI 推荐菜谱后将在此显示</div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useStatsStore } from '@/stores/statsStore'
import dayjs from 'dayjs'
import * as echarts from 'echarts/core'
import {
  LineChart, PieChart, BarChart, RadarChart,
} from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent,
  MarkLineComponent, DataZoomComponent, RadarComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getRecipeUsageStats } from '@/api/stats'

echarts.use([
  LineChart, PieChart, BarChart, RadarChart,
  GridComponent, TooltipComponent, LegendComponent,
  MarkLineComponent, DataZoomComponent, RadarComponent,
  CanvasRenderer,
])

const store = useStatsStore()

// ===== Controls =====
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const selectedDays = ref(30)
const displayDate = computed(() => dayjs(selectedDate.value).format('YYYY年M月D日'))

function reload() {
  store.fetchDashboard(selectedDate.value, selectedDays.value)
  fetchRecipeStats()
}

// ===== Overview cards =====
const GOAL_ENERGY = 2000

const overviewCards = computed(() => {
  const t = store.dashboard?.today
  const db = store.dashboard
  if (!t) return []
  const energy = Number(t.energyKcal ?? 0)
  const protein = Number(t.protein ?? 0)
  const fat = Number(t.fat ?? 0)
  const carb = Number(t.carbohydrate ?? 0)
  return [
    {
      key: 'energy', icon: '🔥', label: '今日热量', color: '#F59E0B',
      value: Math.round(energy), unit: 'kcal',
      sub: `目标 ${GOAL_ENERGY} kcal`,
      pct: (energy / GOAL_ENERGY) * 100,
    },
    {
      key: 'protein', icon: '🥩', label: '蛋白质', color: '#10B981',
      value: protein.toFixed(1), unit: 'g',
      sub: `目标 75 g · ${((protein / 75) * 100).toFixed(0)}%`,
      pct: (protein / 75) * 100,
    },
    {
      key: 'fat', icon: '🥑', label: '脂肪', color: '#EF4444',
      value: fat.toFixed(1), unit: 'g',
      sub: `目标 65 g · ${((fat / 65) * 100).toFixed(0)}%`,
      pct: (fat / 65) * 100,
    },
    {
      key: 'streak', icon: '📅', label: '连续打卡', color: '#8B5CF6',
      value: db?.currentStreak ?? 0, unit: '天',
      sub: `本期记录 ${db?.totalLoggedDays ?? 0} 天 / ${selectedDays.value} 天`,
      badge: db?.currentStreak >= 7 ? '🔥' : '⭐',
    },
  ]
})

// ===== Insights =====
const insights = computed(() => {
  const db = store.dashboard
  if (!db) return []
  const t = db.today
  const energy = Number(t?.energyKcal ?? 0)
  const list = []
  if (energy === 0) {
    list.push({ icon: '📝', text: '今日尚未记录饮食，前往饮食记录开始记录', type: 'info' })
    return list
  }
  const ePct = (energy / GOAL_ENERGY) * 100
  if (ePct >= 110) list.push({ icon: '⚠️', text: `今日热量超标 ${Math.round(energy - GOAL_ENERGY)} kcal，注意控制摄入`, type: 'warning' })
  else if (ePct >= 90) list.push({ icon: '✅', text: '今日热量摄入达标，保持良好状态', type: 'success' })
  else if (ePct < 70) list.push({ icon: '💡', text: `今日热量偏低，还差约 ${Math.round(GOAL_ENERGY - energy)} kcal`, type: 'info' })
  if (Number(t.protein ?? 0) < 75 * 0.8) list.push({ icon: '🥩', text: '蛋白质摄入不足，建议增加瘦肉、蛋、豆制品', type: 'warning' })
  if (Number(t.fiber ?? 0) < 25 * 0.6) list.push({ icon: '🥦', text: '膳食纤维偏低，建议多吃蔬菜、粗粮', type: 'warning' })
  if (db.currentStreak >= 7) list.push({ icon: '🔥', text: `已连续记录 ${db.currentStreak} 天，坚持下去！`, type: 'success' })
  return list.slice(0, 3)
})

// ===== Recipe usage stats =====
const recipeStats        = ref(null)
const recipeStatsLoading = ref(false)

const hasCuisineData = computed(() => recipeStats.value?.cuisineStats?.length > 0)
const hasTrendData   = computed(() => recipeStats.value?.dailyTrend?.length > 0)

async function fetchRecipeStats() {
  recipeStatsLoading.value = true
  try {
    const endDate   = selectedDate.value
    const startDate = dayjs(endDate).subtract(selectedDays.value - 1, 'day').format('YYYY-MM-DD')
    const res = await getRecipeUsageStats(startDate, endDate)
    recipeStats.value = res.data
  } finally {
    recipeStatsLoading.value = false
  }
}

// ===== ECharts instances =====
const trendEl         = ref(null)
const macroEl         = ref(null)
const adequacyEl      = ref(null)
const mealDistEl      = ref(null)
const topFoodsEl      = ref(null)
const cuisineRadarEl  = ref(null)
const adoptionTrendEl = ref(null)

let charts = {}

function initChart(key, el) {
  if (!el) return
  charts[key]?.dispose()
  charts[key] = echarts.init(el, null, { renderer: 'canvas' })
}

// ===== Chart option builders =====

function trendOption(db) {
  if (!db?.trend?.length) return {}
  const dates  = db.trend.map(d => dayjs(d.date).format('M/D'))
  const energy = db.trend.map(d => Number(d.energyKcal ?? 0).toFixed(0))
  const protein = db.trend.map(d => Number(d.protein ?? 0).toFixed(1))
  return {
    animation: true,
    animationDuration: 900,
    animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      padding: [10, 14],
      textStyle: { color: '#111827', fontSize: 13 },
      formatter(params) {
        const d = db.trend[params[0].dataIndex]
        return `<b>${dayjs(d.date).format('YYYY-M-D')}</b><br/>
          热量 <b>${Number(d.energyKcal ?? 0).toFixed(0)} kcal</b><br/>
          蛋白质 ${Number(d.protein ?? 0).toFixed(1)}g &nbsp;
          脂肪 ${Number(d.fat ?? 0).toFixed(1)}g &nbsp;
          碳水 ${Number(d.carbohydrate ?? 0).toFixed(1)}g`
      },
    },
    legend: {
      data: ['热量(kcal)', '蛋白质(g)'],
      top: 0,
      right: 0,
      textStyle: { color: '#6B7280', fontSize: 12 },
    },
    grid: { top: 36, right: 20, bottom: 36, left: 60 },
    xAxis: {
      type: 'category', data: dates,
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisTick: { show: false },
      axisLabel: { color: '#9CA3AF', fontSize: 11 },
    },
    yAxis: [
      {
        type: 'value', name: 'kcal',
        nameTextStyle: { color: '#9CA3AF', fontSize: 11 },
        splitLine: { lineStyle: { color: '#F3F4F6', type: 'dashed' } },
        axisLabel: { color: '#9CA3AF', fontSize: 11 },
      },
      {
        type: 'value', name: 'g',
        nameTextStyle: { color: '#9CA3AF', fontSize: 11 },
        splitLine: { show: false },
        axisLabel: { color: '#9CA3AF', fontSize: 11 },
      },
    ],
    series: [
      {
        name: '热量(kcal)', type: 'line', yAxisIndex: 0, smooth: true,
        symbol: 'circle', symbolSize: 5, data: energy,
        lineStyle: { color: '#4F46E5', width: 2.5 },
        itemStyle: { color: '#4F46E5', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79,70,229,0.22)' },
            { offset: 1, color: 'rgba(79,70,229,0.01)' },
          ]),
        },
        markLine: {
          silent: true,
          data: [{ yAxis: GOAL_ENERGY, name: '目标热量' }],
          lineStyle: { color: '#F59E0B', type: 'dashed', width: 1.5 },
          label: { formatter: '目标 {c} kcal', color: '#F59E0B', fontSize: 11 },
          symbol: 'none',
        },
      },
      {
        name: '蛋白质(g)', type: 'line', yAxisIndex: 1, smooth: true,
        symbol: 'circle', symbolSize: 4, data: protein,
        lineStyle: { color: '#10B981', width: 1.8 },
        itemStyle: { color: '#10B981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16,185,129,0.12)' },
            { offset: 1, color: 'rgba(16,185,129,0.01)' },
          ]),
        },
      },
    ],
  }
}

function macroOption(db) {
  const pPct = Number(db?.proteinPct ?? 0)
  const fPct = Number(db?.fatPct ?? 0)
  const cPct = Number(db?.carbPct ?? 0)
  const totalKcal = Math.round(db?.today?.energyKcal ?? 0)
  return {
    animation: true,
    animationDuration: 900,
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}% ({d}%)',
      backgroundColor: '#fff', borderColor: '#E5E7EB', borderWidth: 1,
      padding: [8, 12], textStyle: { color: '#111827', fontSize: 13 },
    },
    legend: {
      orient: 'vertical', right: 10, top: 'center',
      textStyle: { color: '#6B7280', fontSize: 12 },
      formatter: name => {
        const map = { '蛋白质': `${pPct.toFixed(1)}%`, '脂肪': `${fPct.toFixed(1)}%`, '碳水化合物': `${cPct.toFixed(1)}%` }
        return `${name}  ${map[name] ?? ''}`
      },
    },
    graphic: [{
      type: 'text', left: '34%', top: '42%',
      style: { text: `${totalKcal}\nkcal`, textAlign: 'center', fill: '#111827', fontSize: 15, fontWeight: 'bold', lineHeight: 22 },
    }],
    series: [{
      type: 'pie', radius: ['52%', '74%'], center: ['36%', '50%'],
      avoidLabelOverlap: false, label: { show: false },
      emphasis: { scale: true, scaleSize: 6, label: { show: false } },
      data: [
        { name: '蛋白质',   value: pPct.toFixed(1), itemStyle: { color: '#10B981' } },
        { name: '脂肪',     value: fPct.toFixed(1), itemStyle: { color: '#EF4444' } },
        { name: '碳水化合物', value: cPct.toFixed(1), itemStyle: { color: '#3B82F6' } },
      ],
    }],
  }
}

function adequacyOption(db) {
  if (!db?.nutrientAdequacy?.length) return {}
  const items = [...db.nutrientAdequacy].reverse()
  const labels = items.map(n => n.label)
  const pcts   = items.map(n => Number(n.pct ?? 0))
  const actuals = items.map(n => `${Number(n.actual ?? 0).toFixed(1)} ${n.unit}`)
  return {
    animation: true, animationDuration: 1000, animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'axis', axisPointer: { type: 'none' },
      backgroundColor: '#fff', borderColor: '#E5E7EB', borderWidth: 1,
      padding: [8, 12], textStyle: { color: '#111827', fontSize: 13 },
      formatter(params) {
        const idx = items.length - 1 - params[0].dataIndex
        const n = db.nutrientAdequacy[idx]
        return `<b>${n.label}</b><br/>实际：${Number(n.actual).toFixed(1)} ${n.unit}<br/>参考：${Number(n.drv).toFixed(0)} ${n.unit}<br/>充足度：<b>${Number(n.pct).toFixed(1)}%</b>`
      },
    },
    grid: { top: 10, right: 70, bottom: 10, left: 80, containLabel: true },
    xAxis: {
      type: 'value', max: 200,
      axisLabel: { formatter: v => v + '%', color: '#9CA3AF', fontSize: 11 },
      splitLine: { lineStyle: { color: '#F3F4F6', type: 'dashed' } },
    },
    yAxis: {
      type: 'category', data: labels,
      axisLabel: { color: '#374151', fontSize: 12 },
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#E5E7EB' } },
    },
    series: [{
      type: 'bar', barMaxWidth: 18,
      data: pcts.map((pct, i) => ({
        value: pct,
        label: { show: true, position: 'right', formatter: `${actuals[i]}`, color: '#6B7280', fontSize: 11 },
        itemStyle: {
          color: pct >= 100 ? '#10B981' : pct >= 70 ? '#F59E0B' : '#EF4444',
          borderRadius: [0, 4, 4, 0],
        },
      })),
      markLine: {
        silent: true,
        data: [{ xAxis: 100, name: '100%' }],
        lineStyle: { color: '#9CA3AF', type: 'dashed', width: 1 },
        label: { formatter: '100%', color: '#9CA3AF', fontSize: 11 },
        symbol: 'none',
      },
    }],
  }
}

function mealDistOption(db) {
  if (!db?.mealDistribution?.length) return {}
  const MEAL_LABEL = {
    BREAKFAST: '早餐', MORNING_SNACK: '上午加餐',
    LUNCH: '午餐', AFTERNOON_TEA: '下午茶',
    DINNER: '晚餐', SUPPER: '宵夜',
  }
  const MEAL_COLOR = {
    BREAKFAST: '#F59E0B', MORNING_SNACK: '#10B981',
    LUNCH: '#3B82F6', AFTERNOON_TEA: '#8B5CF6',
    DINNER: '#EF4444', SUPPER: '#6B7280',
  }
  const dist = db.mealDistribution
  const labels = dist.map(d => MEAL_LABEL[d.mealType] ?? d.mealType)
  const values = dist.map(d => Number(d.avgEnergy ?? 0).toFixed(0))
  const colors = dist.map(d => MEAL_COLOR[d.mealType] ?? '#4F46E5')
  return {
    animation: true, animationDuration: 900,
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff', borderColor: '#E5E7EB', borderWidth: 1,
      padding: [8, 12], textStyle: { color: '#111827', fontSize: 13 },
      formatter(params) {
        const d = dist[params[0].dataIndex]
        return `<b>${MEAL_LABEL[d.mealType]}</b><br/>平均热量：<b>${Number(d.avgEnergy).toFixed(0)} kcal</b><br/>记录次数：${d.count} 次`
      },
    },
    grid: { top: 16, right: 20, bottom: 40, left: 20, containLabel: true },
    xAxis: {
      type: 'category', data: labels,
      axisLabel: { color: '#374151', fontSize: 12 },
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#E5E7EB' } },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F3F4F6', type: 'dashed' } },
      axisLabel: { color: '#9CA3AF', fontSize: 11, formatter: '{value} kcal' },
    },
    series: [{
      type: 'bar', barMaxWidth: 44,
      data: values.map((v, i) => ({
        value: v,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors[i] },
            { offset: 1, color: colors[i] + '55' },
          ]),
          borderRadius: [6, 6, 0, 0],
        },
        label: { show: true, position: 'top', color: '#374151', fontSize: 12, fontWeight: 'bold' },
      })),
    }],
  }
}

function topFoodsOption(db) {
  if (!db?.topFoods?.length) return {}
  const foods = [...db.topFoods].reverse()
  return {
    animation: true, animationDuration: 1000, animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'axis', axisPointer: { type: 'none' },
      backgroundColor: '#fff', borderColor: '#E5E7EB', borderWidth: 1,
      padding: [8, 12], textStyle: { color: '#111827', fontSize: 13 },
      formatter(params) {
        const f = foods[params[0].dataIndex]
        return `<b>${f.nameZh}</b><br/>出现次数：<b>${f.times}</b> 次<br/>累计食用：${Number(f.totalWeight).toFixed(0)} g`
      },
    },
    grid: { top: 10, right: 60, bottom: 10, left: 20, containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { formatter: v => v + ' 次', color: '#9CA3AF', fontSize: 11 },
      splitLine: { lineStyle: { color: '#F3F4F6', type: 'dashed' } },
    },
    yAxis: {
      type: 'category',
      data: foods.map(f => f.nameZh),
      axisLabel: { color: '#374151', fontSize: 12 },
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#E5E7EB' } },
    },
    series: [{
      type: 'bar', barMaxWidth: 18,
      data: foods.map((f, i) => ({
        value: f.times,
        label: { show: true, position: 'right', formatter: `${f.times}次`, color: '#6B7280', fontSize: 11 },
        itemStyle: {
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#4F46E5' },
            { offset: 1, color: `rgba(79,70,229,${0.3 + 0.07 * (foods.length - i)})` },
          ]),
          borderRadius: [0, 4, 4, 0],
        },
      })),
    }],
  }
}

function cuisineRadarOption(stats) {
  if (!stats?.cuisineStats?.length) return {}
  // 最多展示前 8 个菜系 + 合并其余为「其他」
  const MAX = 8
  let rows = [...stats.cuisineStats]
  if (rows.length > MAX) {
    const other = rows.slice(MAX).reduce((acc, r) => {
      acc.recommendCount += r.recommendCount
      acc.adoptedCount   += r.adoptedCount
      return acc
    }, { cuisine: '其他', recommendCount: 0, adoptedCount: 0 })
    rows = [...rows.slice(0, MAX), other]
  }
  const maxVal = Math.max(...rows.flatMap(r => [r.recommendCount, r.adoptedCount]), 1)
  return {
    animation: true, animationDuration: 1000,
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff', borderColor: '#E5E7EB', borderWidth: 1,
      padding: [8, 12], textStyle: { color: '#111827', fontSize: 13 },
      formatter(params) {
        const idx = params.dataIndex
        const r   = rows[idx]
        if (!r) return ''
        return `<b>${r.cuisine}</b><br/>推荐展示：${r.recommendCount} 次<br/>实际采用：${r.adoptedCount} 次`
      },
    },
    legend: {
      data: ['推荐次数', '实际采用'],
      bottom: 0, textStyle: { color: '#6B7280', fontSize: 12 },
    },
    radar: {
      indicator: rows.map(r => ({ name: r.cuisine, max: maxVal })),
      shape: 'polygon',
      splitNumber: 4,
      axisName: { color: '#374151', fontSize: 12 },
      splitLine:  { lineStyle: { color: '#E5E7EB' } },
      splitArea:  { areaStyle: { color: ['rgba(79,70,229,0.02)', 'rgba(79,70,229,0.05)'] } },
      axisLine:   { lineStyle: { color: '#E5E7EB' } },
    },
    series: [{
      type: 'radar',
      data: [
        {
          name: '推荐次数',
          value: rows.map(r => r.recommendCount),
          lineStyle: { color: '#4F46E5', width: 2 },
          itemStyle: { color: '#4F46E5' },
          areaStyle:  { color: 'rgba(79,70,229,0.12)' },
        },
        {
          name: '实际采用',
          value: rows.map(r => r.adoptedCount),
          lineStyle: { color: '#10B981', width: 2 },
          itemStyle: { color: '#10B981' },
          areaStyle:  { color: 'rgba(16,185,129,0.15)' },
        },
      ],
    }],
  }
}

function adoptionTrendOption(stats) {
  if (!stats?.dailyTrend?.length) return {}
  const trend  = stats.dailyTrend
  const dates  = trend.map(d => dayjs(d.date).format('M/D'))
  const rates  = trend.map(d => +(d.adoptionRatePct ?? 0).toFixed(1))
  const adopted = trend.map(d => d.adopted)
  return {
    animation: true, animationDuration: 900, animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff', borderColor: '#E5E7EB', borderWidth: 1,
      padding: [10, 14], textStyle: { color: '#111827', fontSize: 13 },
      formatter(params) {
        const i = params[0].dataIndex
        const d = trend[i]
        return `<b>${dayjs(d.date).format('YYYY-M-D')}</b><br/>
          推荐展示：${d.recommended} 次<br/>
          实际采用：<b>${d.adopted}</b> 次<br/>
          采用率：<b>${(d.adoptionRatePct ?? 0).toFixed(1)}%</b>`
      },
    },
    legend: {
      data: ['采用率(%)', '采用次数'],
      top: 0, right: 0, textStyle: { color: '#6B7280', fontSize: 12 },
    },
    grid: { top: 36, right: 20, bottom: 36, left: 50 },
    xAxis: {
      type: 'category', data: dates,
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisTick: { show: false },
      axisLabel: { color: '#9CA3AF', fontSize: 11 },
    },
    yAxis: [
      {
        type: 'value', name: '%', max: 100,
        nameTextStyle: { color: '#9CA3AF', fontSize: 11 },
        axisLabel: { color: '#9CA3AF', fontSize: 11, formatter: '{value}%' },
        splitLine: { lineStyle: { color: '#F3F4F6', type: 'dashed' } },
      },
      {
        type: 'value', name: '次',
        nameTextStyle: { color: '#9CA3AF', fontSize: 11 },
        axisLabel: { color: '#9CA3AF', fontSize: 11 },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: '采用率(%)', type: 'line', yAxisIndex: 0, smooth: true,
        symbol: 'circle', symbolSize: 5, data: rates,
        lineStyle: { color: '#10B981', width: 2.5 },
        itemStyle: { color: '#10B981', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16,185,129,0.20)' },
            { offset: 1, color: 'rgba(16,185,129,0.01)' },
          ]),
        },
      },
      {
        name: '采用次数', type: 'bar', yAxisIndex: 1, barMaxWidth: 20, data: adopted,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4F46E5' },
            { offset: 1, color: 'rgba(79,70,229,0.3)' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      },
    ],
  }
}

// ===== Render all charts =====

function renderCharts(db) {
  if (!db) return
  nextTick(() => {
    initChart('trend',    trendEl.value)
    initChart('macro',    macroEl.value)
    initChart('adequacy', adequacyEl.value)
    initChart('mealDist', mealDistEl.value)
    initChart('topFoods', topFoodsEl.value)

    charts.trend?.setOption(trendOption(db))
    charts.macro?.setOption(macroOption(db))
    charts.adequacy?.setOption(adequacyOption(db))
    charts.mealDist?.setOption(mealDistOption(db))
    charts.topFoods?.setOption(topFoodsOption(db))
  })
}

function renderRecipeCharts(stats) {
  if (!stats) return
  nextTick(() => {
    if (hasCuisineData.value && cuisineRadarEl.value) {
      initChart('cuisineRadar', cuisineRadarEl.value)
      charts.cuisineRadar?.setOption(cuisineRadarOption(stats))
    }
    if (hasTrendData.value && adoptionTrendEl.value) {
      initChart('adoptionTrend', adoptionTrendEl.value)
      charts.adoptionTrend?.setOption(adoptionTrendOption(stats))
    }
  })
}

// ===== Resize handling =====
let resizeObserver
function setupResize() {
  resizeObserver = new ResizeObserver(() => {
    Object.values(charts).forEach(c => c?.resize())
  })
  document.querySelectorAll('.charts-grid').forEach(g => resizeObserver.observe(g))
}

// ===== Lifecycle =====
watch(() => store.dashboard, (db) => renderCharts(db))
watch(recipeStats, (stats) => renderRecipeCharts(stats))

onMounted(() => {
  store.fetchDashboard(selectedDate.value, selectedDays.value)
  fetchRecipeStats()
  setupResize()
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  Object.values(charts).forEach(c => c?.dispose())
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: $space-5 $space-6;
  display: flex;
  flex-direction: column;
  gap: $space-6;
  transition: opacity 0.2s ease;

  &--loading { opacity: 0.7; pointer-events: none; }
}

// ===== Header =====
.db-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: $space-4;

  &__title {
    font-size: $font-size-3xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    line-height: 1.2;
  }

  &__sub {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin-top: $space-1;
  }

  &__controls {
    display: flex;
    gap: $space-3;
    align-items: center;
    flex-wrap: wrap;
  }
}

// ===== Overview Cards =====
.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-4;
}

.ov-card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-xl;
  padding: $space-5;
  display: flex;
  align-items: center;
  gap: $space-4;
  transition: $transition-base;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 3px;
    background: var(--accent);
    border-radius: $radius-xl $radius-xl 0 0;
  }

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  &__icon { font-size: 28px; flex-shrink: 0; }

  &__body { flex: 1; min-width: 0; }

  &__label {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    font-weight: $font-weight-medium;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    margin-bottom: $space-1;
  }

  &__value { display: flex; align-items: baseline; gap: 4px; margin-bottom: 2px; }

  &__num {
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    line-height: 1;
  }

  &__unit { font-size: $font-size-sm; color: $color-text-secondary; }

  &__sub { font-size: $font-size-xs; color: $color-text-tertiary; }

  &__ring {
    position: relative;
    width: 48px;
    height: 48px;
    flex-shrink: 0;

    svg {
      width: 100%;
      height: 100%;
      transform: rotate(-90deg);
    }

    .ring-bg {
      fill: none;
      stroke: $color-bg-secondary;
      stroke-width: 5;
    }

    .ring-fg {
      fill: none;
      stroke-width: 5;
      stroke-linecap: round;
      stroke-dashoffset: 0;
      transition: stroke-dasharray 0.8s cubic-bezier(0.4, 0, 0.2, 1);
    }
  }

  &__pct {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
  }

  &__badge { font-size: 28px; flex-shrink: 0; }
}

// ===== Insights =====
.insights {
  display: flex;
  flex-wrap: wrap;
  gap: $space-2;
}

.insight-chip {
  display: inline-flex;
  align-items: center;
  gap: $space-2;
  padding: 6px $space-4;
  border-radius: $radius-full;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;

  &--success { background: $color-success-light; color: darken(#10B981, 15%); }
  &--warning { background: $color-warning-light; color: darken(#F59E0B, 20%); }
  &--info    { background: $color-info-light;    color: darken(#3B82F6, 10%); }
}

// ===== Charts grid =====
.charts-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: $space-4;
}

.span-full { grid-column: span 12; }
.span-5    { grid-column: span 5; }
.span-7    { grid-column: span 7; }
.span-6    { grid-column: span 6; }

.chart-card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-xl;
  padding: $space-5;
  transition: $transition-base;

  &:hover { box-shadow: $shadow-md; }

  &__header {
    display: flex;
    align-items: baseline;
    gap: $space-3;
    margin-bottom: $space-4;
  }

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  &__sub {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
  }
}

.chart-area {
  width: 100%;
  height: 240px;

  &--tall { height: 300px; }
}

// ===== Recipe section =====
.section-title {
  display: flex;
  align-items: baseline;
  gap: $space-3;
  padding-bottom: $space-2;
  border-bottom: 2px solid $color-accent;

  &__text {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  &__sub {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
  }
}

.recipe-stats-strip {
  display: flex;
  align-items: center;
  gap: 0;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-xl;
  padding: $space-5 $space-6;
}

.rst-item {
  flex: 1;
  text-align: center;

  &__val {
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    font-family: 'JetBrains Mono', monospace;
    color: $color-text-primary;
    line-height: 1;
    margin-bottom: $space-1;

    &--accent { color: $color-accent; }
  }

  &__label {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    font-weight: $font-weight-medium;
  }
}

.rst-divider {
  width: 1px;
  height: 40px;
  background: $color-border;
  flex-shrink: 0;
}

.chart-empty {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  color: $color-text-disabled;
  text-align: center;
  padding: $space-6;
}
</style>
