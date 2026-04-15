<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup>
// App.vue — 根组件，仅负责路由渲染和全局过渡
</script>

<style>
#app {
  min-height: 100vh;
  background-color: var(--color-bg-primary);
}
</style>
