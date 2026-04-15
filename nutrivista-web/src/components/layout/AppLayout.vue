<template>
  <div class="app-layout">
    <AppHeader />
    <div class="app-body">
      <AppSidebar />
      <main class="app-main">
        <div class="app-content">
          <router-view v-slot="{ Component }">
            <transition name="slide-up" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: $color-bg-primary;
}

.app-body {
  display: flex;
  flex: 1;
  padding-top: $header-height;
}

.app-main {
  flex: 1;
  min-width: 0;
  margin-left: $sidebar-width;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.app-content {
  max-width: $content-max-width;
  margin: 0 auto;
  padding: $space-8 $space-8;

  @media (max-width: 1024px) {
    padding: $space-6 $space-6;
  }

  @media (max-width: 768px) {
    padding: $space-4 $space-4;
  }
}
</style>
