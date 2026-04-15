<template>
  <div class="food-library">
    <!-- ===== Left: Category Sidebar ===== -->
    <aside class="category-sidebar">
      <div class="category-sidebar__header">
        <span class="category-sidebar__title">食物分类</span>
      </div>

      <div class="category-tree" v-loading="categoryLoading">
        <!-- All foods entry -->
        <div
          class="category-item category-item--root"
          :class="{ 'is-active': selectedCategoryId === null }"
          @click="selectCategory(null)"
        >
          <el-icon class="category-item__icon"><Grid /></el-icon>
          <span>全部食物</span>
          <span class="category-item__count">{{ totalFoods }}</span>
        </div>

        <!-- Major categories -->
        <template v-for="cat in categories" :key="cat.id">
          <div
            class="category-item category-item--major"
            :class="{ 'is-expanded': expandedCategories.has(cat.id) }"
            @click="toggleCategory(cat)"
          >
            <span class="category-item__emoji">{{ cat.icon || '🍽️' }}</span>
            <span class="category-item__name">{{ cat.nameZh }}</span>
            <el-icon class="category-item__arrow">
              <ArrowDown v-if="expandedCategories.has(cat.id)" />
              <ArrowRight v-else />
            </el-icon>
          </div>

          <!-- Sub-categories -->
          <transition name="submenu">
            <div v-if="expandedCategories.has(cat.id)" class="subcategory-list">
              <div
                v-for="sub in cat.children"
                :key="sub.id"
                class="category-item category-item--sub"
                :class="{ 'is-active': selectedCategoryId === sub.id }"
                @click="selectCategory(sub.id)"
              >
                {{ sub.nameZh }}
              </div>
            </div>
          </transition>
        </template>
      </div>
    </aside>

    <!-- ===== Main: Search + Food Grid ===== -->
    <main class="food-main">
      <!-- Search Bar -->
      <div class="search-bar">
        <el-autocomplete
          v-model="searchInput"
          class="search-bar__input"
          placeholder="搜索食物名称（如：鸡胸肉、燕麦、西兰花…）"
          :fetch-suggestions="fetchSuggestions"
          :debounce="300"
          clearable
          @select="handleSuggestionSelect"
          @clear="handleClear"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #default="{ item }">
            <div class="suggestion-item">
              <span class="suggestion-item__name">{{ item.nameZh }}</span>
              <span class="suggestion-item__meta">
                {{ item.categoryNameZh }} · {{ item.energyKcal ?? '--' }} kcal
              </span>
            </div>
          </template>
        </el-autocomplete>

        <el-button class="search-bar__btn" type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>

        <!-- Sort -->
        <el-select
          v-model="sortBy"
          placeholder="排序"
          class="search-bar__sort"
          @change="handleSortChange"
        >
          <el-option label="默认排序" value="" />
          <el-option label="能量 ↑" value="energy_kcal" />
          <el-option label="蛋白质 ↑" value="protein" />
          <el-option label="脂肪 ↑" value="fat" />
          <el-option label="碳水 ↑" value="carbohydrate" />
        </el-select>
      </div>

      <!-- Breadcrumb / active filter info -->
      <div class="filter-bar" v-if="activeFilterLabel">
        <el-tag closable type="info" @close="clearFilter">
          {{ activeFilterLabel }}
        </el-tag>
        <span class="filter-bar__total">共 {{ totalFoods }} 条结果</span>
      </div>
      <div class="filter-bar" v-else>
        <span class="filter-bar__total">共 {{ totalFoods }} 种食物</span>
      </div>

      <!-- Food Cards Grid -->
      <div v-loading="loading" class="food-grid" :class="{ 'food-grid--empty': !loading && foods.length === 0 }">
        <template v-if="!loading && foods.length === 0">
          <div class="empty-state">
            <el-icon class="empty-state__icon"><Grid /></el-icon>
            <p>未找到相关食物</p>
            <el-button text @click="clearFilter">清除筛选</el-button>
          </div>
        </template>

        <div
          v-for="food in foods"
          :key="food.id"
          class="food-card"
          @click="openDetail(food.id)"
        >
          <div class="food-card__header">
            <span class="food-card__category">{{ food.categoryNameZh }}</span>
            <el-tag size="small" :type="sourceTagType(food.dataSource)" class="food-card__source">
              {{ food.dataSource }}
            </el-tag>
          </div>

          <div class="food-card__name">{{ food.nameZh }}</div>
          <div class="food-card__name-en">{{ food.nameEn }}</div>

          <!-- Nutrient mini bars -->
          <div class="food-card__nutrients">
            <div class="nutrient-row">
              <span class="nutrient-row__label">能量</span>
              <span class="nutrient-row__value energy">{{ fmt(food.energyKcal) }} kcal</span>
            </div>
            <div class="nutrient-row">
              <span class="nutrient-row__label">蛋白质</span>
              <div class="nutrient-row__bar-wrap">
                <div class="nutrient-row__bar nutrient-row__bar--protein" :style="{ width: pct(food.protein, 30) }" />
              </div>
              <span class="nutrient-row__value">{{ fmt(food.protein) }}g</span>
            </div>
            <div class="nutrient-row">
              <span class="nutrient-row__label">脂肪</span>
              <div class="nutrient-row__bar-wrap">
                <div class="nutrient-row__bar nutrient-row__bar--fat" :style="{ width: pct(food.fat, 30) }" />
              </div>
              <span class="nutrient-row__value">{{ fmt(food.fat) }}g</span>
            </div>
            <div class="nutrient-row">
              <span class="nutrient-row__label">碳水</span>
              <div class="nutrient-row__bar-wrap">
                <div class="nutrient-row__bar nutrient-row__bar--carb" :style="{ width: pct(food.carbohydrate, 80) }" />
              </div>
              <span class="nutrient-row__value">{{ fmt(food.carbohydrate) }}g</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="totalFoods > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalFoods"
          :page-sizes="[20, 40, 60]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </main>

    <!-- ===== Food Detail Drawer ===== -->
    <el-drawer
      v-model="drawerVisible"
      :title="currentFood?.nameZh"
      direction="rtl"
      size="480px"
      class="food-detail-drawer"
      destroy-on-close
    >
      <template v-if="currentFood">
        <!-- Header info -->
        <div class="detail-meta">
          <el-tag type="info">{{ currentFood.categoryNameZh }}</el-tag>
          <el-tag :type="sourceTagType(currentFood.dataSource)">{{ currentFood.dataSource }}</el-tag>
        </div>
        <p class="detail-name-en">{{ currentFood.nameEn }}</p>
        <p v-if="currentFood.alias" class="detail-alias">别名：{{ currentFood.alias }}</p>

        <!-- Key nutrient cards (per 100g) -->
        <div class="detail-section-title">核心营养（每 100g）</div>
        <div class="key-nutrients">
          <div class="key-nutrient" v-for="n in keyNutrients" :key="n.label">
            <div class="key-nutrient__value" :style="{ color: n.color }">
              {{ currentFood.nutrition ? fmt(currentFood.nutrition[n.field]) : '--' }}
              <span class="key-nutrient__unit">{{ n.unit }}</span>
            </div>
            <div class="key-nutrient__label">{{ n.label }}</div>
          </div>
        </div>

        <!-- Radar chart -->
        <div class="detail-section-title">营养雷达图</div>
        <div ref="radarChartEl" class="radar-chart" />

        <!-- Full nutrition table -->
        <div class="detail-section-title">完整营养成分表</div>
        <el-table :data="nutritionTableData" size="small" :show-header="false" class="nutrition-table">
          <el-table-column prop="label" width="140" />
          <el-table-column prop="value" align="right">
            <template #default="{ row }">
              <span :class="{ 'text-missing': row.value === null }">
                {{ row.value !== null ? `${fmt(row.value)} ${row.unit}` : '—' }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <div v-else v-loading="detailLoading" class="drawer-loading" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useFoodStore } from '@/stores/foodStore'
