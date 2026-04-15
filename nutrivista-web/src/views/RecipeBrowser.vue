<template>
  <div class="recipe-browser">
    <!-- ===== Page Header ===== -->
    <div class="page-header">
      <h1 class="page-header__title">菜谱浏览</h1>
      <p class="page-header__sub">探索美食，发现适合您的健康菜谱</p>
    </div>

    <!-- ===== Search Bar ===== -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索菜谱名称…"
        clearable
        size="large"
        class="search-bar__input"
        @keyup.enter="doSearch"
        @clear="doSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" size="large" @click="doSearch">搜索</el-button>
    </div>

    <!-- ===== Cuisine Tabs ===== -->
    <div class="cuisine-tabs" v-if="!cuisinesLoading">
      <button
        class="cuisine-tab"
        :class="{ 'cuisine-tab--active': selectedCuisine === '' }"
        @click="selectCuisine('')"
      >
        全部菜系
        <span class="cuisine-tab__count">{{ totalCount }}</span>
      </button>
      <button
        v-for="c in cuisines"
        :key="c.name"
        class="cuisine-tab"
        :class="{ 'cuisine-tab--active': selectedCuisine === c.name }"
        @click="selectCuisine(c.name)"
      >
        {{ c.name }}
        <span class="cuisine-tab__count">{{ c.count }}</span>
      </button>
    </div>
    <el-skeleton v-else :rows="1" animated class="cuisine-tabs-skeleton" />

    <!-- ===== Recipe Grid ===== -->
    <div v-if="loading" class="recipe-grid">
      <div v-for="i in 12" :key="i" class="recipe-card recipe-card--skeleton">
        <el-skeleton :rows="3" animated />
      </div>
    </div>

    <template v-else>
      <div v-if="recipes.length" class="recipe-grid">
        <div
          v-for="(recipe, idx) in recipes"
          :key="recipe.id"
          class="recipe-card"
          :style="{ '--card-delay': `${idx * 40}ms` }"
          @click="openDetail(recipe)"
        >
          <!-- Cuisine badge -->
          <div class="recipe-card__badge" :style="{ background: cuisineColor(recipe.cuisine) }">
            {{ recipe.cuisine }}
          </div>

          <!-- Category chip -->
          <div class="recipe-card__category">{{ recipe.category }}</div>

          <!-- Name -->
          <h3 class="recipe-card__name">{{ recipe.name }}</h3>

          <!-- Metrics row -->
          <div class="recipe-card__metrics">
            <span class="metric">
              <el-icon><Sunny /></el-icon>
              <span class="metric__val">{{ recipe.calories }}</span>
              <span class="metric__unit">kcal</span>
            </span>
            <span class="metric">
              <el-icon><Timer /></el-icon>
              <span class="metric__val">{{ recipe.cookTime }}</span>
              <span class="metric__unit">min</span>
            </span>
          </div>

          <!-- Difficulty tag -->
          <div class="recipe-card__footer">
            <el-tag
              :type="difficultyType(recipe.difficulty)"
              size="small"
              round
            >{{ recipe.difficulty }}</el-tag>
          </div>
        </div>
      </div>

      <el-empty v-else description="没有找到符合条件的菜谱" :image-size="120" class="empty-state" />
    </template>

    <!-- ===== Pagination ===== -->
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadPage"
      />
    </div>

    <!-- ===== Detail Drawer ===== -->
    <el-drawer
      v-model="drawerVisible"
      :title="activeRecipe?.name"
      direction="rtl"
      size="480px"
      class="detail-drawer"
    >
      <template v-if="detailLoading">
        <el-skeleton :rows="8" animated />
      </template>
      <template v-else-if="detail">
        <!-- Meta -->
        <div class="detail-meta">
          <el-tag :style="{ background: cuisineColor(detail.cuisine), color: '#fff', border: 'none' }">
            {{ detail.cuisine }}
          </el-tag>
          <el-tag type="info">{{ detail.category }}</el-tag>
          <el-tag :type="difficultyType(detail.difficulty)">{{ detail.difficulty }}</el-tag>
        </div>

        <!-- Stats -->
        <div class="detail-stats">
          <div class="stat-item">
            <div class="stat-item__val">{{ detail.calories }}</div>
            <div class="stat-item__label">千卡/份</div>
          </div>
          <div class="stat-item">
            <div class="stat-item__val">{{ detail.cookTime }}</div>
            <div class="stat-item__label">烹饪时间(分)</div>
          </div>
          <div class="stat-item">
            <div class="stat-item__val">{{ detail.servings }}</div>
            <div class="stat-item__label">份数</div>
          </div>
        </div>

        <!-- Nutrition bar -->
        <div v-if="detail.nutrition" class="detail-section">
          <div class="detail-section__title">营养素（每份）</div>
          <div class="nutrition-bars">
            <div v-for="n in nutritionItems(detail.nutrition)" :key="n.label" class="nutr-row">
              <span class="nutr-row__label">{{ n.label }}</span>
              <el-progress
                :percentage="n.pct"
                :color="n.color"
                :stroke-width="6"
                :show-text="false"
                class="nutr-row__bar"
              />
              <span class="nutr-row__val">{{ n.val }}g</span>
            </div>
          </div>
        </div>

        <!-- Ingredients -->
        <div v-if="detail.ingredients?.length" class="detail-section">
          <div class="detail-section__title">食材清单</div>
          <div class="ingredient-list">
            <div v-for="ing in detail.ingredients" :key="ing.name" class="ingredient-row">
              <span class="ingredient-row__name">{{ ing.name }}</span>
              <span class="ingredient-row__amount">{{ ing.amount }}{{ ing.unit }}</span>
            </div>
          </div>
        </div>

        <!-- Steps -->
        <div v-if="detail.steps?.length" class="detail-section">
          <div class="detail-section__title">烹饪步骤</div>
          <ol class="steps-list">
            <li v-for="(step, i) in detail.steps" :key="i" class="steps-list__item">
              {{ step }}
            </li>
          </ol>
        </div>

        <!-- Tips -->
        <div v-if="detail.tips" class="detail-section">
          <div class="detail-section__title">小贴士</div>
          <p class="tips-text">{{ detail.tips }}</p>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Sunny, Timer } from '@element-plus/icons-vue'
