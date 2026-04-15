# NutriVista — 个人饮食营养大数据可视化分析系统

NutriVista 是一个全栈饮食营养可视化分析系统，帮助用户记录每日饮食、追踪营养摄入、发现饮食模式，并通过 AI 推荐菜谱。

**前端已部署至 GitHub Pages，无需自行搭建**：[https://mapler-s.github.io/NutriVista/](https://mapler-s.github.io/NutriVista/)

只需在本地部署后端服务即可与前端交互。

---

## 目录

- [本地部署指南](#本地部署指南)
- [可视化方案：旨在解答什么问题](#可视化方案旨在解答什么问题)
- [设计决策的依据](#设计决策的依据)
- [外部数据源引用](#外部数据源引用)
- [开发流程概述](#开发流程概述)

---

## 本地部署指南

### 环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 推荐 JDK 24 |
| Maven | 3.9+ | 后端构建 |
| MySQL | 8.0+ | 主数据库 |
| Docker & Docker Compose | 最新版 | 运行 Milvus 向量数据库（AI 菜谱推荐功能所需） |

### 第一步：克隆仓库

```bash
git clone https://github.com/Mapler-S/NutriVista.git
cd NutriVista
```

### 第二步：配置 MySQL 数据库

1. 登录 MySQL，创建数据库：

```sql
CREATE DATABASE nutrivista DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 数据表和食物数据由 Flyway 自动迁移创建，无需手动导入 SQL。

### 第三步：创建后端配置文件

后端配置文件 `application.yml` 未纳入版本管理（含敏感信息），需手动创建：

```bash
# 在以下路径创建配置文件
nutrivista-server/src/main/resources/application.yml
```

写入以下内容，并根据实际情况修改：

```yaml
spring:
  application:
    name: nutrivista-server
  datasource:
    url: jdbc:mysql://localhost:3306/nutrivista?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root          # 改为你的 MySQL 用户名
    password: your_password # 改为你的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: 8080

# ===== AI 功能配置（菜谱推荐需要） =====
# 如果不需要 AI 菜谱推荐功能，可以跳过以下配置

# LLM 大语言模型（DeepSeek）
llm:
  provider: deepseek
  api-key: your_deepseek_api_key   # 从 https://platform.deepseek.com 获取
  model: deepseek-chat
  base-url: https://api.deepseek.com
  max-tokens: 2048
  temperature: 0.7

# 文本向量化（阿里云 DashScope）
embedding:
  provider: dashscope
  api-key: your_dashscope_api_key  # 从 https://dashscope.console.aliyun.com 获取
  model: text-embedding-v3
  dimension: 1024
  batch-size: 10

# Milvus 向量数据库
milvus:
  host: localhost
  port: 19530
  collection-name: recipe_vectors
  dimension: 1024

# 菜谱数据初始化（首次运行设为 true，向量化完成后改回 false）
recipe:
  init:
    enabled: false

# 营养素每日推荐摄入量（DRI），可根据个人情况调整
nutrivista:
  dris:
    energy-kcal: 2000
    protein-g: 65
    fat-g: 60
    carbohydrate-g: 300
    fiber-g: 25
    vitamin-a-ug: 800
    vitamin-c-mg: 100
    vitamin-b1-mg: 1.4
    vitamin-b2-mg: 1.4
    calcium-mg: 800
    iron-mg: 12
    zinc-mg: 12.5
    selenium-ug: 60
```

### 第四步：启动 Milvus（可选，AI 菜谱推荐功能需要）

```bash
cd docker
docker compose up -d
```

等待所有服务（etcd、minio、milvus）启动完成。首次启动后，将 `application.yml` 中的 `recipe.init.enabled` 设为 `true`，启动后端以触发菜谱数据向量化，完成后改回 `false`。

### 第五步：启动后端

```bash
cd nutrivista-server
mvn spring-boot:run
```

后端启动后运行在 `http://localhost:8080`，Flyway 会自动创建所有数据表并导入 8000+ 条食物数据。

### 第六步：访问前端

打开浏览器，访问 [https://mapler-s.github.io/NutriVista/](https://mapler-s.github.io/NutriVista/) 即可开始使用。

> 如需本地运行前端进行开发调试：
> ```bash
> cd nutrivista-web
> npm install
> npm run dev
> ```
> 访问 `http://localhost:5173/NutriVista/`

---

## 可视化方案：旨在解答什么问题

NutriVista 的可视化系统围绕以下核心问题展开设计：

### 1. 我今天吃得够不够？——每日营养摄入总览

用户最直接的问题是"今天吃了多少热量""蛋白质够不够"。仪表盘页面通过**环形进度图**展示每日能量摄入占目标的百分比，通过**进度条**展示蛋白质、脂肪、碳水三大宏量营养素的达标情况，让用户一眼看出当天饮食是否达标。

### 2. 三大营养素比例是否合理？——宏量营养素结构分析

单看总量不够，比例同样重要。**环形饼图**将蛋白质、脂肪、碳水的热量占比可视化呈现，帮助用户判断饮食结构是否均衡，是否存在脂肪占比过高或蛋白质不足等问题。

### 3. 微量营养素有没有短板？——营养素充足率评估

维生素和矿物质的缺乏往往不易察觉。**水平条形图**将 13 种关键营养素（维生素 A/C/B1/B2、钙、铁、锌、硒等）的实际摄入与中国居民膳食营养素参考摄入量（DRI）对比，用**三色编码**标识状态：绿色（达标 ≥100%）、黄色（需关注 50-99%）、红色（不足 <50%），帮助用户快速定位营养短板。

### 4. 我的饮食习惯有什么趋势？——时间维度的纵向分析

单日数据不足以反映饮食习惯。**面积折线图**展示 7/14/30 天的热量摄入趋势，叠加目标参考线（2000 kcal），让用户观察自己的饮食是否稳定、是否有逐渐偏离目标的趋势。

### 5. 哪顿饭吃得最多？吃得最多的食物是什么？——餐次与食物频率分析

**柱状图**呈现各餐次（早/中/晚/加餐）的平均热量分布，揭示是否存在"早饭不吃、晚饭暴食"等不良模式。另一组**水平柱状图**统计食用频率最高的 Top-10 食物，反映用户的食物多样性。

### 6. AI 菜谱推荐效果如何？——推荐与采纳分析

**雷达图**按菜系维度展示推荐次数与实际采纳次数的对比，帮助用户了解自己的口味偏好。**折线图**展示每日推荐与采纳趋势，**排行榜**展示最受欢迎的 Top-10 菜谱。

---

## 设计决策的依据

### 可视化编码选择

| 数据特征 | 选用的可视化编码 | 选择理由 |
|----------|------------------|----------|
| 单一指标占比（能量达标率） | 环形进度图 | 符合"目标完成度"的认知隐喻，比数字更直观 |
| 部分与整体关系（三大营养素比例） | 环形饼图 | 类别仅 3 个，饼图可有效传达各部分占比，中心区域复用展示总量 |
| 多维度达标对比（营养素充足率） | 水平条形图 + 三色编码 | 条形图适合比较多个类别的数值大小；颜色编码减少认知负担，无需逐一读数 |
| 时间序列（摄入趋势） | 面积折线图 + 参考线 | 折线图是时间序列可视化的标准选择，面积填充增强趋势感知，参考线提供锚点 |
| 分类计数（餐次分布/食物排行） | 柱状图 / 水平柱状图 | 离散类别间的数值比较，柱状图是最自然的选择 |
| 多维度环形对比（菜系偏好） | 雷达图 | 适合展示 6-8 个维度的整体轮廓，直观反映偏好分布 |

### 考虑过的替代方案

- **营养素充足率**：曾考虑使用**雷达图**，但当营养素数量达到 13 种时，雷达图轴线过密、数值难以精确读取。水平条形图在可读性和信息密度上更优。
- **三大营养素比例**：曾考虑**堆叠柱状图**，但仅 3 个类别时饼图的"占比"语义更强，且环形饼图中心可复用展示总热量，信息密度更高。
- **摄入趋势**：曾考虑**柱状图**按天展示，但连续折线更能体现趋势的连续性和波动方向。
- **前端图表库**：在 ECharts、Chart.js、D3.js 之间做了比较。D3.js 灵活性最强但开发成本高；Chart.js 轻量但定制能力有限；ECharts 在中文环境下的生态支持最好、图表类型丰富、交互功能开箱即用（tooltip、legend、dataZoom），综合考虑选择了 ECharts。

### 交互技术

- **Tooltip 悬浮提示**：所有图表均支持鼠标悬浮显示详细数值，避免在图表上堆叠过多标签。
- **Legend 图例交互**：支持点击图例切换数据系列的显示/隐藏，方便用户聚焦关注的指标。
- **时间范围切换**：趋势图支持 7/14/30 天切换，适应不同的分析粒度。
- **Hash 路由**：使用 `createWebHashHistory` 而非 HTML5 History，确保 GitHub Pages 上刷新不会出现 404。

---

## 外部数据源引用

### 1. 美国农业部食品数据中心（USDA FoodData Central）

- **来源**：[https://fdc.nal.usda.gov/](https://fdc.nal.usda.gov/)
- **用途**：提供食物的营养成分数据，包括每 100g 可食部分的能量、蛋白质、脂肪、碳水化合物、膳食纤维、维生素、矿物质等 40+ 项营养素指标。
- **范围**：系统中标记为 `USDA` 数据源的食物条目，主要补充国内数据库未覆盖的食材（如藜麦、芦笋、树莓、百香果等）。

### 2. XiaChuFang Recipe Corpus（下厨房菜谱语料库）

- **来源**：基于下厨房平台的公开菜谱数据集
- **用途**：提供 200+ 道中式菜谱的结构化信息，包括菜名、菜系、烹饪步骤、食材清单、营养估算等，用于 AI 菜谱推荐功能的知识库。
- **处理方式**：菜谱数据经清洗后存储于 MySQL，同时通过 DashScope 文本向量化服务生成 1024 维语义向量，存入 Milvus 向量数据库，支持基于自然语言的语义检索。

### 3. 中国食物成分表（CNF）

- **用途**：系统中大部分食物（8000+ 条）的营养数据来源，标记为 `CNF` 数据源。

---

## 开发流程概述

### 总体耗时

项目总开发时间约 **120-150 工时**，分布如下：

| 阶段 | 耗时估算 | 占比 |
|------|----------|------|
| 需求分析与原型设计 | ~10 工时 | 7% |
| 数据库设计与食物数据处理 | ~20 工时 | 14% |
| 后端 API 开发（基础 CRUD） | ~25 工时 | 18% |
| 前端页面与可视化开发 | ~35 工时 | 25% |
| AI 菜谱推荐（RAG 管线） | ~30 工时 | 21% |
| 食材模糊匹配与数据对齐 | ~15 工时 | 10% |
| 联调、Bug 修复与部署 | ~10 工时 | 7% |

### 耗时最多的环节

1. **前端可视化开发（~35 工时）**：这是耗时最多的部分。每个图表都需要反复调试 ECharts 配置项（颜色、间距、响应式适配、tooltip 格式化），同时要考虑数据为空时的降级展示。仪表盘页面包含 7 个独立图表组件，每个都需要处理数据转换、加载状态和异常状态。

2. **AI 菜谱推荐的 RAG 管线（~30 工时）**：涉及多个外部服务的集成（DeepSeek LLM、DashScope Embedding、Milvus 向量数据库），每一层都有各自的调试难点。Milvus 的 Schema 设计和过滤查询语法调试花了不少时间；Prompt 工程需要反复试验才能让 LLM 输出稳定的结构化推荐结果。

3. **食材模糊匹配（~15 工时）**：将菜谱中的食材名称（如"去骨鸡腿肉"）与数据库中的标准食物名称（如"鸡腿肉（生）"）对齐，是一个看似简单实则棘手的问题。最终实现了 7 层渐进式匹配策略（精确匹配 → 全文索引 → 双向子串 → 修饰词剥离 → 3 字符滑动窗口 → 关键字符重叠评分），每一层都是为了解决上一层漏匹配的实际案例而添加的。

### 开发过程评述

- **技术栈选型**先于编码，确定 Spring Boot 3 + Vue 3 + ECharts + MySQL 的组合后，开发效率显著提高——Element Plus 提供了成熟的 UI 组件，ECharts 的声明式配置减少了大量底层绑定代码。
- **数据是地基**。花在清洗食物数据、统一单位、填补缺失营养素字段上的时间超出预期，但这些工作直接决定了可视化结果的可信度。
- **AI 功能是锦上添花，不是核心**。系统设计保证了即使不配置 LLM/Milvus，基础的饮食记录和营养分析功能也能完整运行。
- **迭代式开发**。先完成最小可用版本（饮食记录 + 仪表盘），再逐步添加菜谱浏览、AI 推荐、数据导入导出等功能，每次迭代都保持系统可运行状态。