import { storeToRefs } from 'pinia'
import * as echarts from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, RadarComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { Grid, ArrowDown, ArrowRight, Search } from '@element-plus/icons-vue'

echarts.use([RadarChart, TooltipComponent, LegendComponent, RadarComponent, CanvasRenderer])

const store = useFoodStore()
const {
  foods, totalFoods, currentPage, pageSize, totalPages,
  categories, selectedCategoryId, searchKeyword, currentFood, loading,
} = storeToRefs(store)

// ---- UI state ----
const searchInput = ref('')
const sortBy = ref('')
const expandedCategories = ref(new Set())
const drawerVisible = ref(false)
const detailLoading = ref(false)
const categoryLoading = ref(false)
const radarChartEl = ref(null)
let radarChart = null

// ---- Computed ----
const activeFilterLabel = computed(() => {
  if (searchKeyword.value) return `搜索：${searchKeyword.value}`
  if (selectedCategoryId.value) {
    for (const cat of categories.value) {
      const sub = cat.children?.find(s => s.id === selectedCategoryId.value)
      if (sub) return `${cat.nameZh} > ${sub.nameZh}`
    }
  }
  return null
})

// ---- Category actions ----
function toggleCategory(cat) {
  if (expandedCategories.value.has(cat.id)) {
    expandedCategories.value.delete(cat.id)
  } else {
    expandedCategories.value.add(cat.id)
  }
  // trigger reactivity
  expandedCategories.value = new Set(expandedCategories.value)
}