import { searchRecipes, getCuisines, getRecipeDetail } from '@/api/recipe'

// ── State ──────────────────────────────────────────────────────────────────
const keyword        = ref('')
const selectedCuisine = ref('')
const currentPage    = ref(1)
const pageSize       = 12
const total          = ref(0)
const totalCount     = ref(0)

const recipes         = ref([])
const cuisines        = ref([])
const loading         = ref(false)
const cuisinesLoading = ref(false)

const drawerVisible = ref(false)
const activeRecipe  = ref(null)
const detail        = ref(null)
const detailLoading = ref(false)

// ── Init ───────────────────────────────────────────────────────────────────
onMounted(() => {
  loadCuisines()
  loadPage(1)
})

// ── Data Loading ───────────────────────────────────────────────────────────
async function loadCuisines() {
  cuisinesLoading.value = true
  try {
    const res = await getCuisines()
    cuisines.value = res.data ?? []
    totalCount.value = cuisines.value.reduce((s, c) => s + (c.count ?? 0), 0)
  } finally {
    cuisinesLoading.value = false
  }
}

async function loadPage(page) {
  currentPage.value = page
  loading.value = true
  try {
    const res = await searchRecipes({
      keyword:  keyword.value.trim() || undefined,
      cuisine:  selectedCuisine.value || undefined,
      page,
      pageSize,
    })
    const data    = res.data ?? {}
    recipes.value = data.records ?? []
    total.value   = data.total ?? 0
  } finally {
    loading.value = false
  }
}

function doSearch() {
  loadPage(1)
}

function selectCuisine(c) {
  selectedCuisine.value = c
  doSearch()
}

async function openDetail(recipe) {
  activeRecipe.value  = recipe
  drawerVisible.value = true
  detail.value        = null
  detailLoading.value = true
  try {
    const res     = await getRecipeDetail(recipe.id)
    detail.value  = res.data ?? null
  } finally {
    detailLoading.value = false
  }
}

// ── Helpers ────────────────────────────────────────────────────────────────
const CUISINE_COLORS = {
  '川菜': '#EF4444', '粤菜': '#F59E0B', '鲁菜': '#3B82F6',
  '苏菜': '#10B981', '浙菜': '#8B5CF6', '湘菜': '#F97316',
  '闽菜': '#06B6D4', '徽菜': '#84CC16', '西北菜': '#EC4899',
  '东北菜': '#6366F1', '云贵菜': '#14B8A6', '京菜': '#F43F5E',
}

function cuisineColor(cuisine) {
  return CUISINE_COLORS[cuisine] ?? '#6B7280'
}

function difficultyType(d) {
  if (d === '简单') return 'success'
  if (d === '中等') return 'warning'
  if (d === '困难') return 'danger'
  return 'info'
}

function nutritionItems(nutrition) {
  if (!nutrition) return []
  try {
    const n = typeof nutrition === 'string' ? JSON.parse(nutrition) : nutrition
    const items = [
      { label: '蛋白质', key: 'proteinG', color: '#3B82F6' },
      { label: '脂肪',   key: 'fatG',     color: '#F59E0B' },
      { label: '碳水',   key: 'carbG',    color: '#10B981' },
      { label: '膳食纤维', key: 'fiberG', color: '#8B5CF6' },
    ]
    const max = Math.max(...items.map(i => n[i.key] ?? 0), 1)
    return items.map(i => ({
      label: i.label,
      color: i.color,
      val:   +(n[i.key] ?? 0).toFixed(1),
      pct:   Math.round(((n[i.key] ?? 0) / max) * 100),
    }))
  } catch {
    return []
  }
}
</script>

