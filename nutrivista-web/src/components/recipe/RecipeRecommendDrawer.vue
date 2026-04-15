<template>
  <!-- ===== Main Drawer ===== -->
  <el-drawer
    v-model="drawerVisible"
    direction="rtl"
    size="520px"
    :destroy-on-close="false"
    class="recipe-drawer"
    @open="onOpen"
  >
    <template #header>
      <div class="drawer-header">
        <el-icon class="drawer-header__icon"><MagicStick /></el-icon>
        <span class="drawer-header__title">AI 菜谱推荐</span>
        <el-tag size="small" class="drawer-header__meal-tag">{{ mealLabel }}</el-tag>
      </div>
    </template>

    <div class="drawer-body">

      <!-- ===== Condition Section ===== -->
      <div class="condition-section">
        <el-input
          v-model="userQuery"
          type="textarea"
          :rows="2"
          placeholder="描述你想吃什么，例如：清淡的、快手的、有鱼的、辣一点的..."
          maxlength="200"
          show-word-limit
          class="query-input"
        />

        <!-- Quick tags -->
        <div class="quick-tags">
          <div v-for="group in QUICK_TAGS" :key="group.group" class="tag-group">
            <span class="tag-group__label">{{ group.group }}</span>
            <div class="tag-group__items">
              <span
                v-for="tag in group.tags"
                :key="tag"
                class="qtag"
                :class="{ 'qtag--active': selectedTags.includes(tag) }"
                @click="toggleTag(tag)"
              >{{ tag }}</span>
            </div>
          </div>
        </div>

        <!-- Collapsible filters -->
        <el-collapse v-model="filterExpanded" class="filter-collapse">
          <el-collapse-item name="f" title="更多筛选条件">
            <div class="filters">
              <div class="filter-row">
                <span class="filter-label">偏好菜系</span>
                <el-select v-model="preferredCuisine" placeholder="不限" clearable size="small" style="flex:1">
                  <el-option v-for="c in cuisines" :key="c.name" :label="`${c.name} (${c.count})`" :value="c.name" />
                </el-select>
              </div>

              <div class="filter-row">
                <span class="filter-label">热量上限</span>
                <div style="flex:1">
                  <el-slider
                    v-model="maxCalories"
                    :min="200" :max="1000" :step="50"
                    :format-tooltip="v => v >= 1000 ? '不限' : v + ' kcal'"
                  />
                </div>
                <span class="filter-val">{{ maxCalories >= 1000 ? '不限' : maxCalories + ' kcal' }}</span>
              </div>

              <div class="filter-row filter-row--wrap">
                <span class="filter-label">排除食材</span>
                <div class="exclude-area">
                  <el-tag
                    v-for="ing in excludeIngredients"
                    :key="ing"
                    closable
                    size="small"
                    @close="removeExclude(ing)"
                  >{{ ing }}</el-tag>
                  <el-input
                    v-if="excludeInputVisible"
                    ref="excludeInputRef"
                    v-model="excludeInputVal"
                    size="small"
                    style="width:90px"
                    placeholder="食材名"
                    @keyup.enter="addExclude"
                    @blur="addExclude"
                  />
                  <el-button v-else link size="small" @click="showExcludeInput">
                    <el-icon><Plus /></el-icon> 添加
                  </el-button>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>

        <el-button
          type="primary"
          class="recommend-btn"
          :loading="loading"
          @click="doRecommend"
        >
          <el-icon><MagicStick /></el-icon>
          开始推荐
        </el-button>
      </div>

      <!-- ===== Results Section ===== -->
      <div class="results-section">

        <!-- Loading -->
        <template v-if="loading">
          <div class="loading-hint">
            <el-icon class="is-loading"><Loading /></el-icon>
            AI 正在为您搭配菜谱...
          </div>
          <div v-for="i in 3" :key="i" class="skeleton-wrap">
            <el-skeleton :rows="4" animated />
          </div>
        </template>

        <!-- Success -->
        <template v-else-if="hasSearched && recommendations.length">
          <!-- AI summary -->
          <div class="ai-summary">
            <el-icon class="ai-summary__ico"><ChatDotRound /></el-icon>
            <div class="ai-summary__body">
              <p>{{ aiSummary }}</p>
              <p v-if="nutritionAdvice" class="ai-summary__advice">{{ nutritionAdvice }}</p>
            </div>
          </div>

          <!-- Recipe cards -->
          <transition-group name="card-fade" tag="div" class="recipe-list">
            <div
              v-for="(recipe, idx) in recommendations"
              :key="recipe.recipeId"
              class="recipe-card"
              :style="{ '--enter-delay': idx * 100 + 'ms' }"
            >
              <!-- Top info -->
              <div class="rc-top">
                <div class="rc-title-row">
                  <span class="rc-name">{{ recipe.name }}</span>
                  <span
                    v-if="recipe.cuisine"
                    class="rc-cuisine"
                    :style="{ background: cuisineBg(recipe.cuisine), color: '#fff' }"
                  >{{ recipe.cuisine }}</span>
                </div>

                <div class="rc-metrics">
                  <span class="metric-mono">{{ recipe.calories }} kcal</span>
                  <span class="metric-sep">·</span>
                  <span class="metric-mono">{{ recipe.cookTime }} 分钟</span>
                  <span class="metric-sep">·</span>
                  <span>{{ diffZh(recipe.difficulty) }}</span>
                </div>

                <p class="rc-reason">"{{ recipe.matchReason }}"</p>

                <p class="rc-ing-preview">{{ ingPreview(recipe.ingredients) }}</p>

                <div class="rc-relevance">
                  <span class="rc-relevance__label">匹配度</span>
                  <div class="rc-relevance__bar">
                    <div
                      class="rc-relevance__fill"
                      :style="{ width: Math.min(100, (recipe.relevanceScore ?? 0) * 100).toFixed(0) + '%' }"
                    />
                  </div>
                  <span class="rc-relevance__val">{{ Math.min(100, ((recipe.relevanceScore ?? 0) * 100)).toFixed(0) }}%</span>
                </div>
              </div>

              <!-- Actions -->
              <div class="rc-actions">
                <el-button text size="small" @click="toggleDetail(recipe.recipeId)">
                  {{ expandedId === recipe.recipeId ? '收起' : '查看详情' }}
                  <el-icon>
                    <ArrowUp v-if="expandedId === recipe.recipeId" />
                    <ArrowDown v-else />
                  </el-icon>
                </el-button>
                <el-button type="primary" size="small" @click="openAddDialog(recipe)">
                  添加到本餐
                </el-button>
              </div>

              <!-- Expandable detail -->
              <transition name="slide-down">
                <div v-if="expandedId === recipe.recipeId" class="rc-detail">

                  <!-- Ingredients -->
                  <div class="detail-block">
                    <div class="detail-block__title">食材清单</div>
                    <div class="ing-table">
                      <div v-for="ing in recipe.ingredients" :key="ing.name" class="ing-row">
                        <span class="ing-row__name">{{ ing.name }}</span>
                        <span class="ing-row__amount">{{ ing.amount }}{{ ing.unit }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- Steps -->
                  <div class="detail-block">
                    <div class="detail-block__title">做法步骤</div>
                    <ol class="steps-ol">
                      <li v-for="(s, i) in recipe.steps" :key="i">{{ s }}</li>
                    </ol>
                  </div>

                  <!-- Nutrition mini chart -->
                  <div v-if="recipe.nutrition" class="detail-block">
                    <div class="detail-block__title">营养成分 (每份)</div>
                    <div class="nutr-bars">
                      <div
                        v-for="n in nutrItems(recipe.nutrition)"
                        :key="n.label"
                        class="nutr-row"
                      >
                        <span class="nutr-row__label">{{ n.label }}</span>
                        <div class="nutr-row__track">
                          <div class="nutr-row__fill" :style="{ width: n.pct + '%', background: n.color }" />
                        </div>
                        <span class="nutr-row__val">{{ n.val }}g</span>
                      </div>
                    </div>
                  </div>
                </div>
              </transition>
            </div>
          </transition-group>

          <!-- Bottom actions -->
          <div class="result-footer">
            <el-button :loading="loading" @click="doRecommend">换一批推荐</el-button>
            <el-button text @click="goBrowser">浏览全部菜谱 →</el-button>
          </div>
        </template>

        <!-- Empty after search -->
        <div v-else-if="hasSearched && !loading" class="state-empty">
          <p>未找到符合条件的菜谱，请尝试放宽筛选条件</p>
          <el-button @click="doRecommend">再试一次</el-button>
        </div>

        <!-- Initial hint -->
        <div v-else-if="!hasSearched" class="state-initial">
          <div class="state-initial__emoji">🍽️</div>
          <p>描述你的喜好或选择快捷标签</p>
          <p class="state-initial__sub">AI 将根据语义搜索 + 营养分析为你推荐</p>
        </div>
      </div>
    </div>
  </el-drawer>

  <!-- ===== Add to Meal Dialog ===== -->
  <el-dialog
    v-model="addDialogVisible"
    title="添加到本餐"
    width="420px"
    :close-on-click-modal="false"
    append-to-body
  >
    <div class="add-dlg">
      <p class="add-dlg__name">{{ addingRecipe?.name }}</p>
      <p class="add-dlg__sub">将按所选份数换算食材重量添加至 <strong>{{ mealLabel }}</strong></p>

      <div class="add-dlg__servings">
        <span class="add-dlg__label">份数</span>
        <el-radio-group v-model="addingServings">
          <el-radio-button :value="0.5">0.5 份</el-radio-button>
          <el-radio-button :value="1">1 份</el-radio-button>
          <el-radio-button :value="1.5">1.5 份</el-radio-button>
          <el-radio-button :value="2">2 份</el-radio-button>
        </el-radio-group>
      </div>

      <div class="add-dlg__ings">
        <div class="add-dlg__ings-title">将添加的食材（克/毫升单位）</div>
        <div
          v-for="ing in weightIngredients"
          :key="ing.name"
          class="add-ing-row"
        >
          <span class="add-ing-row__name">{{ ing.name }}</span>
          <span class="add-ing-row__weight">
            约 {{ calcWeight(ing.amount, addingServings, addingRecipe?.servings) }}g
          </span>
        </div>
        <p v-if="skippedCount > 0" class="add-dlg__skip-note">
          另有 {{ skippedCount }} 种食材（非克重单位，如"1条"）将由后端跳过
        </p>
      </div>
    </div>

    <template #footer>
      <el-button @click="addDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="adding" @click="confirmAdd">确认添加</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  MagicStick, Plus, ArrowUp, ArrowDown,
  Loading, ChatDotRound,
} from '@element-plus/icons-vue'
import { getRecipeRecommendations, getCuisines, addRecipeToMeal } from '@/api/recipe'
import { useMealStore } from '@/stores/mealStore'