function selectCategory(categoryId) {
  searchInput.value = ''
  store.searchKeyword = ''
  sortBy.value = ''
  store.setCategory(categoryId)
}

// ---- Search actions ----
async function fetchSuggestions(queryStr, cb) {
  if (!queryStr?.trim()) { cb([]); return }
  await store.fetchSuggestions(queryStr)
  cb(store.suggestions.map(s => ({ ...s, value: s.nameZh })))
}

function handleSuggestionSelect(item) {
  searchInput.value = item.nameZh
  store.setKeyword(item.nameZh)
}

function handleSearch() {
  store.setKeyword(searchInput.value.trim())
}

function handleClear() {
  store.setKeyword('')
}

function clearFilter() {
  searchInput.value = ''
  sortBy.value = ''
  store.reset()
  store.fetchFoods()
}

function handleSortChange() {
  store.fetchFoods({ sortBy: sortBy.value })
}

function handlePageChange(page) {
  store.setPage(page)
}

function handleSizeChange(size) {
  store.pageSize = size
  store.setPage(1)
}

// ---- Food detail ----
async function openDetail(id) {
  drawerVisible.value = true
  detailLoading.value = true
  currentFood.value = null
  try {
    await store.fetchFoodById(id)
  } finally {
    detailLoading.value = false
  }
  await nextTick()
  renderRadarChart()
}

// ---- Helpers ----
function fmt(val) {
  if (val === null || val === undefined) return '--'
  const n = parseFloat(val)
  return isNaN(n) ? '--' : n % 1 === 0 ? n.toString() : n.toFixed(1)
}

function pct(val, max) {
  if (!val) return '0%'
  return Math.min(100, (parseFloat(val) / max) * 100).toFixed(1) + '%'
}

function sourceTagType(source) {
  const map = { CNF: 'success', USDA: 'primary', CUSTOM: 'warning' }
  return map[source] || 'info'
}

// ---- Key nutrients config ----
const keyNutrients = [
  { label: '能量', field: 'energyKcal', unit: 'kcal', color: '#F59E0B' },
  { label: '蛋白质', field: 'protein',  unit: 'g',    color: '#10B981' },
  { label: '脂肪',   field: 'fat',      unit: 'g',    color: '#EF4444' },
  { label: '碳水',   field: 'carbohydrate', unit: 'g', color: '#3B82F6' },
  { label: '膳食纤维', field: 'fiber',  unit: 'g',    color: '#8B5CF6' },
]

