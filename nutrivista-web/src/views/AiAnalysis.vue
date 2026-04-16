<template>
  <div class="ai-page">

    <!-- ===== Header ===== -->
    <div class="ai-header">
      <div class="ai-header__left">
        <h1 class="ai-header__title">AI 饮食分析</h1>
        <p class="ai-header__sub">基于大语言模型的智能饮食评估、个性化建议与趋势预测</p>
      </div>
    </div>

    <!-- ===== Tab 切换 ===== -->
    <el-radio-group v-model="activeTab" size="default" class="ai-tabs">
      <el-radio-button value="analysis">综合分析</el-radio-button>
      <el-radio-button value="recommend">个性化建议</el-radio-button>
      <el-radio-button value="predict">趋势预测</el-radio-button>
    </el-radio-group>

    <!-- ============================================================ -->
    <!-- Tab 1: 综合分析                                               -->
    <!-- ============================================================ -->
    <div v-if="activeTab === 'analysis'" class="tab-panel">
      <div class="panel-controls">
        <el-date-picker v-model="analysisRange" type="daterange" range-separator="至"
          start-placeholder="开始日期" end-placeholder="结束日期"
          format="YYYY-MM-DD" value-format="YYYY-MM-DD"
          :clearable="false" size="default" style="width: 300px" />
        <el-button type="primary" :loading="analysisLoading" @click="runAnalysis">
          <el-icon><MagicStick /></el-icon>开始分析
        </el-button>
      </div>

      <!-- 结果 -->
      <div v-if="analysisResult" class="analysis-result" v-loading="analysisLoading">

        <!-- 评分卡 -->
        <div class="score-card">
          <div class="score-ring" :class="scoreClass">
            <svg viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="42" class="ring-bg" />
              <circle cx="50" cy="50" r="42" class="ring-fg"
                :stroke-dasharray="`${(analysisData.score ?? 0) * 2.639} 263.9`" />
            </svg>
            <div class="score-num">{{ analysisData.score ?? '--' }}</div>
          </div>
          <div class="score-text">
            <div class="score-label">综合评分</div>
            <div class="score-summary">{{ analysisData.summary }}</div>
          </div>
        </div>

        <!-- 亮点 & 问题 & 建议 三列 -->
        <div class="analysis-grid">
          <div class="analysis-col analysis-col--green">
            <div class="analysis-col__title">饮食亮点</div>
            <ul class="analysis-list">
              <li v-for="(item, i) in analysisData.strengths" :key="i">{{ item }}</li>
            </ul>
            <div v-if="!analysisData.strengths?.length" class="analysis-empty">暂无</div>
          </div>
          <div class="analysis-col analysis-col--red">
            <div class="analysis-col__title">潜在问题</div>
            <ul class="analysis-list">
              <li v-for="(item, i) in analysisData.issues" :key="i">{{ item }}</li>
            </ul>
            <div v-if="!analysisData.issues?.length" class="analysis-empty">暂无</div>
          </div>
          <div class="analysis-col analysis-col--blue">
            <div class="analysis-col__title">改进建议</div>
            <ul class="analysis-list">
              <li v-for="(item, i) in analysisData.advice" :key="i">{{ item }}</li>
            </ul>
            <div v-if="!analysisData.advice?.length" class="analysis-empty">暂无</div>
          </div>
        </div>
      </div>

      <!-- 空态 -->
      <div v-else-if="!analysisLoading" class="empty-state">
        <div class="empty-state__icon">&#129302;</div>
        <div class="empty-state__text">选择日期范围后点击"开始分析"，AI 将为你的饮食做全面评估</div>
      </div>
    </div>

    <!-- ============================================================ -->
    <!-- Tab 2: 个性化建议                                             -->
    <!-- ============================================================ -->
    <div v-if="activeTab === 'recommend'" class="tab-panel">
      <div class="panel-controls">
        <el-button type="primary" :loading="recommendLoading" @click="runRecommend">
          <el-icon><MagicStick /></el-icon>获取 AI 建议
        </el-button>
        <span class="panel-hint">基于近 7 天饮食数据生成</span>
      </div>

      <div v-if="recommendResult" class="recommend-result" v-loading="recommendLoading">

        <!-- 重点关注 -->
        <div class="focus-card">
          <el-icon class="focus-card__icon"><ChatDotRound /></el-icon>
          <div class="focus-card__text">{{ recommendData.focus }}</div>
        </div>

        <!-- 具体建议列表 -->
        <div v-if="recommendData.recommendations?.length" class="rec-list">
          <div v-for="(rec, i) in recommendData.recommendations" :key="i" class="rec-item">
            <div class="rec-item__idx">{{ i + 1 }}</div>
            <div class="rec-item__body">
              <div class="rec-item__title">{{ rec.title }}</div>
              <div class="rec-item__reason">{{ rec.reason }}</div>
              <div class="rec-item__action">
                <el-icon><Right /></el-icon>{{ rec.action }}
              </div>
            </div>
          </div>
        </div>

        <!-- 建议增加 / 减少的食物 -->
        <div class="food-tags-row">
          <div class="food-tags-group" v-if="recommendData.foods_to_add?.length">
            <span class="food-tags-label food-tags-label--green">建议增加</span>
            <el-tag v-for="f in recommendData.foods_to_add" :key="f" type="success" effect="light" round>{{ f }}</el-tag>
          </div>
          <div class="food-tags-group" v-if="recommendData.foods_to_reduce?.length">
            <span class="food-tags-label food-tags-label--red">建议减少</span>
            <el-tag v-for="f in recommendData.foods_to_reduce" :key="f" type="danger" effect="light" round>{{ f }}</el-tag>
          </div>
        </div>
      </div>

      <div v-else-if="!recommendLoading" class="empty-state">
        <div class="empty-state__icon">&#128161;</div>
        <div class="empty-state__text">点击"获取 AI 建议"，获取基于你近期饮食的个性化改进方案</div>
      </div>
    </div>

    <!-- ============================================================ -->
    <!-- Tab 3: 趋势预测                                               -->
    <!-- ============================================================ -->
    <div v-if="activeTab === 'predict'" class="tab-panel">
      <div class="panel-controls">
        <el-select v-model="predictNutrient" size="default" style="width: 160px">
          <el-option v-for="n in nutrientOptions" :key="n.value" :label="n.label" :value="n.value" />
        </el-select>
        <el-select v-model="predictDays" size="default" style="width: 120px">
          <el-option :value="7" label="预测 7 天" />
          <el-option :value="14" label="预测 14 天" />
          <el-option :value="30" label="预测 30 天" />
        </el-select>
        <el-button type="primary" :loading="predictLoading" @click="runPredict">
          <el-icon><TrendCharts /></el-icon>开始预测
        </el-button>
      </div>

      <div v-if="predictResult" class="predict-result" v-loading="predictLoading">
        <!-- 解读 -->
        <div class="interpret-card">
          <el-icon class="interpret-card__icon"><ChatDotRound /></el-icon>
          <div class="interpret-card__text">{{ predictResult.interpretation }}</div>
        </div>

        <!-- 预测折线图 -->
        <div class="chart-card span-full">
          <div class="chart-card__header">
            <span class="chart-card__title">{{ nutrientLabel }} · 未来 {{ predictDays }} 天预测</span>
            <span class="chart-card__sub">虚线部分为预测值，基于近期摄入趋势推算</span>
          </div>
          <div ref="predictChartEl" class="chart-area chart-area--tall" />
        </div>
      </div>

      <div v-else-if="!predictLoading" class="empty-state">
        <div class="empty-state__icon">&#128200;</div>
        <div class="empty-state__text">选择营养素和预测天数，AI 将基于历史数据推算未来趋势</div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { MagicStick, ChatDotRound, Right, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { analyzeDiet, getRecommendation, predictTrend } from '@/api/ai'
import { getDashboard } from '@/api/stats'
import dayjs from 'dayjs'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, MarkLineComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, MarkLineComponent, CanvasRenderer])

