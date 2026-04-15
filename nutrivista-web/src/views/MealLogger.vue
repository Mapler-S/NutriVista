<template>
  <div class="meal-logger">

    <!-- ===== Date Navigation ===== -->
    <div class="date-nav">
      <el-button text :disabled="dialogVisible" @click="shiftDay(-1)"><el-icon><ArrowLeft /></el-icon></el-button>
      <el-date-picker
        v-model="selectedDate"
        type="date"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        :clearable="false"
        :disabled="dialogVisible"
        class="date-nav__picker"
        @change="store.setDate($event)"
      />
      <el-button text :disabled="dialogVisible" @click="shiftDay(1)"><el-icon><ArrowRight /></el-icon></el-button>
      <el-button size="small" @click="goToday" :disabled="isToday || dialogVisible">今天</el-button>
    </div>

    <!-- ===== Daily Nutrition Summary ===== -->
    <div class="day-summary" v-loading="store.loading">
      <div class="day-summary__energy">
        <div class="energy-ring">
          <svg viewBox="0 0 64 64" class="ring-svg">
            <circle cx="32" cy="32" r="28" class="ring-bg" />
            <circle cx="32" cy="32" r="28" class="ring-fill"
              :stroke-dasharray="`${energyPct * 1.759} 175.9`" />
          </svg>
          <div class="ring-label">
            <div class="ring-label__val">{{ fmt0(dayTotals.energyKcal) }}</div>
            <div class="ring-label__unit">/ {{ ENERGY_GOAL }} kcal</div>
          </div>
        </div>
        <div class="energy-text">
          <div class="energy-text__title">今日摄入</div>
          <div class="energy-text__remain" :class="overGoal ? 'over' : ''">
            {{ overGoal ? `超出 ${fmt0(dayTotals.energyKcal - ENERGY_GOAL)} kcal` : `还剩 ${fmt0(ENERGY_GOAL - dayTotals.energyKcal)} kcal` }}
          </div>
        </div>
      </div>

      <div class="day-summary__macros">
        <div class="macro-item" v-for="m in macros" :key="m.key">
          <div class="macro-item__header">
            <span class="macro-item__label">{{ m.label }}</span>
            <span class="macro-item__val">{{ fmt1(dayTotals[m.key]) }}g</span>
          </div>
          <div class="macro-bar">
            <div class="macro-bar__fill" :style="{ width: pct(dayTotals[m.key], m.goal) + '%', background: m.color }" />
          </div>
          <div class="macro-item__goal">目标 {{ m.goal }}g</div>
        </div>
      </div>
    </div>

    <!-- ===== Meal Sections ===== -->
    <div class="meal-list" v-loading="store.loading">
      <div v-for="mt in MEAL_TYPES" :key="mt.type" class="meal-card">

        <!-- Meal card header -->
        <div class="meal-card__header">
          <span class="meal-card__icon">{{ mt.icon }}</span>
          <span class="meal-card__title">{{ mt.label }}</span>
          <span class="meal-card__kcal" v-if="getMeal(mt.type)">
            {{ fmt0(getMeal(mt.type).totalEnergyKcal) }} kcal
          </span>
          <div class="meal-card__actions">
            <el-button
              v-if="getMeal(mt.type)"
              size="small" text type="danger"
              @click="confirmRemoveMeal(getMeal(mt.type).id)"
            >清空</el-button>
            <el-button size="small" class="btn-ai" @click="openRecommendDrawer(mt.type, mt.label)">
              <el-icon><MagicStick /></el-icon> AI 推荐
            </el-button>
            <el-button size="small" type="primary" @click="openAddDialog(mt.type)">
              <el-icon><Plus /></el-icon> 添加食物
            </el-button>
          </div>
        </div>

        <!-- Food items -->
        <div v-if="getMeal(mt.type) && getMeal(mt.type).items.length" class="meal-card__items">
          <div
            v-for="item in getMeal(mt.type).items"
            :key="item.id"
            class="food-item"
          >
            <div class="food-item__name">
              <span class="food-item__name-zh">{{ item.nameZh }}</span>
              <span class="food-item__name-en">{{ item.nameEn }}</span>
            </div>
            <div class="food-item__controls">
              <el-input-number
                :model-value="item.weightGram"
                :min="1"
                :max="9999"
                :precision="0"
                :step="10"
                size="small"
                class="food-item__weight"
                @change="(v) => handleWeightChange(item.id, v)"
              />
              <span class="food-item__unit">g</span>
              <span class="food-item__kcal">
                {{ item.energyKcal != null ? fmt1(item.energyKcal) + ' kcal' : '--' }}
              </span>
              <el-button
                size="small" text type="danger"
                @click="confirmRemoveItem(item.id)"
              ><el-icon><Delete /></el-icon></el-button>
            </div>
          </div>
        </div>

        <!-- Empty hint -->
        <div v-else class="meal-card__empty">
          暂无记录 — 点击"添加食物"开始记录
        </div>
      </div>
    </div>

    <!-- ===== Recipe Recommend Drawer ===== -->
    <RecipeRecommendDrawer
      v-model="drawerVisible"
      :meal-type="drawerMealType"
      :meal-label="drawerMealLabel"
      :meal-date="selectedDate"
    />

    <!-- ===== Add Food Dialog ===== -->
    <el-dialog
      v-model="dialogVisible"
      :title="`添加食物 · ${currentMealLabel}`"
      width="520px"
      destroy-on-close
      class="add-food-dialog"
    >
      <!-- Search input -->
      <el-autocomplete
        v-model="searchInput"
        class="dialog-search"
        placeholder="搜索食物名称…"
        :fetch-suggestions="fetchSuggestions"
        :debounce="250"
        clearable
        @select="handleSuggestionSelect"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
        <template #default="{ item }">
          <div class="sug-item">
            <span>{{ item.nameZh }}</span>
            <span class="sug-item__meta">{{ item.categoryNameZh }} · {{ item.energyKcal ?? '--' }} kcal/100g</span>
          </div>
        </template>
      </el-autocomplete>

      <!-- Selected food preview -->
      <div v-if="selectedFood" class="selected-food">
        <div class="selected-food__name">{{ selectedFood.nameZh }}</div>

        <!-- Per-100g reference -->
        <div class="selected-food__ref">
          <span class="selected-food__ref-label">每 100g</span>
          <span>能量 {{ fmt1(selectedFood.energyKcal) }} kcal</span>
          <span>蛋白质 {{ fmt1(selectedFood.protein) }}g</span>
          <span>脂肪 {{ fmt1(selectedFood.fat) }}g</span>
          <span>碳水 {{ fmt1(selectedFood.carbohydrate) }}g</span>
        </div>

        <!-- Weight selector -->
        <div class="weight-input-row">
          <span>用量</span>
          <el-input-number
            v-model="addWeight"
            :min="1" :max="9999" :precision="0" :step="10"
            size="default"
            class="weight-input"
          />
          <span>g</span>
        </div>

        <!-- Calculated nutrients (reacts to addWeight) -->
        <div class="selected-food__calc">
          <span class="selected-food__calc-label">实际摄入</span>
          <span class="calc-energy">{{ calcedNutrients.energyKcal }} kcal</span>
          <span>蛋白质 {{ calcedNutrients.protein }}g</span>
          <span>脂肪 {{ calcedNutrients.fat }}g</span>
          <span>碳水 {{ calcedNutrients.carbohydrate }}g</span>
        </div>
      </div>

      <div v-else class="dialog-hint">
        <el-icon><Search /></el-icon>
        <span>搜索并选择食物</span>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!selectedFood"
          :loading="store.submitting"
          @click="confirmAdd"
        >添加</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMealStore } from '@/stores/mealStore'