// ---- Full nutrition table rows ----
const nutritionTableData = computed(() => {
  const n = currentFood.value?.nutrition
  if (!n) return []
  return [
    { label: '能量', value: n.energyKcal, unit: 'kcal' },
    { label: '能量(kJ)', value: n.energyKj, unit: 'kJ' },
    { label: '水分', value: n.water, unit: 'g' },
    { label: '蛋白质', value: n.protein, unit: 'g' },
    { label: '脂肪', value: n.fat, unit: 'g' },
    { label: '— 饱和脂肪', value: n.saturatedFat, unit: 'g' },
    { label: '— 单不饱和', value: n.monounsaturatedFat, unit: 'g' },
    { label: '— 多不饱和', value: n.polyunsaturatedFat, unit: 'g' },
    { label: '— 反式脂肪', value: n.transFat, unit: 'g' },
    { label: '胆固醇', value: n.cholesterol, unit: 'mg' },
    { label: '碳水化合物', value: n.carbohydrate, unit: 'g' },
    { label: '— 膳食纤维', value: n.fiber, unit: 'g' },
    { label: '— 糖', value: n.sugar, unit: 'g' },
    { label: '— 淀粉', value: n.starch, unit: 'g' },
    { label: '灰分', value: n.ash, unit: 'g' },
    { label: '钙', value: n.calcium, unit: 'mg' },
    { label: '铁', value: n.iron, unit: 'mg' },
    { label: '镁', value: n.magnesium, unit: 'mg' },
    { label: '磷', value: n.phosphorus, unit: 'mg' },
    { label: '钾', value: n.potassium, unit: 'mg' },
    { label: '钠', value: n.sodium, unit: 'mg' },
    { label: '锌', value: n.zinc, unit: 'mg' },
    { label: '铜', value: n.copper, unit: 'mg' },
    { label: '锰', value: n.manganese, unit: 'mg' },
    { label: '硒', value: n.selenium, unit: 'μg' },
    { label: '碘', value: n.iodine, unit: 'μg' },
    { label: '维生素A', value: n.vitaminA, unit: 'μg RAE' },
    { label: 'β-胡萝卜素', value: n.betaCarotene, unit: 'μg' },
    { label: '维生素D', value: n.vitaminD, unit: 'μg' },
    { label: '维生素E', value: n.vitaminE, unit: 'mg' },
    { label: '维生素K', value: n.vitaminK, unit: 'μg' },
    { label: '维生素C', value: n.vitaminC, unit: 'mg' },
    { label: '维生素B1', value: n.vitaminB1, unit: 'mg' },
    { label: '维生素B2', value: n.vitaminB2, unit: 'mg' },
    { label: '维生素B3', value: n.vitaminB3, unit: 'mg' },
    { label: '维生素B5', value: n.vitaminB5, unit: 'mg' },
    { label: '维生素B6', value: n.vitaminB6, unit: 'mg' },
    { label: '维生素B12', value: n.vitaminB12, unit: 'μg' },
    { label: '叶酸', value: n.folate, unit: 'μg DFE' },
    { label: '生物素', value: n.biotin, unit: 'μg' },
  ]
})

// ---- Radar chart ----
// Daily reference values for radar
const radarDRV = {
  energyKcal: 2000, protein: 60, fat: 65, carbohydrate: 300, fiber: 25,
  vitaminC: 100, calcium: 800, iron: 15,
}
const radarIndicators = [
  { name: '能量', field: 'energyKcal', max: radarDRV.energyKcal },
  { name: '蛋白质', field: 'protein', max: radarDRV.protein },
  { name: '脂肪', field: 'fat', max: radarDRV.fat },
  { name: '碳水', field: 'carbohydrate', max: radarDRV.carbohydrate },
  { name: '纤维', field: 'fiber', max: radarDRV.fiber },
  { name: '维C', field: 'vitaminC', max: radarDRV.vitaminC },
  { name: '钙', field: 'calcium', max: radarDRV.calcium },
  { name: '铁', field: 'iron', max: radarDRV.iron },
]

