<template>
   <el-dialog
      v-model="visible"
      width="720px"
      append-to-body
      draggable
      :close-on-click-modal="false"
      class="rd-dialog icon-select-dialog"
      @open="onOpen"
   >
      <template #header>
         <div class="rd-detail-header">
            <div class="rd-detail-header-icon">
               <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M12 16v-4M12 8h.01" />
               </svg>
            </div>
            <span class="rd-detail-header-title">选择菜单图标</span>
            <div class="rd-detail-header-sub" v-if="activeIconLocal">
               <div class="rd-detail-header-divider"></div>
               <span class="rd-detail-header-no">当前：{{ activeIconLocal }}</span>
            </div>
         </div>
      </template>

      <div class="icon-dialog-body">
         <!-- 搜索 + 上传 -->
         <div class="icon-toolbar">
            <el-input
               v-model="iconName"
               class="icon-search"
               clearable
               placeholder="请输入图标名称搜索"
               @clear="filterIcons"
               @input="filterIcons"
            >
               <template #prefix>
                  <el-icon><Search /></el-icon>
               </template>
            </el-input>
            <el-tooltip placement="bottom" :show-after="300">
               <template #content>
                  <div style="line-height: 1.8">
                     <div style="font-weight: 600; margin-bottom: 4px;">上传要求</div>
                     <div>• 格式：SVG 矢量图</div>
                     <div>• 大小：不超过 100KB</div>
                     <div>• 建议尺寸：24×24 或 48×48 px</div>
                     <div>• 颜色：建议使用 currentColor 以支持主题适配</div>
                     <div>• 需包含 viewBox 属性</div>
                  </div>
               </template>
               <el-upload
                  ref="uploadRef"
                  :show-file-list="false"
                  :before-upload="beforeUpload"
                  :http-request="handleUpload"
                  accept=".svg"
               >
                  <el-button type="primary" plain>
                     <el-icon class="el-icon--left"><Upload /></el-icon>
                     上传自定义图标
                  </el-button>
               </el-upload>
            </el-tooltip>
         </div>

         <!-- 上传提示信息 -->
         <div class="upload-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>仅支持 SVG 格式，文件大小 ≤ 100KB，建议 24×24px，使用 currentColor 以适配主题色。</span>
         </div>

         <!-- 图标网格 -->
         <div class="icon-list">
            <div class="list-container">
               <div
                  v-for="(item, index) in iconList"
                  :key="index"
                  class="icon-item-wrapper"
                  @click="selectedIcon(item)"
               >
                  <div :class="['icon-item', { active: activeIconLocal === item }]">
                     <svg-icon :icon-class="item" class-name="icon" />
                     <span>{{ item }}</span>
                  </div>
               </div>
            </div>
            <el-empty v-if="iconList.length === 0" description="未找到匹配的图标" :image-size="60" />
         </div>
      </div>

      <template #footer>
         <div class="dialog-footer">
            <span v-if="uploadMsg" class="upload-msg" :class="uploadMsgType">{{ uploadMsg }}</span>
            <el-button @click="visible = false">取 消</el-button>
            <el-button type="primary" :disabled="!activeIconLocal" @click="confirmSelect">确 定</el-button>
         </div>
      </template>
   </el-dialog>
</template>

<script setup>
import icons from './requireIcons'
import { Search, Upload, InfoFilled } from '@element-plus/icons-vue'
import { uploadIcon } from './iconUpload'
import { getToken } from '@/utils/auth'

const props = defineProps({
   activeIcon: {
      type: String,
      default: ''
   }
})

const emit = defineEmits(['selected'])

const visible = ref(false)
const iconName = ref('')
const iconList = ref(icons)
const activeIconLocal = ref('')
const uploadRef = ref(null)
const uploadMsg = ref('')
const uploadMsgType = ref('')

/** 弹框打开时初始化 */
function onOpen() {
   iconName.value = ''
   iconList.value = [...icons]
   activeIconLocal.value = props.activeIcon || ''
   uploadMsg.value = ''
}

/** 打开弹框 */
function open() {
   visible.value = true
}

/** 过滤图标 */
function filterIcons() {
   if (iconName.value) {
      iconList.value = icons.filter(item => item.indexOf(iconName.value) !== -1)
   } else {
      iconList.value = [...icons]
   }
}

/** 点击图标选中 */
function selectedIcon(name) {
   activeIconLocal.value = name
}

/** 确认选择 */
function confirmSelect() {
   emit('selected', activeIconLocal.value)
   visible.value = false
}

