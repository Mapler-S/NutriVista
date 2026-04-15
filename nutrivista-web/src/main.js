import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as echarts from 'echarts'
import nutriVistaTheme from '@/assets/styles/echarts-theme.js'
import router from './router'
import App from './App.vue'

// 全局样式（必须在 Element Plus 之前导入以确保覆盖优先级）
import '@/assets/styles/global.scss'

// ===== ECharts 主题注册 =====
echarts.registerTheme('nutrivista', nutriVistaTheme)

// ===== 创建应用实例 =====
const app = createApp(App)

// ===== 注册插件 =====
app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default',
})

// ===== 注册 Element Plus 图标（全局）=====
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// ===== 全局属性：ECharts 实例工厂 =====
app.config.globalProperties.$echarts = echarts

// ===== 挂载 =====
app.mount('#app')