<style lang="scss" scoped>
.recipe-browser {
  padding-bottom: $space-12;
}

// ── Page Header ─────────────────────────────────────────────────────────────
.page-header {
  margin-bottom: $space-6;

  &__title {
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0 0 $space-1;
  }

  &__sub {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin: 0;
  }
}

// ── Search Bar ───────────────────────────────────────────────────────────────
.search-bar {
  display: flex;
  gap: $space-3;
  margin-bottom: $space-5;

  &__input {
    flex: 1;
  }
}

// ── Cuisine Tabs ─────────────────────────────────────────────────────────────
.cuisine-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: $space-2;
  margin-bottom: $space-6;
}

.cuisine-tabs-skeleton {
  margin-bottom: $space-6;
}

.cuisine-tab {
  display: inline-flex;
  align-items: center;
  gap: $space-1;
  padding: 6px $space-4;
  border-radius: $radius-full;
  border: 1px solid $color-border;
  background: $color-surface;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: $transition-fast;
  white-space: nowrap;

  &__count {
    font-size: $font-size-xs;
    color: $color-text-disabled;
  }

  &:hover:not(&--active) {
    border-color: $color-accent;
    color: $color-accent;
  }

  &--active {
    background: $color-accent;
    border-color: $color-accent;
    color: #fff;

    .cuisine-tab__count {
      color: rgba(255, 255, 255, 0.75);
    }
  }
}

// ── Recipe Grid ──────────────────────────────────────────────────────────────
.recipe-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: $space-4;
  margin-bottom: $space-6;
}

.recipe-card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
  padding: $space-4;
  cursor: pointer;
  position: relative;
  transition: box-shadow 0.2s, transform 0.2s;
  animation: card-in 0.35s ease both;
  animation-delay: var(--card-delay, 0ms);

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  &--skeleton {
    cursor: default;
    min-height: 160px;

    &:hover {
      box-shadow: none;
      transform: none;
    }
  }

  &__badge {
    position: absolute;
    top: $space-3;
    right: $space-3;
    padding: 2px $space-2;
    border-radius: $radius-full;
    font-size: 11px;
    color: #fff;
    font-weight: $font-weight-medium;
  }

  &__category {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-bottom: $space-1;
  }

  &__name {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 $space-3;
    line-height: 1.4;
    padding-right: 56px; // avoid overlap with badge
  }

  &__metrics {
    display: flex;
    gap: $space-3;
    margin-bottom: $space-3;
  }

  &__footer {
    display: flex;
    align-items: center;
  }
}

.metric {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: $font-size-xs;
  color: $color-text-secondary;

  .el-icon {
    font-size: 12px;
  }

  &__val {
    font-family: 'JetBrains Mono', monospace;
    font-size: 13px;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__unit {
    color: $color-text-disabled;
    font-size: 11px;
  }
}

@keyframes card-in {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

// ── Empty & Pagination ───────────────────────────────────────────────────────
.empty-state {
  padding: $space-12 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: $space-4;
}

// ── Detail Drawer ─────────────────────────────────────────────────────────────
.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $space-2;
  margin-bottom: $space-5;
}

.detail-stats {
  display: flex;
  gap: $space-4;
  background: $color-bg-secondary;
  border-radius: $radius-base;
  padding: $space-4;
  margin-bottom: $space-5;
}

.stat-item {
  flex: 1;
  text-align: center;

  &__val {
    font-family: 'JetBrains Mono', monospace;
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
  }

  &__label {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-top: 2px;
  }
}

.detail-section {
  margin-bottom: $space-5;

  &__title {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin-bottom: $space-3;
    padding-bottom: $space-2;
    border-bottom: 1px solid $color-border-light;
  }
}

.nutrition-bars {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.nutr-row {
  display: flex;
  align-items: center;
  gap: $space-2;

  &__label {
    width: 60px;
    font-size: $font-size-xs;
    color: $color-text-secondary;
    flex-shrink: 0;
  }

  &__bar {
    flex: 1;
  }

  &__val {
    width: 40px;
    text-align: right;
    font-size: $font-size-xs;
    font-family: 'JetBrains Mono', monospace;
    color: $color-text-secondary;
  }
}

.ingredient-list {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.ingredient-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $space-2 $space-3;
  background: $color-bg-secondary;
  border-radius: $radius-sm;
  font-size: $font-size-sm;

  &__name {
    color: $color-text-primary;
  }

  &__amount {
    color: $color-text-secondary;
    font-family: 'JetBrains Mono', monospace;
    font-size: $font-size-xs;
  }
}

.steps-list {
  padding-left: $space-5;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: $space-3;

  &__item {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    line-height: 1.6;
  }
}

.tips-text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  background: #FFFBEB;
  border-left: 3px solid #F59E0B;
  padding: $space-3 $space-4;
  border-radius: 0 $radius-sm $radius-sm 0;
  margin: 0;
  line-height: 1.6;
}
</style>