/** 上传前校验 */
function beforeUpload(file) {
   const isSvg = file.type === 'image/svg+xml' || file.name.endsWith('.svg')
   if (!isSvg) {
      uploadMsg.value = '只能上传 SVG 格式文件'
      uploadMsgType.value = 'error'
      return false
   }
   const isLt100k = file.size / 1024 < 100
   if (!isLt100k) {
      uploadMsg.value = 'SVG 文件大小不能超过 100KB'
      uploadMsgType.value = 'error'
      return false
   }
   return true
}

/** 自定义上传 */
async function handleUpload(options) {
   const { file } = options
   uploadMsg.value = '上传中...'
   uploadMsgType.value = ''
   try {
      const res = await uploadIcon(file)
      if (res.code === 200) {
         const iconName = res.iconName || file.name.replace(/\.svg$/i, '')
         // 将新图标动态注册到 SVG sprite
         registerDynamicIcon(iconName, res.svgContent)
         // 添加到图标列表
         if (!icons.includes(iconName)) {
            icons.push(iconName)
         }
         iconList.value = [...icons]
         activeIconLocal.value = iconName
         uploadMsg.value = `图标 "${iconName}" 上传成功，已添加到列表`
         uploadMsgType.value = 'success'
      } else {
         uploadMsg.value = res.msg || '上传失败'
         uploadMsgType.value = 'error'
      }
   } catch (e) {
      uploadMsg.value = '上传失败：' + (e.message || '服务器错误')
      uploadMsgType.value = 'error'
   }
}

/** 动态注册 SVG 图标到 sprite */
function registerDynamicIcon(name, svgContent) {
   const symbolId = `icon-${name}`
   // 检查是否已存在
   const existing = document.getElementById(symbolId)
   if (existing) return
   // 创建 symbol
   const svgEl = new DOMParser().parseFromString(svgContent, 'image/svg+xml').documentElement
   const symbol = document.createElementNS('http://www.w3.org/2000/svg', 'symbol')
   symbol.setAttribute('id', symbolId)
   symbol.setAttribute('viewBox', svgEl.getAttribute('viewBox') || '0 0 1024 1024')
   while (svgEl.firstChild) {
      symbol.appendChild(svgEl.firstChild)
   }
   // 找到 SVG sprite 容器并添加
   const sprite = document.querySelector('svg[aria-hidden="true"]') || document.body.querySelector('svg')
   if (sprite) {
      sprite.appendChild(symbol)
   } else {
      // 创建隐藏的 SVG sprite
      const spriteEl = document.createElementNS('http://www.w3.org/2000/svg', 'svg')
      spriteEl.setAttribute('aria-hidden', 'true')
      spriteEl.style.display = 'none'
      spriteEl.appendChild(symbol)
      document.body.insertBefore(spriteEl, document.body.firstChild)
   }
}

defineExpose({ open, reset: () => { iconName.value = ''; iconList.value = [...icons] } })
</script>

<style lang="scss" scoped>
.icon-dialog-body {
   padding: 4px 0;
}

.icon-toolbar {
   display: flex;
   align-items: center;
   gap: 12px;
   margin-bottom: 8px;

   .icon-search {
      flex: 1;
   }
}

.upload-tips {
   display: flex;
   align-items: center;
   gap: 6px;
   margin-bottom: 14px;
   padding: 8px 12px;
   background: #f0f9ff;
   border: 1px solid #bae6fd;
   border-radius: 8px;
   font-size: 12px;
   color: #0369a1;
   line-height: 1.5;

   .el-icon {
      font-size: 14px;
      flex-shrink: 0;
      color: #0ea5e9;
   }
}

.icon-list {
   height: 360px;
   overflow-y: auto;
   border: 1px solid #e5e7eb;
   border-radius: 8px;
   padding: 8px;
   background: #fff;

   .list-container {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;

      .icon-item-wrapper {
         width: calc(100% / 5 - 6px);
         cursor: pointer;

         .icon-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 10px 4px;
            border-radius: 8px;
            border: 2px solid transparent;
            transition: all 0.15s ease;

            .svg-icon {
               width: 24px;
               height: 24px;
               margin-bottom: 4px;
            }

            span {
               font-size: 11px;
               color: #6b7280;
               overflow: hidden;
               text-overflow: ellipsis;
               white-space: nowrap;
               max-width: 100%;
               text-align: center;
            }

            &:hover {
               background: #f0f7ff;
               border-color: #93c5fd;
            }

            &.active {
               background: #dbeafe;
               border-color: #2563eb;

               .svg-icon {
                  color: #2563eb;
               }

               span {
                  color: #1e40af;
                  font-weight: 600;
               }
            }
         }
      }
   }
}

.dialog-footer {
   display: flex;
   align-items: center;
   justify-content: flex-end;
   gap: 12px;

   .upload-msg {
      flex: 1;
      text-align: left;
      font-size: 13px;

      &.success { color: #10b981; }
      &.error { color: #ef4444; }
   }
}
</style>