function renderRadarChart() {
  if (!radarChartEl.value || !currentFood.value?.nutrition) return
  if (radarChart) radarChart.dispose()
  radarChart = echarts.init(radarChartEl.value)

  const n = currentFood.value.nutrition
  const values = radarIndicators.map(ind => {
    const val = parseFloat(n[ind.field]) || 0
    return Math.min(val, ind.max)
  })

  radarChart.setOption({
    tooltip: { trigger: 'item' },
    radar: {
      indicator: radarIndicators.map(ind => ({ name: ind.name, max: ind.max })),
      shape: 'polygon',
      splitNumber: 4,
      axisName: { color: '#6B7280', fontSize: 11 },
      splitLine: { lineStyle: { color: '#E5E7EB' } },
      splitArea: { areaStyle: { color: ['rgba(79,70,229,0.02)', 'rgba(79,70,229,0.04)'] } },
    },
    series: [{
      type: 'radar',
      data: [{ value: values, name: '每100g' }],
      areaStyle: { color: 'rgba(79,70,229,0.15)' },
      lineStyle: { color: '#4F46E5', width: 2 },
      itemStyle: { color: '#4F46E5' },
    }],
  })
}

watch(drawerVisible, (val) => {
  if (!val && radarChart) {
    radarChart.dispose()
    radarChart = null
  }
})

// ---- Init ----
onMounted(async () => {
  categoryLoading.value = true
  await store.fetchCategories()
  categoryLoading.value = false
  await store.fetchFoods()
})
</script>

<style lang="scss" scoped>
.food-library {
  display: flex;
  height: 100%;
  gap: 0;
}

// ===== Category Sidebar =====
.category-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: $color-surface;
  border-right: 1px solid $color-border;
  display: flex;
  flex-direction: column;
  overflow-y: auto;

  &__header {
    padding: $space-4 $space-4 $space-3;
    border-bottom: 1px solid $color-border-light;
  }

  &__title {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }
}

.category-tree {
  padding: $space-2;
}

.category-item {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: 7px $space-3;
  border-radius: $radius-base;
  cursor: pointer;
  font-size: $font-size-base;
  color: $color-text-secondary;
  transition: $transition-fast;

  &:hover { background: $color-bg-secondary; color: $color-text-primary; }

  &.is-active {
    background: $color-accent-light;
    color: $color-accent;
    font-weight: $font-weight-medium;
  }

  &--root {
    margin-bottom: $space-1;
    font-weight: $font-weight-medium;
  }

  &--major {
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &--sub {
    padding-left: 28px;
    font-size: $font-size-sm;
  }

  &__icon { font-size: 15px; }
  &__emoji { font-size: 16px; flex-shrink: 0; }
  &__name { flex: 1; }
  &__arrow { margin-left: auto; font-size: 12px; color: $color-text-tertiary; }
  &__count {
    margin-left: auto;
    font-size: $font-size-xs;
    color: $color-text-disabled;
    background: $color-bg-secondary;
    padding: 1px 6px;
    border-radius: $radius-full;
  }
}

.subcategory-list { overflow: hidden; }

// submenu transition
.submenu-enter-active,
.submenu-leave-active { transition: all 0.22s ease; max-height: 400px; }
.submenu-enter-from,
.submenu-leave-to { max-height: 0; opacity: 0; }

// ===== Main area =====
.food-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: $color-bg-primary;
  padding: $space-5 $space-6;
  gap: $space-4;
}