import { useFoodStore } from '@/stores/foodStore'
import { ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { ArrowLeft, ArrowRight, Plus, Delete, Search, MagicStick } from '@element-plus/icons-vue'

const store = useMealStore()
const foodStore = useFoodStore()

// ===== Meal type config =====
const MEAL_TYPES = [
  { type: 'BREAKFAST',      label: '早餐',    icon: '🌅' },
  { type: 'MORNING_SNACK', label: '上午加餐', icon: '🍎' },
  { type: 'LUNCH',          label: '午餐',    icon: '☀️' },
  { type: 'AFTERNOON_TEA', label: '下午茶',   icon: '🍵' },
  { type: 'DINNER',         label: '晚餐',    icon: '🌙' },
  { type: 'SUPPER',         label: '宵夜',    icon: '🌃' },
]

// ===== Nutrition goals (fixed for demo) =====
const ENERGY_GOAL = 2000
const macros = [
  { key: 'protein',      label: '蛋白质', goal: 75,  color: '#10B981' },
  { key: 'fat',          label: '脂肪',   goal: 65,  color: '#EF4444' },
  { key: 'carbohydrate', label: '碳水化合物', goal: 250, color: '#3B82F6' },
  { key: 'fiber',        label: '膳食纤维', goal: 25, color: '#8B5CF6' },
]

// ===== Date nav =====
const selectedDate = ref(store.selectedDate)
const isToday = computed(() => selectedDate.value === dayjs().format('YYYY-MM-DD'))
const overGoal = computed(() => store.dayTotals.energyKcal > ENERGY_GOAL)
const energyPct = computed(() => Math.min(100, (store.dayTotals.energyKcal / ENERGY_GOAL) * 100))

function shiftDay(delta) {
  const d = dayjs(selectedDate.value).add(delta, 'day').format('YYYY-MM-DD')
  selectedDate.value = d
  store.setDate(d)
}

function goToday() {
  const today = dayjs().format('YYYY-MM-DD')
  selectedDate.value = today
  store.setDate(today)
}

// ===== Meal data helpers =====
const dayTotals = computed(() => store.dayTotals)

function getMeal(mealType) {
  return store.getMealByType(mealType)
}

// ===== Weight update (debounced) =====
const weightTimers = {}

function handleWeightChange(itemId, newWeight) {
  if (!newWeight || newWeight < 1) return
  clearTimeout(weightTimers[itemId])
  weightTimers[itemId] = setTimeout(() => {
    store.updateItem(itemId, newWeight)
  }, 500)
}

// ===== Remove actions =====
function confirmRemoveItem(itemId) {
  ElMessageBox.confirm('确认删除该条记录？', '提示', {
    confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning',
  }).then(() => store.removeItem(itemId)).catch(() => {})
}

function confirmRemoveMeal(mealId) {
  ElMessageBox.confirm('确认清空该餐次的全部记录？', '提示', {
    confirmButtonText: '清空', cancelButtonText: '取消', type: 'warning',
  }).then(() => store.removeMeal(mealId)).catch(() => {})
}

// ===== Recipe Recommend Drawer =====
const drawerVisible  = ref(false)
const drawerMealType = ref('')
const drawerMealLabel = ref('')

function openRecommendDrawer(mealType, mealLabel) {
  drawerMealType.value  = mealType
  drawerMealLabel.value = mealLabel
  drawerVisible.value   = true
}

// ===== Add Food Dialog =====
const dialogVisible = ref(false)
const currentMealType = ref('')
const currentMealLabel = computed(
  () => MEAL_TYPES.find(m => m.type === currentMealType.value)?.label ?? ''
)
const searchInput = ref('')
const selectedFood = ref(null)
const addWeight = ref(100)
const calcedNutrients = computed(() => {
  const f = selectedFood.value
  if (!f) return { energyKcal: '--', protein: '--', fat: '--', carbohydrate: '--' }
  const ratio = addWeight.value / 100
  const calc = (v) => v != null ? (v * ratio).toFixed(1) : '--'
  return {
    energyKcal:   calc(f.energyKcal),
    protein:      calc(f.protein),
    fat:          calc(f.fat),
    carbohydrate: calc(f.carbohydrate),
  }
})

function openAddDialog(mealType) {
  currentMealType.value = mealType
  searchInput.value = ''
  selectedFood.value = null
  addWeight.value = 100
  dialogVisible.value = true
}

async function fetchSuggestions(queryStr, cb) {
  if (!queryStr?.trim()) { cb([]); return }
  await foodStore.fetchSuggestions(queryStr)
  cb(foodStore.suggestions.map(s => ({ ...s, value: s.nameZh })))
}

function handleSuggestionSelect(item) {
  selectedFood.value = item
}

async function confirmAdd() {
  if (!selectedFood.value) return
  await store.addItem(currentMealType.value, selectedFood.value.id, addWeight.value)
  dialogVisible.value = false
}

// ===== Formatters =====
function fmt0(v) { return v == null ? '0' : Math.round(v).toString() }
function fmt1(v) { return v == null ? '0' : parseFloat(v).toFixed(1) }
function pct(v, max) { return Math.min(100, ((v ?? 0) / max) * 100).toFixed(1) }

// ===== Init =====
onMounted(() => store.fetchDay())
</script>

<style lang="scss" scoped>
.meal-logger {
  display: flex;
  flex-direction: column;
  gap: $space-5;
  padding: $space-5 $space-6;
  max-width: 900px;
}

// ===== Date nav =====
.date-nav {
  display: flex;
  align-items: center;
  gap: $space-2;

  &__picker { width: 160px; }
}

// ===== Day summary =====
.day-summary {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
  padding: $space-5;
  display: flex;
  gap: $space-8;
  align-items: center;

  &__energy {
    display: flex;
    align-items: center;
    gap: $space-4;
    flex-shrink: 0;
  }

  &__macros {
    flex: 1;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $space-4;
  }
}

// Energy ring
.energy-ring {
  position: relative;
  width: 88px;
  height: 88px;
  flex-shrink: 0;
}

.ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: $color-bg-secondary;
  stroke-width: 7;
}