// ===== Tab 控制 =====
const activeTab = ref('analysis')

// ===== 综合分析 =====
const analysisRange   = ref([dayjs().subtract(6, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])
const analysisLoading = ref(false)
const analysisResult  = ref(null)

const analysisData = computed(() => analysisResult.value?.analysis ?? {})
const scoreClass   = computed(() => {
  const s = analysisData.value.score ?? 0
  if (s >= 80) return 'score-ring--green'
  if (s >= 60) return 'score-ring--yellow'
  return 'score-ring--red'
})

async function runAnalysis() {
  const [start, end] = analysisRange.value
  if (!start || !end) {
    ElMessage.warning('请选择日期范围')
    return
  }
  analysisLoading.value = true
  try {
    const res = await analyzeDiet(start, end)
    analysisResult.value = res.data
  } catch {
    ElMessage.error('分析请求失败，请稍后重试')
  } finally {
    analysisLoading.value = false
  }
}

// ===== 个性化建议 =====
const recommendLoading = ref(false)
const recommendResult  = ref(null)
const recommendData    = computed(() => recommendResult.value?.recommendation ?? {})

async function runRecommend() {
  recommendLoading.value = true
  try {
    const res = await getRecommendation()
    recommendResult.value = res.data
  } catch {
    ElMessage.error('获取建议失败，请稍后重试')
  } finally {
    recommendLoading.value = false
  }
}

// ===== 趋势预测 =====
const nutrientOptions = [
  { value: 'energyKcal', label: '热量 (kcal)' },
  { value: 'protein',    label: '蛋白质 (g)' },
  { value: 'fat',        label: '脂肪 (g)' },
  { value: 'carbohydrate', label: '碳水化合物 (g)' },
  { value: 'fiber',      label: '膳食纤维 (g)' },
  { value: 'vitaminC',   label: '维生素 C (mg)' },
  { value: 'calcium',    label: '钙 (mg)' },
  { value: 'iron',       label: '铁 (mg)' },
]

const predictNutrient = ref('energyKcal')
const predictDays     = ref(7)
const predictLoading  = ref(false)
const predictResult   = ref(null)
const predictChartEl  = ref(null)

const nutrientLabel = computed(() =>
  nutrientOptions.find(n => n.value === predictNutrient.value)?.label ?? predictNutrient.value
)

let predictChart = null

async function runPredict() {
  predictLoading.value = true
  try {
    // 并行：获取历史 dashboard + 预测数据
    const historyDays = Math.max(14, predictDays.value * 2)
    const [predictRes, dashRes] = await Promise.all([
      predictTrend(predictNutrient.value, predictDays.value),
      getDashboard(dayjs().format('YYYY-MM-DD'), historyDays),
    ])
    predictResult.value = predictRes.data

    // 构建图表
    nextTick(() => renderPredictChart(dashRes.data, predictRes.data))
  } catch {
    ElMessage.error('预测请求失败，请稍后重试')
  } finally {
    predictLoading.value = false
  }
}

function renderPredictChart(dashboard, predict) {
  if (!predictChartEl.value) return

  predictChart?.dispose()
  predictChart = echarts.init(predictChartEl.value, null, { renderer: 'canvas' })

  const trend    = dashboard?.trend ?? []
  const forecast = predict?.forecast ?? []
  const field    = predictNutrient.value

  // 历史数据
  const histDates  = trend.map(d => dayjs(d.date).format('M/D'))
  const histValues = trend.map(d => Number(d[field] ?? d.energyKcal ?? 0).toFixed(1))

  // 预测数据（前面补 null 占位，最后一个历史点作为连接点）
  const predDates  = forecast.map(f => dayjs(f.date).format('M/D'))
  const allDates   = [...histDates, ...predDates]
  const nullPad    = new Array(histDates.length - 1).fill(null)
  const predValues = [...nullPad, histValues[histValues.length - 1], ...forecast.map(f => Number(f.value).toFixed(1))]
  const histPadded = [...histValues, ...new Array(predDates.length).fill(null)]

  const option = {
    animation: true, animationDuration: 900, animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff', borderColor: '#E5E7EB', borderWidth: 1,
      padding: [8, 12], textStyle: { color: '#111827', fontSize: 13 },
    },
    legend: {
      data: ['历史摄入', '预测趋势'],
      top: 0, right: 0,
      textStyle: { color: '#6B7280', fontSize: 12 },
    },
    grid: { top: 36, right: 20, bottom: 36, left: 60 },
    xAxis: {
      type: 'category', data: allDates,
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisTick: { show: false },
      axisLabel: { color: '#9CA3AF', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F3F4F6', type: 'dashed' } },
      axisLabel: { color: '#9CA3AF', fontSize: 11 },
    },
    series: [
      {
        name: '历史摄入', type: 'line', smooth: true,
        symbol: 'circle', symbolSize: 5,
        data: histPadded,
        lineStyle: { color: '#4F46E5', width: 2.5 },
        itemStyle: { color: '#4F46E5', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79,70,229,0.18)' },
            { offset: 1, color: 'rgba(79,70,229,0.01)' },
          ]),
        },
      },
      {
        name: '预测趋势', type: 'line', smooth: true,
        symbol: 'emptyCircle', symbolSize: 6,
        data: predValues,
        lineStyle: { color: '#F59E0B', width: 2.5, type: 'dashed' },
        itemStyle: { color: '#F59E0B', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245,158,11,0.12)' },
            { offset: 1, color: 'rgba(245,158,11,0.01)' },
          ]),
        },
      },
    ],
  }

  predictChart.setOption(option)
}

