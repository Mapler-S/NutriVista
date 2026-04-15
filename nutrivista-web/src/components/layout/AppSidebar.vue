<template>
  <aside class="app-sidebar">
    <nav class="sidebar-nav">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ 'nav-item--active': $route.path.startsWith(item.path) }"
      >
        <el-icon class="nav-item__icon"><component :is="item.icon" /></el-icon>
        <span class="nav-item__label">{{ item.label }}</span>
        <div class="nav-item__indicator" />
      </router-link>
    </nav>

    <!-- 底部版本信息 -->
    <div class="sidebar-footer">
      <div class="sidebar-footer__version">v1.0.0</div>
      <div class="sidebar-footer__copy">NutriVista © 2024</div>
    </div>
  </aside>
</template>

<script setup>
import { DataLine, EditPen, Calendar, FolderOpened, Grid, Bowl } from '@element-plus/icons-vue'

const navItems = [
  { path: '/dashboard',    label: '数据仪表盘', icon: DataLine },
  { path: '/meal-logger',  label: '饮食记录',   icon: EditPen },
  { path: '/calendar',     label: '饮食日历',   icon: Calendar },
  { path: '/food-library', label: '食物库',     icon: Grid },
  { path: '/recipes',      label: '菜谱浏览',   icon: Bowl },
  { path: '/data',         label: '数据管理',   icon: FolderOpened },
]
</script>

<style lang="scss" scoped>
.app-sidebar {
  position: fixed;
  left: 0;
  top: $header-height;
  bottom: 0;
  width: $sidebar-width;
  background: $color-surface;
  border-right: 1px solid $color-border;
  display: flex;
  flex-direction: column;
  z-index: $z-sticky;
  padding: $space-4 $space-3;
  overflow-y: auto;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: $space-1;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: 10px $space-3;
  border-radius: $radius-base;
  color: $color-text-secondary;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  text-decoration: none;
  position: relative;
  transition: $transition-fast;
  cursor: pointer;

  &__icon {
    font-size: 18px;
    flex-shrink: 0;
    transition: $transition-fast;
  }

  &__label {
    flex: 1;
  }

  &__indicator {
    width: 4px;
    height: 0;
    background: $color-accent;
    border-radius: $radius-full;
    transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover:not(&--active) {
    background: $color-bg-secondary;
    color: $color-text-primary;
  }

  &--active {
    background: $color-accent-light;
    color: $color-accent;

    .nav-item__indicator {
      height: 100%;
      position: absolute;
      right: 0;
      top: 0;
    }

    .nav-item__icon {
      color: $color-accent;
    }
  }
}

.sidebar-footer {
  padding-top: $space-4;
  border-top: 1px solid $color-border-light;
  text-align: center;

  &__version,
  &__copy {
    font-size: $font-size-xs;
    color: $color-text-disabled;
    line-height: 1.8;
  }
}
</style>