.ring-fill {
  fill: none;
  stroke: $color-accent;
  stroke-width: 7;
  stroke-linecap: round;
  stroke-dashoffset: 0;
  transition: stroke-dasharray 0.6s ease;
}

.ring-label {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;

  &__val {
    font-size: $font-size-md;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    line-height: 1.1;
  }

  &__unit {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    line-height: 1.2;
  }
}

.energy-text {
  &__title { font-size: $font-size-sm; color: $color-text-secondary; margin-bottom: $space-1; }

  &__remain {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-success;

    &.over { color: $color-danger; }
  }
}

// Macro bars
.macro-item {
  &__header {
    display: flex;
    justify-content: space-between;
    margin-bottom: $space-1;
    font-size: $font-size-xs;
  }

  &__label { color: $color-text-secondary; }
  &__val { font-weight: $font-weight-semibold; }
  &__goal { font-size: $font-size-xs; color: $color-text-disabled; margin-top: 2px; }
}

.macro-bar {
  height: 5px;
  background: $color-bg-secondary;
  border-radius: $radius-full;
  overflow: hidden;

  &__fill {
    height: 100%;
    border-radius: $radius-full;
    transition: width 0.5s ease;
  }
}

// ===== Meal cards =====
.meal-list {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.meal-card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    gap: $space-3;
    padding: $space-4 $space-5;
    border-bottom: 1px solid $color-border-light;
    background: $color-bg-primary;
  }

  &__icon { font-size: 20px; }
  &__title { font-weight: $font-weight-semibold; font-size: $font-size-md; }
  &__kcal { font-size: $font-size-sm; color: $color-warning; font-weight: $font-weight-medium; }

  &__actions {
    margin-left: auto;
    display: flex;
    gap: $space-2;
    align-items: center;
  }

  &__items { padding: $space-2 $space-5; }

  &__empty {
    padding: $space-4 $space-5;
    font-size: $font-size-sm;
    color: $color-text-disabled;
    text-align: center;
  }
}