// ===== Resize =====
let resizeOb
watch(predictChartEl, (el) => {
  if (el) {
    resizeOb = new ResizeObserver(() => predictChart?.resize())
    resizeOb.observe(el.parentElement)
  }
})

onUnmounted(() => {
  resizeOb?.disconnect()
  predictChart?.dispose()
})
</script>

<style lang="scss" scoped>
.ai-page {
  padding: $space-5 $space-6;
  display: flex;
  flex-direction: column;
  gap: $space-5;
}

// ===== Header =====
.ai-header {
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
}

// ===== Tabs =====
.ai-tabs {
  align-self: flex-start;
}

// ===== Panel controls =====
.panel-controls {
  display: flex;
  align-items: center;
  gap: $space-3;
  flex-wrap: wrap;
}

.panel-hint {
  font-size: $font-size-xs;
  color: $color-text-tertiary;
}

// ===== Tab panel =====
.tab-panel {
  display: flex;
  flex-direction: column;
  gap: $space-5;
}

// ===== Empty state =====
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $space-12 $space-6;
  gap: $space-4;

  &__icon {
    font-size: 48px;
    opacity: 0.6;
  }

  &__text {
    font-size: $font-size-sm;
    color: $color-text-tertiary;
    text-align: center;
    max-width: 400px;
  }
}

