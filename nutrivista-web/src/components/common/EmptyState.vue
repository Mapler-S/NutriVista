<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <slot name="icon">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <circle cx="40" cy="40" r="40" fill="#F3F4F6"/>
          <path d="M28 30h24M28 38h24M28 46h16" stroke="#D1D5DB" stroke-width="2.5" stroke-linecap="round"/>
          <circle cx="54" cy="50" r="10" fill="#EEF2FF" stroke="#818CF8" stroke-width="2"/>
          <path d="M50 50h8M54 46v8" stroke="#818CF8" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </slot>
    </div>
    <div class="empty-state__title">{{ title }}</div>
    <div class="empty-state__desc">{{ description }}</div>
    <div v-if="$slots.action" class="empty-state__action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '暂无数据',
  },
  description: {
    type: String,
    default: '还没有相关记录，开始添加吧',
  },
})
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $space-12 $space-8;
  text-align: center;

  &__icon {
    margin-bottom: $space-5;
    opacity: 0.8;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    margin-bottom: $space-2;
  }

  &__desc {
    font-size: $font-size-sm;
    color: $color-text-tertiary;
    max-width: 280px;
    line-height: $line-height-base;
  }

  &__action {
    margin-top: $space-6;
  }
}
</style>