.btn-ai {
  border: 1px solid $color-accent !important;
  color: $color-accent !important;
  background: transparent !important;
  transition: $transition-fast;

  &:hover {
    background: $color-accent !important;
    color: #fff !important;
  }
}

// ===== Food items =====
.food-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 0;
  border-bottom: 1px solid $color-border-light;

  &:last-child { border-bottom: none; }

  &__name {
    flex: 1;
    min-width: 0;
  }

  &__name-zh {
    display: block;
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__name-en {
    display: block;
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__controls {
    display: flex;
    align-items: center;
    gap: $space-2;
    flex-shrink: 0;
  }

  &__weight { width: 100px; }
  &__unit { font-size: $font-size-sm; color: $color-text-secondary; }

  &__kcal {
    width: 80px;
    text-align: right;
    font-size: $font-size-sm;
    color: $color-warning;
    font-weight: $font-weight-medium;
  }
}

// ===== Add food dialog =====
.add-food-dialog {
  :deep(.el-dialog__body) { padding: $space-5; }
}

.dialog-search {
  width: 100%;
  margin-bottom: $space-4;
}

.sug-item {
  display: flex;
  justify-content: space-between;
  align-items: center;

  &__meta { font-size: $font-size-xs; color: $color-text-secondary; }
}

.selected-food {
  background: $color-bg-primary;
  border-radius: $radius-base;
  padding: $space-4;
  display: flex;
  flex-direction: column;
  gap: $space-3;

  &__name {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
  }

  &__ref {
    display: flex;
    flex-wrap: wrap;
    gap: $space-3;
    font-size: $font-size-sm;
    color: $color-text-secondary;
    padding: $space-2 $space-3;
    background: $color-surface;
    border-radius: $radius-sm;
    border: 1px solid $color-border-light;
  }

  &__ref-label {
    color: $color-text-disabled;
    font-size: $font-size-xs;
    align-self: center;
  }

  &__calc {
    display: flex;
    flex-wrap: wrap;
    gap: $space-3;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    padding: $space-2 $space-3;
    background: $color-accent-light;
    border-radius: $radius-sm;
  }

  &__calc-label {
    font-size: $font-size-xs;
    color: $color-accent;
    font-weight: $font-weight-semibold;
    align-self: center;
  }
}

.calc-energy {
  color: $color-warning;
  font-weight: $font-weight-bold;
}

.weight-input-row {
  display: flex;
  align-items: center;
  gap: $space-3;
  font-size: $font-size-base;
}

.weight-input { width: 130px; }

.weight-preview {
  font-size: $font-size-sm;
  color: $color-warning;
  font-weight: $font-weight-semibold;
}

.dialog-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-2;
  padding: $space-8;
  color: $color-text-disabled;
  font-size: $font-size-base;
}
</style>
