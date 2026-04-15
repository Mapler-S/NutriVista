/**
 * NutriVista ECharts 自定义主题
 * 风格：极简、高级感、莫兰迪色系
 */

export const CHART_COLORS = [
  '#6366F1', // 主色 - 靛蓝
  '#8B5CF6', // 紫色
  '#EC4899', // 粉色
  '#F43F5E', // 玫红
  '#F97316', // 橙色
  '#EAB308', // 黄色
  '#22C55E', // 绿色
  '#14B8A6', // 青绿
  '#06B6D4', // 青色
  '#3B82F6', // 蓝色
]

export const NUTRIENT_COLORS = {
  energy: '#6366F1',
  protein: '#22C55E',
  fat: '#F97316',
  carbohydrate: '#3B82F6',
  fiber: '#14B8A6',
  sugar: '#EC4899',
}

const nutriVistaTheme = {
  color: CHART_COLORS,

  backgroundColor: 'transparent',

  textStyle: {
    fontFamily: "'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif",
    fontSize: 13,
    color: '#6B7280',
  },

  title: {
    textStyle: {
      color: '#111827',
      fontSize: 15,
      fontWeight: '600',
    },
    subtextStyle: {
      color: '#9CA3AF',
      fontSize: 12,
    },
  },

  legend: {
    textStyle: {
      color: '#6B7280',
      fontSize: 12,
    },
    icon: 'roundRect',
    itemWidth: 10,
    itemHeight: 10,
    itemGap: 16,
  },

  tooltip: {
    backgroundColor: '#FFFFFF',
    borderColor: '#E5E7EB',
    borderWidth: 1,
    borderRadius: 10,
    padding: [10, 14],
    textStyle: {
      color: '#111827',
      fontSize: 13,
    },
    extraCssText: 'box-shadow: 0 10px 25px rgba(0,0,0,0.1); backdrop-filter: blur(8px);',
  },

  grid: {
    containLabel: true,
    left: '2%',
    right: '2%',
    top: '12%',
    bottom: '8%',
  },

  // 坐标轴
  categoryAxis: {
    axisLine: {
      lineStyle: { color: '#E5E7EB' },
    },
    axisTick: { show: false },
    axisLabel: {
      color: '#9CA3AF',
      fontSize: 12,
    },
    splitLine: { show: false },
  },

  valueAxis: {
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: {
      color: '#9CA3AF',
      fontSize: 12,
    },
    splitLine: {
      lineStyle: {
        color: '#F3F4F6',
        type: 'dashed',
      },
    },
  },

  // Line Chart
  line: {
    smooth: true,
    symbolSize: 6,
    symbol: 'circle',
    lineStyle: { width: 2.5 },
    emphasis: {
      scale: true,
      lineStyle: { width: 3 },
    },
  },

  // Bar Chart
  bar: {
    barMaxWidth: 40,
    itemStyle: {
      borderRadius: [4, 4, 0, 0],
    },
  },

  // Pie Chart
  pie: {
    label: {
      color: '#6B7280',
      fontSize: 12,
    },
    itemStyle: {
      borderWidth: 2,
      borderColor: '#FFFFFF',
    },
  },

  // Radar Chart
  radar: {
    splitNumber: 4,
    name: {
      textStyle: {
        color: '#6B7280',
        fontSize: 12,
      },
    },
    splitLine: {
      lineStyle: { color: '#F3F4F6' },
    },
    splitArea: {
      areaStyle: {
        color: ['rgba(243,244,246,0.3)', 'rgba(243,244,246,0.1)'],
      },
    },
    axisLine: {
      lineStyle: { color: '#E5E7EB' },
    },
  },
}

export default nutriVistaTheme
