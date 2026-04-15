<template>
  <header class="app-header">
    <div class="app-header__inner">
      <!-- Logo -->
      <div class="app-header__logo" @click="$router.push('/dashboard')">
        <div class="logo-icon">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <circle cx="14" cy="14" r="14" fill="#4F46E5" fill-opacity="0.1"/>
            <path d="M8 14C8 10.686 10.686 8 14 8C17.314 8 20 10.686 20 14" stroke="#4F46E5" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="14" cy="14" r="3" fill="#4F46E5"/>
            <path d="M14 11V8M11.5 12L9 10M16.5 12L19 10" stroke="#4F46E5" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="logo-text">NutriVista</span>
      </div>

      <!-- Spacer -->
      <div class="flex-1" />

      <!-- 右侧区域 -->
      <div class="app-header__right">
        <!-- 快捷操作：记录今日饮食 -->
        <el-button
          type="primary"
          size="small"
          :icon="Plus"
          @click="$router.push('/meal-logger')"
        >
          记录饮食
        </el-button>

        <!-- 用户头像（预留） -->
        <el-avatar
          class="user-avatar"
          :size="34"
          src=""
          @error="() => true"
        >
          <el-icon><User /></el-icon>
        </el-avatar>
      </div>
    </div>
  </header>
</template>

<script setup>
import { Plus, User } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: $header-height;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid $color-border;
  z-index: $z-fixed;
  transition: $transition-base;

  &__inner {
    display: flex;
    align-items: center;
    height: 100%;
    padding: 0 $space-6;
    max-width: 100%;
  }

  &__logo {
    display: flex;
    align-items: center;
    gap: $space-2;
    cursor: pointer;
    user-select: none;

    .logo-text {
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      letter-spacing: -0.3px;
    }

    &:hover .logo-text {
      color: $color-accent;
    }
  }

  &__right {
    display: flex;
    align-items: center;
    gap: $space-3;
  }
}

.user-avatar {
  cursor: pointer;
  background: $color-bg-secondary;
  color: $color-text-secondary;
  transition: $transition-fast;

  &:hover {
    transform: scale(1.05);
  }
}

.flex-1 { flex: 1; }
</style>
