<template>
  <div class="data-mgmt">

    <!-- ===== Header ===== -->
    <div class="dm-header">
      <div>
        <h1 class="dm-header__title">数据管理</h1>
        <p class="dm-header__sub">导出饮食记录 · 批量导入历史数据</p>
      </div>
    </div>

    <div class="dm-grid">

      <!-- ============================= -->
      <!-- Export Card                   -->
      <!-- ============================= -->
      <div class="dm-card">
        <div class="dm-card__head">
          <div class="dm-card__icon dm-card__icon--export">
            <el-icon size="22"><Download /></el-icon>
          </div>
          <div>
            <div class="dm-card__title">导出数据</div>
            <div class="dm-card__desc">将指定日期范围内的饮食记录下载到本地</div>
          </div>
        </div>

        <div class="dm-card__body">
          <el-form label-position="top" class="export-form">
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="exportRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                :clearable="false"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="导出格式">
              <el-radio-group v-model="exportFormat" class="format-group">
                <el-radio-button value="csv">
                  <el-icon><Document /></el-icon> CSV
                </el-radio-button>
                <el-radio-button value="json">
                  <el-icon><DataLine /></el-icon> JSON
                </el-radio-button>
              </el-radio-group>
              <div class="format-hint">
                <span v-if="exportFormat === 'csv'">推荐格式，可直接用 Excel 打开</span>
                <span v-else>结构化数据，适合程序处理</span>
              </div>
            </el-form-item>
          </el-form>

          <el-button
            type="primary"
            size="large"
            :loading="exporting"
            :disabled="!exportRange"
            class="action-btn"
            @click="doExport"
          >
            <el-icon><Download /></el-icon>
            {{ exporting ? '导出中...' : '开始导出' }}
          </el-button>
        </div>

        <!-- Recent exports log -->
        <div class="dm-card__log" v-if="exportLog.length">
          <div class="log-title">本次会话导出记录</div>
          <div v-for="(entry, i) in exportLog" :key="i" class="log-entry">
            <el-icon class="log-entry__icon"><SuccessFilled /></el-icon>
            <span>{{ entry.name }}</span>
            <span class="log-entry__time">{{ entry.time }}</span>
          </div>
        </div>
      </div>

      <!-- ============================= -->
      <!-- Import Card                   -->
      <!-- ============================= -->
      <div class="dm-card">
        <div class="dm-card__head">
          <div class="dm-card__icon dm-card__icon--import">
            <el-icon size="22"><Upload /></el-icon>
          </div>
          <div>
            <div class="dm-card__title">导入数据</div>
            <div class="dm-card__desc">上传 CSV 文件，批量将历史饮食记录导入系统</div>
          </div>
        </div>

        <div class="dm-card__body">
          <!-- Template download -->
          <div class="template-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>请先下载导入模板，按格式填写后上传</span>
            <el-button
              link
              type="primary"
              :loading="downloadingTemplate"
              @click="doDownloadTemplate"
            >下载模板</el-button>
          </div>

          <!-- Upload area -->
          <el-upload
            ref="uploadRef"
            drag
            :auto-upload="false"
            accept=".csv"
            :limit="1"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            :file-list="fileList"
            class="upload-area"
          >
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">
              <p class="upload-text__main">将 CSV 文件拖到此处，或 <em>点击上传</em></p>
              <p class="upload-text__sub">仅支持 .csv 格式，文件编码 UTF-8 / GBK 均可</p>
            </div>
          </el-upload>

          <el-button
            type="success"
            size="large"
            :loading="importing"
            :disabled="!pendingFile"
            class="action-btn"
            @click="doImport"
          >
            <el-icon><Upload /></el-icon>
            {{ importing ? '导入中...' : '开始导入' }}
          </el-button>
        </div>

        <!-- Import result -->
        <transition name="fade-slide">
          <div class="import-result" v-if="importResult">
            <div class="import-result__summary">
              <div class="result-stat result-stat--total">
                <span class="result-stat__num">{{ importResult.total }}</span>
                <span class="result-stat__label">总行数</span>
              </div>
              <div class="result-stat result-stat--success">
                <span class="result-stat__num">{{ importResult.success }}</span>
                <span class="result-stat__label">成功</span>
              </div>
              <div class="result-stat result-stat--skip">
                <span class="result-stat__num">{{ importResult.skipped }}</span>
                <span class="result-stat__label">跳过</span>
              </div>
            </div>

            <el-alert
              v-if="importResult.success > 0 && !importResult.skipped"
              type="success"
              :title="`全部 ${importResult.success} 条记录导入成功`"
              :closable="false"
              show-icon
            />
            <el-alert
              v-else-if="importResult.success > 0"
              type="warning"
              :title="`成功导入 ${importResult.success} 条，跳过 ${importResult.skipped} 条`"
              :closable="false"
              show-icon
            />
            <el-alert
              v-else
              type="error"
              title="导入失败，请检查文件格式"
              :closable="false"
              show-icon
            />

            <div class="error-list" v-if="importResult.errors?.length">
              <div class="error-list__title">错误详情：</div>
              <div v-for="(err, i) in importResult.errors.slice(0, 10)" :key="i" class="error-item">
                {{ err }}
              </div>
              <div v-if="importResult.errors.length > 10" class="error-item error-item--more">
                … 还有 {{ importResult.errors.length - 10 }} 条错误
              </div>
            </div>
          </div>
        </transition>
      </div>

    </div>

    <!-- ===== Format Reference ===== -->
    <div class="dm-card dm-card--wide">
      <div class="dm-card__head">
        <div class="dm-card__icon dm-card__icon--info">
          <el-icon size="22"><QuestionFilled /></el-icon>
        </div>
        <div>
          <div class="dm-card__title">CSV 格式说明</div>
          <div class="dm-card__desc">导入文件需与导出文件格式保持一致</div>
        </div>
      </div>
      <div class="dm-card__body">
        <el-table :data="formatSpec" size="small" border class="spec-table">
          <el-table-column prop="col" label="列名" width="160" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="required" label="是否必填" width="100">
            <template #default="{ row }">
              <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                {{ row.required ? '必填' : '可选' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="example" label="示例" width="140" />
          <el-table-column prop="note" label="说明" />
        </el-table>

        <div class="valid-meal-types">
          <span class="vmt__label">有效餐次值：</span>
          <el-tag v-for="t in mealTypes" :key="t" class="vmt__tag">{{ t }}</el-tag>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import {
  Download, Upload, Document, DataLine,
  SuccessFilled, InfoFilled, UploadFilled, QuestionFilled,
} from '@element-plus/icons-vue'
import { exportData, importData, downloadTemplate } from '@/api/data'

// ===== Export =====
const exportRange  = ref([
  dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD'),
])
const exportFormat = ref('csv')
const exporting    = ref(false)
const exportLog    = ref([])

async function doExport() {
  if (!exportRange.value) return
  const [from, to] = exportRange.value
  exporting.value = true
  try {
    const blob = await exportData(from, to, exportFormat.value)
    triggerDownload(blob, `nutrivista_${from}_${to}.${exportFormat.value}`)
    exportLog.value.unshift({
      name: `nutrivista_${from}_${to}.${exportFormat.value}`,
      time: dayjs().format('HH:mm:ss'),
    })
    ElMessage.success('导出成功')
  } catch {
    // error shown by interceptor
  } finally {
    exporting.value = false
  }
}

// ===== Template download =====
const downloadingTemplate = ref(false)

async function doDownloadTemplate() {
  downloadingTemplate.value = true
  try {
    const blob = await downloadTemplate()
    triggerDownload(blob, 'nutrivista_import_template.csv')
  } catch {
    // error shown by interceptor
  } finally {
    downloadingTemplate.value = false
  }
}

// ===== Import =====
const uploadRef    = ref(null)
const fileList     = ref([])
const pendingFile  = ref(null)
const importing    = ref(false)
const importResult = ref(null)

function handleFileChange(file) {
  pendingFile.value  = file.raw
  importResult.value = null
}

function handleExceed() {
  ElMessage.warning('每次只能上传一个文件，请先移除已选文件')
}

async function doImport() {
  if (!pendingFile.value) return
  importing.value = true
  importResult.value = null
  try {
    const res = await importData(pendingFile.value)
    importResult.value = res.data
    // Clear upload list after successful request
    fileList.value = []
    pendingFile.value = null
    uploadRef.value?.clearFiles()
  } catch {
    // error shown by interceptor
  } finally {
    importing.value = false
  }
}

// ===== Helpers =====
function triggerDownload(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a   = document.createElement('a')
  a.href     = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// ===== Static data =====
const mealTypes = ['早餐', '上午加餐', '午餐', '下午茶', '晚餐', '夜宵']

const formatSpec = [
  { col: '日期',        type: 'YYYY-MM-DD', required: true,  example: '2026-03-31', note: '饮食记录日期' },
  { col: '餐次',        type: '字符串',      required: true,  example: '早餐',       note: '见下方有效餐次列表' },
  { col: '食物名称',    type: '字符串',      required: true,  example: '鸡蛋',       note: '须与食物库中的中文名一致' },
  { col: '重量(g)',     type: '数字',        required: true,  example: '60',         note: '实际摄入克重，必须大于 0' },
  { col: '能量(kcal)', type: '数字',        required: false, example: '85.8',       note: '可留空，导入时从食物库重新计算' },
  { col: '蛋白质(g)',  type: '数字',        required: false, example: '7.1',        note: '可留空' },
  { col: '脂肪(g)',    type: '数字',        required: false, example: '5.9',        note: '可留空' },
  { col: '碳水化合物(g)', type: '数字',     required: false, example: '0.3',        note: '可留空' },
  { col: '膳食纤维(g)', type: '数字',       required: false, example: '0.0',        note: '可留空' },
]
</script>

<style lang="scss" scoped>
$space-xs:  4px;
$space-sm:  8px;
$space-md:  16px;
$space-lg:  24px;
$space-xl:  32px;
$radius-md: 12px;
$radius-lg: 16px;

.data-mgmt {
  padding: $space-xl;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: $space-xl;
}

// ===== Header =====
.dm-header {
  &__title {
    font-size: 24px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 4px;
  }
  &__sub {
    font-size: 14px;
    color: #8a8fa8;
    margin: 0;
  }
}

// ===== Grid =====
.dm-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-xl;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

// ===== Card =====
.dm-card {
  background: #fff;
  border-radius: $radius-lg;
  border: 1px solid #e8eaf0;
  overflow: hidden;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06); }

  &--wide { grid-column: 1 / -1; }

  &__head {
    display: flex;
    align-items: flex-start;
    gap: $space-md;
    padding: $space-lg $space-xl;
    border-bottom: 1px solid #f0f2f8;
    background: #fafbff;
  }

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    color: #fff;

    &--export { background: linear-gradient(135deg, #4f8ef7, #2563eb); }
    &--import { background: linear-gradient(135deg, #34d399, #059669); }
    &--info   { background: linear-gradient(135deg, #a78bfa, #7c3aed); }
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a2e;
    margin-bottom: 2px;
  }

  &__desc {
    font-size: 13px;
    color: #8a8fa8;
  }

  &__body {
    padding: $space-xl;
    display: flex;
    flex-direction: column;
    gap: $space-md;
  }

  &__log {
    padding: $space-md $space-xl;
    border-top: 1px solid #f0f2f8;
    background: #fafbff;
  }
}

// ===== Export form =====
.export-form {
  :deep(.el-form-item) { margin-bottom: 0; }
  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 500;
    color: #4a4f6a;
    padding-bottom: 6px;
  }
}

.format-group {
  :deep(.el-radio-button__inner) {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.format-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #8a8fa8;
}

// ===== Template tip =====
.template-tip {
  display: flex;
  align-items: center;
  gap: $space-sm;
  padding: 10px $space-md;
  background: #eff6ff;
  border-radius: 8px;
  font-size: 13px;
  color: #4a4f6a;

  .el-icon { color: #3b82f6; flex-shrink: 0; }
  .el-button { margin-left: auto; padding: 0; font-size: 13px; }
}

// ===== Upload area =====
.upload-area {
  :deep(.el-upload-dragger) {
    width: 100%;
    padding: $space-xl $space-md;
    border-radius: $radius-md;
    border-color: #c7d2fe;
    background: #f8faff;
    transition: all 0.2s;

    &:hover, &.is-dragover {
      border-color: #4f8ef7;
      background: #eff6ff;
    }
  }

  :deep(.el-upload) { width: 100%; }
}

.upload-icon {
  font-size: 40px;
  color: #4f8ef7;
  margin-bottom: $space-sm;
}

.upload-text {
  &__main {
    font-size: 14px;
    color: #4a4f6a;
    margin: 0 0 4px;
    em { color: #4f8ef7; font-style: normal; }
  }
  &__sub {
    font-size: 12px;
    color: #8a8fa8;
    margin: 0;
  }
}

// ===== Action button =====
.action-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

// ===== Export log =====
.log-title {
  font-size: 12px;
  font-weight: 500;
  color: #8a8fa8;
  margin-bottom: $space-sm;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.log-entry {
  display: flex;
  align-items: center;
  gap: $space-sm;
  font-size: 13px;
  color: #4a4f6a;
  padding: 4px 0;

  &__icon { color: #10b981; flex-shrink: 0; }
  &__time {
    margin-left: auto;
    font-size: 12px;
    color: #8a8fa8;
    font-variant-numeric: tabular-nums;
  }
}

// ===== Import result =====
.import-result {
  display: flex;
  flex-direction: column;
  gap: $space-md;
  padding-top: $space-sm;
  border-top: 1px solid #f0f2f8;

  &__summary {
    display: flex;
    gap: $space-md;
  }
}

.result-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-md $space-sm;
  border-radius: 10px;
  text-align: center;

  &--total   { background: #f0f2f8; }
  &--success { background: #ecfdf5; }
  &--skip    { background: #fff7ed; }

  &__num {
    font-size: 26px;
    font-weight: 700;
    line-height: 1;

    .result-stat--total &   { color: #4a4f6a; }
    .result-stat--success & { color: #059669; }
    .result-stat--skip &    { color: #d97706; }
  }

  &__label {
    font-size: 12px;
    color: #8a8fa8;
    margin-top: 4px;
  }
}

.error-list {
  background: #fff5f5;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: $space-md;

  &__title {
    font-size: 13px;
    font-weight: 500;
    color: #dc2626;
    margin-bottom: $space-sm;
  }
}

.error-item {
  font-size: 12px;
  color: #7f1d1d;
  padding: 3px 0;
  border-bottom: 1px solid #fecaca;

  &:last-child { border-bottom: none; }
  &--more { color: #dc2626; font-style: italic; }
}

// ===== Format spec table =====
.spec-table {
  width: 100%;
  :deep(.el-table__header-wrapper th) {
    background: #f8faff;
    color: #4a4f6a;
    font-weight: 500;
  }
}

.valid-meal-types {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $space-sm;
  margin-top: $space-md;
  padding: $space-md;
  background: #f8faff;
  border-radius: 8px;
}

.vmt__label {
  font-size: 13px;
  color: #4a4f6a;
  font-weight: 500;
  flex-shrink: 0;
}

.vmt__tag {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1d4ed8;
}

// ===== Transitions =====
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