// ── Props / Emits ──────────────────────────────────────────────────────────────
const props = defineProps({
  modelValue: Boolean,
  mealType:   { type: String, default: '' },
  mealLabel:  { type: String, default: '' },
  mealDate:   { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue'])

const drawerVisible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

// ── Constants ──────────────────────────────────────────────────────────────────
const QUICK_TAGS = [
  { group: '口味', tags: ['清淡', '麻辣', '酸甜', '鲜香', '咸鲜'] },
  { group: '烹饪', tags: ['快手菜', '简单', '不开火'] },
  { group: '营养', tags: ['高蛋白', '低脂', '低碳水', '高纤维'] },
  { group: '食材', tags: ['有肉', '素食', '有鱼', '有蛋'] },
]

const CUISINE_COLORS = {
  '川菜': '#EF4444', '粤菜': '#F59E0B', '鲁菜': '#3B82F6',
  '苏菜': '#10B981', '浙菜': '#8B5CF6', '闽菜': '#EC4899',
  '湘菜': '#F97316', '徽菜': '#14B8A6', '京菜': '#6366F1',
  '东北菜': '#0EA5E9', '西北菜': '#84CC16', '其他': '#9CA3AF',
}

// ── State ──────────────────────────────────────────────────────────────────────
const mealStore     = useMealStore()
const router        = useRouter()

const userQuery          = ref('')
const selectedTags       = ref([])
const preferredCuisine   = ref('')
const maxCalories        = ref(1000)
const excludeIngredients = ref([])
const filterExpanded     = ref([])
const cuisines           = ref([])

const loading        = ref(false)
const hasSearched    = ref(false)
const recommendations = ref([])
const aiSummary      = ref('')
const nutritionAdvice = ref('')
const expandedId     = ref(null)

const excludeInputVisible = ref(false)
const excludeInputVal     = ref('')
const excludeInputRef     = ref(null)

const addDialogVisible = ref(false)
const addingRecipe     = ref(null)
const addingServings   = ref(1)
const adding           = ref(false)

// ── Computed ───────────────────────────────────────────────────────────────────
const weightIngredients = computed(() =>
  (addingRecipe.value?.ingredients ?? []).filter(i =>
    i.unit?.toLowerCase() === 'g' || i.unit?.toLowerCase() === 'ml'
  )
)

const skippedCount = computed(() =>
  (addingRecipe.value?.ingredients ?? []).length - weightIngredients.value.length
)

// ── Lifecycle ──────────────────────────────────────────────────────────────────
watch(() => props.mealType, () => {
  // 切换餐次时清空结果，但保留输入
  recommendations.value = []
  hasSearched.value = false
  expandedId.value = null
})

async function onOpen() {
  if (!cuisines.value.length) {
    try {
      const res = await getCuisines()
      cuisines.value = res.data ?? []
    } catch { /* 加载失败不影响主流程 */ }
  }
}

// ── Tag helpers ────────────────────────────────────────────────────────────────
function toggleTag(tag) {
  const idx = selectedTags.value.indexOf(tag)
  if (idx >= 0) selectedTags.value.splice(idx, 1)
  else selectedTags.value.push(tag)
}

// ── Exclude ingredient helpers ─────────────────────────────────────────────────
function showExcludeInput() {
  excludeInputVisible.value = true
  nextTick(() => excludeInputRef.value?.focus())
}

function addExclude() {
  const v = excludeInputVal.value.trim()
  if (v && !excludeIngredients.value.includes(v)) {
    excludeIngredients.value.push(v)
  }
  excludeInputVal.value = ''
  excludeInputVisible.value = false
}

function removeExclude(ing) {
  excludeIngredients.value = excludeIngredients.value.filter(i => i !== ing)
}

// ── Recommend ──────────────────────────────────────────────────────────────────
async function doRecommend() {
  loading.value = true
  hasSearched.value = true
  expandedId.value = null

  const tagText = selectedTags.value.join(' ')
  const query   = [props.userQuery, userQuery.value.trim(), tagText].filter(Boolean).join(' ')

  const payload = {
    mealType:           props.mealType || undefined,
    userQuery:          query || undefined,
    preferredCuisine:   preferredCuisine.value || undefined,
    maxCalories:        maxCalories.value < 1000 ? maxCalories.value : undefined,
    excludeIngredients: excludeIngredients.value.length ? excludeIngredients.value : undefined,
    count:              3,
  }

  try {
    const res = await getRecipeRecommendations(payload)
    const d = res.data
    recommendations.value  = d.recipes ?? []
    aiSummary.value         = d.aiSummary ?? ''
    nutritionAdvice.value   = d.advice?.advice ?? ''
  } catch {
    recommendations.value = []
  } finally {
    loading.value = false
  }
}

// ── Detail expand ──────────────────────────────────────────────────────────────
function toggleDetail(id) {
  expandedId.value = expandedId.value === id ? null : id
}

// ── Add to meal ────────────────────────────────────────────────────────────────
function openAddDialog(recipe) {
  addingRecipe.value   = recipe
  addingServings.value = 1
  addDialogVisible.value = true
}

async function confirmAdd() {
  adding.value = true
  try {
    await addRecipeToMeal(addingRecipe.value.recipeId, {
      mealDate:  props.mealDate,
      mealType:  props.mealType,
      servings:  addingServings.value,
    })
    const name = addingRecipe.value.name
    addDialogVisible.value = false
    drawerVisible.value    = false
    ElMessage.success(`已将「${name}」的食材添加到${props.mealLabel}`)
    mealStore.fetchDay()
  } finally {
    adding.value = false
  }
}

// ── Helpers ────────────────────────────────────────────────────────────────────
function cuisineBg(cuisine) {
  return CUISINE_COLORS[cuisine] ?? CUISINE_COLORS['其他']
}

function diffZh(d) {
  return { easy: '简单', medium: '中等', hard: '较难' }[d] ?? d
}

function ingPreview(ings) {
  if (!ings?.length) return ''
  const names = ings.slice(0, 5).map(i => i.name)
  return ings.length > 5
    ? names.join('、') + ` 等 ${ings.length} 种食材`
    : names.join('、')
}

function nutrItems(nutrition) {
  const raw = [
    { label: '蛋白质', value: nutrition.proteinG, color: '#10B981' },
    { label: '脂肪',   value: nutrition.fatG,     color: '#EF4444' },
    { label: '碳水',   value: nutrition.carbG,    color: '#3B82F6' },
    { label: '膳食纤维', value: nutrition.fiberG, color: '#8B5CF6' },
  ].filter(i => i.value != null)
  const max = Math.max(...raw.map(i => i.value), 1)
  return raw.map(i => ({ ...i, pct: Math.round(i.value / max * 100), val: i.value.toFixed(1) }))
}

function calcWeight(amount, servings, recipeServings) {
  const raw  = parseFloat(amount)
  const serv = recipeServings || 1
  if (isNaN(raw)) return '--'
  return (raw / serv * servings).toFixed(0)
}

function goBrowser() {
  drawerVisible.value = false
  router.push('/recipes')
}
</script>

<style lang="scss" scoped>
// ── Drawer ─────────────────────────────────────────────────────────────────────
:deep(.el-drawer__header) { margin-bottom: 0; padding: $space-4 $space-5; border-bottom: 1px solid $color-border; }
:deep(.el-drawer__body)   { padding: 0; overflow-y: auto; }

.drawer-header {
  display: flex;
  align-items: center;
  gap: $space-2;

  &__icon { font-size: 18px; color: $color-accent; }
  &__title { font-size: $font-size-md; font-weight: $font-weight-semibold; }
  &__meal-tag { margin-left: $space-1; }
}

.drawer-body {
  display: flex;
  flex-direction: column;
  gap: $space-5;
  padding: $space-5;
}

// ── Condition section ──────────────────────────────────────────────────────────
.condition-section {
  display: flex;
  flex-direction: column;
  gap: $space-4;
  padding: $space-4;
  background: $color-bg-primary;
  border-radius: $radius-lg;
  border: 1px solid $color-border;
}

.query-input { width: 100%; }

.quick-tags {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.tag-group {
  display: flex;
  align-items: flex-start;
  gap: $space-3;

  &__label {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    min-width: 32px;
    padding-top: 4px;
    flex-shrink: 0;
  }

  &__items {
    display: flex;
    flex-wrap: wrap;
    gap: $space-2;
  }
}

.qtag {
  display: inline-block;
  padding: 2px $space-3;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  border: 1px solid $color-accent;
  color: $color-accent;
  cursor: pointer;
  transition: $transition-fast;
  user-select: none;

  &:hover { background: $color-accent-light; }

  &--active {
    background: $color-accent;
    color: #fff;
  }
}

.filter-collapse {
  border: none;

  :deep(.el-collapse-item__header) {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    background: transparent;
    border: none;
    height: 32px;
  }

  :deep(.el-collapse-item__wrap)  { border: none; background: transparent; }
  :deep(.el-collapse-item__content) { padding-bottom: 0; }
}

.filters {
  display: flex;
  flex-direction: column;
  gap: $space-3;
  padding-top: $space-2;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: $space-3;

  &--wrap { align-items: flex-start; flex-wrap: wrap; }
}

.filter-label {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  min-width: 52px;
  flex-shrink: 0;
}

.filter-val {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  min-width: 52px;
  text-align: right;
}

.exclude-area {
  display: flex;
  flex-wrap: wrap;
  gap: $space-2;
  align-items: center;
}

.recommend-btn {
  width: 100%;
  height: 40px;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
}

// ── Results ────────────────────────────────────────────────────────────────────
.results-section { display: flex; flex-direction: column; gap: $space-4; }

.loading-hint {
  display: flex;
  align-items: center;
  gap: $space-2;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  padding: $space-2 0;
}

.skeleton-wrap {
  padding: $space-4;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
}

// AI summary
.ai-summary {
  display: flex;
  gap: $space-3;
  padding: $space-4;
  background: #EEF2FF;
  border-radius: $radius-lg;
  border: 1px solid rgba(79, 70, 229, 0.15);

  &__ico {
    font-size: 18px;
    color: $color-accent;
    flex-shrink: 0;
    margin-top: 2px;
  }

  &__body {
    font-size: $font-size-sm;
    color: #4B5563;
    line-height: $line-height-relaxed;

    p { margin: 0 0 $space-1; }
  }

  &__advice {
    color: $color-text-secondary;
    font-style: italic;
  }
}

// Recipe list + transition
.recipe-list { display: flex; flex-direction: column; gap: $space-4; }

.card-fade-enter-active {
  animation: card-in 0.35s ease both;
  animation-delay: var(--enter-delay, 0ms);
}

@keyframes card-in {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

// Recipe card
.recipe-card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
  overflow: hidden;
  transition: box-shadow $transition-fast;

  &:hover { box-shadow: $shadow-md; }
}

.rc-top { padding: $space-4 $space-4 0; }

.rc-title-row {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin-bottom: $space-2;
}

.rc-name {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.rc-cuisine {
  font-size: $font-size-xs;
  padding: 2px $space-2;
  border-radius: $radius-full;
  font-weight: $font-weight-medium;
  flex-shrink: 0;
}

.rc-metrics {
  display: flex;
  align-items: center;
  gap: $space-2;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-bottom: $space-2;
}

.metric-mono {
  font-family: $font-mono;
  color: $color-warning;
  font-weight: $font-weight-semibold;
}

.metric-sep { color: $color-text-disabled; }

.rc-reason {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  font-style: italic;
  margin: 0 0 $space-2;
  line-height: $line-height-relaxed;
}

.rc-ing-preview {
  font-size: $font-size-xs;
  color: $color-text-tertiary;
  margin: 0 0 $space-3;
}

.rc-relevance {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin-bottom: $space-3;

  &__label { font-size: $font-size-xs; color: $color-text-tertiary; width: 36px; flex-shrink: 0; }

  &__bar {
    flex: 1;
    height: 4px;
    background: $color-bg-secondary;
    border-radius: $radius-full;
    overflow: hidden;
  }

  &__fill {
    height: 100%;
    background: $color-accent;
    border-radius: $radius-full;
    transition: width 0.6s ease;
  }

  &__val { font-size: $font-size-xs; color: $color-accent; font-family: $font-mono; width: 30px; text-align: right; }
}

.rc-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: $space-2;
  padding: $space-3 $space-4;
  border-top: 1px solid $color-border-light;
  background: $color-bg-primary;
}

// Expandable detail
.rc-detail {
  padding: $space-4;
  background: $color-bg-primary;
  border-top: 1px solid $color-border-light;
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}
.slide-down-enter-from, .slide-down-leave-to {
  opacity: 0;
  max-height: 0;
}
.slide-down-enter-to, .slide-down-leave-from {
  opacity: 1;
  max-height: 800px;
}

.detail-block {
  &__title {
    font-size: $font-size-xs;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    margin-bottom: $space-2;
  }
}

.ing-table { display: flex; flex-direction: column; gap: 4px; }

.ing-row {
  display: flex;
  justify-content: space-between;
  font-size: $font-size-sm;

  &__name  { color: $color-text-primary; }
  &__amount { color: $color-text-secondary; font-family: $font-mono; }
}

.steps-ol {
  margin: 0;
  padding-left: $space-5;
  display: flex;
  flex-direction: column;
  gap: $space-2;

  li { font-size: $font-size-sm; color: $color-text-primary; line-height: $line-height-relaxed; }
}

.nutr-bars { display: flex; flex-direction: column; gap: $space-2; }

.nutr-row {
  display: flex;
  align-items: center;
  gap: $space-2;

  &__label { font-size: $font-size-xs; color: $color-text-secondary; width: 44px; }
  &__track { flex: 1; height: 6px; background: $color-bg-secondary; border-radius: $radius-full; overflow: hidden; }
  &__fill  { height: 100%; border-radius: $radius-full; transition: width 0.6s ease; }
  &__val   { font-size: $font-size-xs; color: $color-text-secondary; font-family: $font-mono; width: 38px; text-align: right; }
}

// Footer
.result-footer {
  display: flex;
  gap: $space-3;
  padding-top: $space-2;
}

// States
.state-empty, .state-initial {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: $space-2;
  padding: $space-12 $space-6;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.state-initial {
  &__emoji { font-size: 48px; margin-bottom: $space-2; }
  &__sub   { color: $color-text-disabled; font-size: $font-size-xs; }
}

// ── Add dialog ─────────────────────────────────────────────────────────────────
.add-dlg {
  display: flex;
  flex-direction: column;
  gap: $space-4;

  &__name { font-size: $font-size-lg; font-weight: $font-weight-bold; margin: 0; }
  &__sub  { font-size: $font-size-sm; color: $color-text-secondary; margin: 0; }

  &__servings {
    display: flex;
    align-items: center;
    gap: $space-3;
  }

  &__label { font-size: $font-size-sm; color: $color-text-secondary; flex-shrink: 0; }

  &__ings {
    background: $color-bg-primary;
    border-radius: $radius-base;
    padding: $space-3;
  }

  &__ings-title {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    margin-bottom: $space-2;
    font-weight: $font-weight-medium;
  }

  &__skip-note {
    font-size: $font-size-xs;
    color: $color-text-disabled;
    margin-top: $space-2;
  }
}

.add-ing-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: $font-size-sm;
  border-bottom: 1px solid $color-border-light;

  &:last-of-type { border-bottom: none; }

  &__name   { color: $color-text-primary; }
  &__weight { color: $color-text-secondary; font-family: $font-mono; }
}
</style>
