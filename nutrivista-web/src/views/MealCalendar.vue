<template>
  <div class="meal-calendar">
    <h1 class="page-title">饮食日历</h1>
    <p class="page-sub">完整日历视图将在 Phase 4 实现</p>
    <EmptyState
      title="饮食日历开发中"
      description="Phase 4 将实现：月/周视图、每日营养达标状态、迷你图表、点击跳转记录页等功能"
    >
      <template #action>
        <el-button @click="$router.push('/dashboard')">返回仪表盘</el-button>
      </template>
    </EmptyState>
  </div>
</template>

<script setup>
import EmptyState from '@/components/common/EmptyState.vue'
</script>

<style lang="scss" scoped>
.page-title {
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  margin-bottom: $space-1;
}
.page-sub {
  color: $color-text-secondary;
  margin-bottom: $space-8;
}
</style>
