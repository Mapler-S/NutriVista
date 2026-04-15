<template>
  <div class="not-found">
    <div class="not-found__code">404</div>
    <div class="not-found__title">页面不存在</div>
    <div class="not-found__desc">您访问的页面不存在或已被移动</div>
    <el-button type="primary" @click="$router.push('/dashboard')">
      返回仪表盘
    </el-button>
  </div>
</template>

<script setup></script>

<style lang="scss" scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $space-4;
  text-align: center;

  &__code {
    font-size: 96px;
    font-weight: $font-weight-bold;
    color: $color-border;
    font-family: $font-mono;
    line-height: 1;
  }

  &__title {
    font-size: $font-size-2xl;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  &__desc {
    color: $color-text-secondary;
    margin-bottom: $space-4;
  }
}
</style>