// ============================================================
// 综合分析
// ============================================================

.score-card {
  display: flex;
  align-items: center;
  gap: $space-6;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-xl;
  padding: $space-6;
}

.score-ring {
  position: relative;
  width: 100px;
  height: 100px;
  flex-shrink: 0;

  svg {
    width: 100%;
    height: 100%;
    transform: rotate(-90deg);
  }

  .ring-bg {
    fill: none;
    stroke: $color-bg-secondary;
    stroke-width: 8;
  }
  .ring-fg {
    fill: none;
    stroke-width: 8;
    stroke-linecap: round;
    transition: stroke-dasharray 1s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &--green .ring-fg { stroke: #10B981; }
  &--yellow .ring-fg { stroke: #F59E0B; }
  &--red .ring-fg { stroke: #EF4444; }
}

.score-num {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  font-family: 'JetBrains Mono', monospace;
  color: $color-text-primary;
}

.score-text {
  flex: 1;
}
.score-label {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  font-weight: $font-weight-medium;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: $space-2;
}
.score-summary {
  font-size: $font-size-base;
  color: $color-text-primary;
  line-height: 1.6;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $space-4;
}

.analysis-col {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-xl;
  padding: $space-5;
  border-top: 3px solid transparent;

  &--green { border-top-color: #10B981; }
  &--red   { border-top-color: #EF4444; }
  &--blue  { border-top-color: #3B82F6; }

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin-bottom: $space-3;
  }
}

.analysis-list {
  margin: 0;
  padding-left: $space-5;

  li {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    line-height: 1.8;
  }
}

.analysis-empty {
  font-size: $font-size-sm;
  color: $color-text-disabled;
}

// ============================================================
// 个性化建议
// ============================================================

.focus-card {
  display: flex;
  align-items: flex-start;
  gap: $space-3;
  background: #EEF2FF;
  border: 1px solid #C7D2FE;
  border-radius: $radius-xl;
  padding: $space-5;

  &__icon {
    font-size: 22px;
    color: $color-accent;
    flex-shrink: 0;
    margin-top: 2px;
  }

  &__text {
    font-size: $font-size-base;
    color: #312E81;
    line-height: 1.6;
    font-weight: $font-weight-medium;
  }
}

.rec-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.rec-item {
  display: flex;
  gap: $space-4;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-xl;
  padding: $space-5;
  transition: $transition-base;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-1px);
  }

  &__idx {
    width: 32px;
    height: 32px;
    border-radius: $radius-full;
    background: $color-accent;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-size-sm;
    font-weight: $font-weight-bold;
    flex-shrink: 0;
  }

  &__body { flex: 1; }

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin-bottom: $space-1;
  }

  &__reason {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin-bottom: $space-2;
  }

  &__action {
    display: flex;
    align-items: center;
    gap: $space-1;
    font-size: $font-size-sm;
    color: $color-accent;
    font-weight: $font-weight-medium;
  }
}

.food-tags-row {
  display: flex;
  gap: $space-6;
  flex-wrap: wrap;
}

.food-tags-group {
  display: flex;
  align-items: center;
  gap: $space-2;
  flex-wrap: wrap;
}

.food-tags-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  padding: 2px $space-2;
  border-radius: $radius-sm;

  &--green { background: $color-success-light; color: darken(#10B981, 15%); }
  &--red   { background: $color-danger-light;  color: darken(#EF4444, 10%); }
}

// ============================================================
// 趋势预测
// ============================================================

.interpret-card {
  display: flex;
  align-items: flex-start;
  gap: $space-3;
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  border-radius: $radius-xl;
  padding: $space-5;

  &__icon {
    font-size: 22px;
    color: #D97706;
    flex-shrink: 0;
    margin-top: 2px;
  }

  &__text {
    font-size: $font-size-base;
    color: #78350F;
    line-height: 1.6;
  }
}

// ===== Shared chart styles (same as Dashboard) =====
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
  &--tall { height: 320px; }
}
</style>