// Search bar
.search-bar {
  display: flex;
  gap: $space-3;
  align-items: center;

  &__input { flex: 1; }

  &__btn {
    flex-shrink: 0;
    :deep(.el-icon) { margin-right: $space-1; }
  }

  &__sort { width: 130px; flex-shrink: 0; }
}

// Filter bar
.filter-bar {
  display: flex;
  align-items: center;
  gap: $space-3;
  min-height: 24px;

  &__total { font-size: $font-size-sm; color: $color-text-secondary; }
}

// Food grid
.food-grid {
  flex: 1;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: $space-4;
  align-content: start;

  &--empty { display: flex; align-items: center; justify-content: center; }
}

.food-card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
  padding: $space-4;
  cursor: pointer;
  transition: $transition-base;

  &:hover {
    border-color: $color-accent;
    box-shadow: 0 4px 16px rgba(79, 70, 229, 0.1);
    transform: translateY(-2px);
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $space-2;
  }

  &__category {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    background: $color-bg-secondary;
    padding: 2px $space-2;
    border-radius: $radius-sm;
  }

  &__source { flex-shrink: 0; }

  &__name {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin-bottom: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__name-en {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    margin-bottom: $space-3;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__nutrients { display: flex; flex-direction: column; gap: 5px; }
}

.nutrient-row {
  display: flex;
  align-items: center;
  gap: $space-2;
  font-size: $font-size-xs;

  &__label { width: 38px; color: $color-text-secondary; flex-shrink: 0; }

  &__bar-wrap {
    flex: 1;
    height: 4px;
    background: $color-bg-secondary;
    border-radius: $radius-full;
    overflow: hidden;
  }

  &__bar {
    height: 100%;
    border-radius: $radius-full;
    transition: width 0.4s ease;

    &--protein { background: #10B981; }
    &--fat     { background: #EF4444; }
    &--carb    { background: #3B82F6; }
  }

  &__value {
    width: 52px;
    text-align: right;
    color: $color-text-primary;
    font-weight: $font-weight-medium;
    flex-shrink: 0;

    &.energy { flex: 1; text-align: right; color: $color-warning; font-weight: $font-weight-semibold; }
  }
}

// Empty state
.empty-state {
  text-align: center;
  color: $color-text-tertiary;
  padding: 60px 0;

  &__icon { font-size: 48px; margin-bottom: $space-3; }
  p { margin-bottom: $space-4; }
}

// Pagination
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: $space-3 0;
  flex-shrink: 0;
}

// ===== Detail Drawer =====
.food-detail-drawer {
  :deep(.el-drawer__header) {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    padding: $space-5;
    border-bottom: 1px solid $color-border;
    margin-bottom: 0;
  }
  :deep(.el-drawer__body) { padding: $space-5; overflow-y: auto; }
}

.detail-meta {
  display: flex;
  gap: $space-2;
  margin-bottom: $space-2;
}

.detail-name-en {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-bottom: $space-1;
}

.detail-alias {
  font-size: $font-size-xs;
  color: $color-text-tertiary;
  margin-bottom: $space-4;
}

.detail-section-title {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $color-text-secondary;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: $space-5 0 $space-3;
  padding-bottom: $space-2;
  border-bottom: 1px solid $color-border-light;
}

.key-nutrients {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: $space-3;
}

.key-nutrient {
  background: $color-bg-primary;
  border-radius: $radius-base;
  padding: $space-3 $space-2;
  text-align: center;

  &__value {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    line-height: 1.2;
  }

  &__unit {
    font-size: $font-size-xs;
    font-weight: $font-weight-normal;
  }

  &__label {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-top: 4px;
  }
}

.radar-chart {
  width: 100%;
  height: 260px;
}

.nutrition-table {
  width: 100%;
  .text-missing { color: $color-text-disabled; }
}

.drawer-loading { height: 200px; }

// Suggestion dropdown
.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: $space-2;

  &__name { font-weight: $font-weight-medium; }

  &__meta {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    flex-shrink: 0;
  }
}
</style>
