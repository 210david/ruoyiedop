<template>
  <div class="dashboard-container" ref="dashboardRef">
    <!-- 顶部标题栏 -->
    <header class="top-bar">
      <div class="top-bar-left">
        <div class="decoration-line"></div>
        <el-icon :size="20" color="var(--eq-primary)" style="opacity: 0.8;">
          <Cpu />
        </el-icon>
        <span class="sub-title">EQUIPMENT LIFECYCLE</span>
      </div>
      <div class="top-bar-center">
        <span class="main-title glow-amber">设备全生命周期监控大屏</span>
      </div>
      <div class="top-bar-right">
        <div class="fullscreen-btn" @click="toggleFullscreen" :title="isFullscreen ? '退出全屏' : '全屏'">
          <svg-icon :icon-class="isFullscreen ? 'exit-fullscreen' : 'fullscreen'" />
        </div>
        <div class="datetime">
          <span class="date-text">{{ currentDate }}</span>
          <span class="time-text">{{ currentTime }}</span>
        </div>
        <div class="right-decoration">
          <el-icon :size="16" color="var(--eq-primary)" style="opacity: 0.7;">
            <Connection />
          </el-icon>
          <div class="decoration-line-right"></div>
        </div>
      </div>
      <div class="bottom-glow-line"></div>
    </header>

    <!-- 设备信息栏 -->
    <div class="device-info-bar">
      <div class="device-info-left">
        <el-select v-model="selectedDevice" class="device-select" size="small" @change="onDeviceChange" placeholder="选择设备">
          <el-option v-for="item in equipmentList" :key="item.equipmentId" :label="item.equipmentName" :value="item.equipmentId" />
        </el-select>
        <span class="device-code font-mono">{{ deviceInfo.equipmentCode }}</span>
        <span class="divider">|</span>
        <span class="dept-name">{{ deviceInfo.deptName }}</span>
      </div>
      <div class="device-status">
        <span class="status-dot" :class="statusDotClass"></span>
        <span class="status-text">{{ deviceStatusText }} · {{ deviceStatusEn }}</span>
      </div>
    </div>

    <!-- 主体三栏布局 -->
    <div class="main-content">
      <!-- 左侧边栏 -->
      <div class="left-sidebar">
        <!-- 生命周期时间线 -->
        <div class="panel surface-panel glow-border-amber timeline-panel">
          <div class="panel-header">
            <el-icon :size="14" color="var(--eq-primary)">
              <Share />
            </el-icon>
            <span class="panel-title">生命周期阶段</span>
          </div>
          <div class="timeline">
            <div class="timeline-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: lifecycleProgress + '%' }"></div>
                <div class="progress-glow" :style="{ left: lifecycleProgress + '%' }"></div>
              </div>
              <span class="progress-text font-mono">{{ lifecycleProgress }}%</span>
            </div>
            <div class="timeline-stages">
              <div class="timeline-item" v-for="(item, index) in lifecycleStages" :key="index">
                <div class="timeline-node" :class="[item.status, item.status === 'active' ? item.statusType : '']">
                  <div class="node-glow"></div>
                  <div class="node-inner">
                    <el-icon v-if="item.icon && item.status !== 'future'" :size="12" color="var(--eq-primary-foreground)">
                      <component :is="item.icon" />
                    </el-icon>
                    <el-icon v-else-if="item.icon && item.status === 'future'" :size="12" color="var(--eq-muted-foreground)">
                      <component :is="item.icon" />
                    </el-icon>
                    <span v-else class="node-status-icon">●</span>
                  </div>
                </div>
                <div class="timeline-label">
                  <template v-if="item.status === 'active' && item.currentStatus">
                    <span class="label-text active-label" :class="'status-' + item.statusType">{{ item.currentStatus }}</span>
                    <span class="label-date font-mono active-label" :class="'status-' + item.statusType">当前</span>
                  </template>
                  <template v-else>
                    <span class="label-text" :class="{ 'active-label': item.status === 'active' }">{{ item.label }}</span>
                    <span class="label-date font-mono" :class="{ 'active-label': item.status === 'active' }">{{ item.date }}</span>
                  </template>
                </div>
                <div class="timeline-connector" v-if="index < lifecycleStages.length - 1">
                  <div class="connector-line" :class="[item.status, lifecycleStages[index + 1].status]"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 备件更换 -->
        <div class="panel surface-panel parts-panel">
          <div class="panel-header">
            <el-icon :size="14" color="var(--eq-primary)">
              <Box />
            </el-icon>
            <span class="panel-title">备件更换</span>
          </div>
          <div class="parts-list">
            <div v-if="partsList.length === 0" class="parts-empty">
              <span class="empty-text">暂无数据</span>
            </div>
            <div class="parts-item" v-for="(item, index) in partsList" :key="index" v-else>
              <span class="parts-name">{{ item.name }}</span>
              <span class="parts-info font-mono">
                {{ item.date }} | <span class="parts-qty">×{{ item.qty }}</span>
              </span>
            </div>
          </div>
        </div>

        <!-- 维护费用 -->
        <div class="panel surface-panel cost-panel">
          <div class="panel-header">
            <el-icon :size="14" color="var(--eq-primary)">
              <Wallet />
            </el-icon>
            <span class="panel-title">维护费用</span>
            <span class="panel-badge font-mono">近6月</span>
          </div>
          <div class="cost-summary">
            <div class="cost-total">
              <span class="total-label">累计费用</span>
              <span class="total-value font-mono glow-amber">¥{{ formatNumber(totalCost) }}</span>
            </div>
            <div class="cost-trend">
              <span class="trend-icon">↑</span>
              <span class="trend-text font-mono">+{{ costTrend }}%</span>
            </div>
          </div>
          <div class="cost-chart">
            <div class="chart-grid">
              <div class="grid-line" v-for="n in 5" :key="n">
                <span class="grid-label font-mono">{{ ['20k', '15k', '10k', '5k', '0'][n-1] }}</span>
              </div>
            </div>
            <div class="bar-group">
              <div class="bar-col" v-for="(item, index) in costData" :key="index" :style="{ animationDelay: index * 0.12 + 's' }">
              <div class="bar-wrapper">
                <div class="bar-value font-mono">¥{{ item.value }}</div>
                <div class="bar-fill" :style="{ height: item.height + '%', '--bar-delay': index * 0.12 + 's' }">
                  <div class="bar-gradient"></div>
                  <div class="bar-highlight"></div>
                  <div class="bar-glow-top"></div>
                  <div class="bar-shine"></div>
                </div>
                <div class="bar-base"></div>
              </div>
              <span class="bar-month font-mono">{{ item.month }}</span>
            </div>
            </div>
          </div>
          <div class="cost-breakdown">
            <div class="breakdown-item">
              <div class="breakdown-bar">
                <div class="breakdown-bar-fill" :style="{ width: partsCostPercent + '%', background: 'linear-gradient(90deg, var(--eq-primary), #fcd34d)' }"></div>
              </div>
              <div class="breakdown-info">
                <span class="breakdown-dot" style="background: var(--eq-primary);"></span>
                <span class="breakdown-label">备件费用</span>
              </div>
              <span class="breakdown-value font-mono">¥{{ formatNumber(partsCost) }}</span>
            </div>
            <div class="breakdown-item">
              <div class="breakdown-bar">
                <div class="breakdown-bar-fill" :style="{ width: (100 - partsCostPercent) + '%', background: 'linear-gradient(90deg, var(--state-success), #86efac)' }"></div>
              </div>
              <div class="breakdown-info">
                <span class="breakdown-dot" style="background: var(--state-success);"></span>
                <span class="breakdown-label">维修费用</span>
              </div>
              <span class="breakdown-value font-mono">¥{{ formatNumber(repairCost) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中央区域 -->
      <div class="center-area">
        <!-- 雷达仪表盘区域 -->
        <div class="panel surface-glow radar-panel eq-fade-in-2">
          <div class="hero-border-anim"></div>
          
          <!-- SVG雷达背景 -->
          <svg class="radar-bg" viewBox="0 0 800 500" preserveAspectRatio="xMidYMid slice">
            <circle cx="400" cy="250" r="180" fill="none" stroke="var(--eq-primary)" stroke-width="0.5" opacity="0.3"/>
            <circle cx="400" cy="250" r="140" fill="none" stroke="var(--eq-primary)" stroke-width="0.5" opacity="0.25"/>
            <circle cx="400" cy="250" r="100" fill="none" stroke="var(--eq-primary)" stroke-width="0.5" opacity="0.2"/>
            <circle cx="400" cy="250" r="60" fill="none" stroke="var(--eq-primary)" stroke-width="0.5" opacity="0.15"/>
            <g stroke="var(--eq-primary)" stroke-width="0.5" opacity="0.2">
              <line x1="400" y1="70" x2="400" y2="80"/>
              <line x1="400" y1="420" x2="400" y2="430"/>
              <line x1="220" y1="250" x2="230" y2="250"/>
              <line x1="570" y1="250" x2="580" y2="250"/>
              <line x1="272" y1="122" x2="279" y2="129"/>
              <line x1="521" y1="371" x2="528" y2="378"/>
              <line x1="528" y1="122" x2="521" y2="129"/>
              <line x1="279" y1="371" x2="272" y2="378"/>
            </g>
            <line x1="400" y1="70" x2="400" y2="430" stroke="var(--eq-primary)" stroke-width="0.3" opacity="0.15"/>
            <line x1="220" y1="250" x2="580" y2="250" stroke="var(--eq-primary)" stroke-width="0.3" opacity="0.15"/>
          </svg>

          <!-- 3D设备图 -->
          <img :src="deviceImage" alt="CNC-A3200" class="device-3d-img" />

          <!-- 中央全息3D齿轮组 -->
          <div class="hologram-radar">
            <svg viewBox="0 0 220 220" class="hologram-svg">
              <defs>
                <linearGradient id="metal-grad-1" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#fcd34d" stop-opacity="0.9"/>
                  <stop offset="30%" stop-color="#f59e0b" stop-opacity="0.7"/>
                  <stop offset="60%" stop-color="#d97706" stop-opacity="0.5"/>
                  <stop offset="100%" stop-color="#fbbf24" stop-opacity="0.8"/>
                </linearGradient>
                <linearGradient id="metal-grad-2" x1="100%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#fbbf24" stop-opacity="0.8"/>
                  <stop offset="40%" stop-color="#f59e0b" stop-opacity="0.6"/>
                  <stop offset="70%" stop-color="#d97706" stop-opacity="0.4"/>
                  <stop offset="100%" stop-color="#fcd34d" stop-opacity="0.7"/>
                </linearGradient>
                <radialGradient id="metal-3d" cx="35%" cy="30%" r="65%">
                  <stop offset="0%" stop-color="#fef3c7" stop-opacity="0.95"/>
                  <stop offset="20%" stop-color="#fcd34d" stop-opacity="0.9"/>
                  <stop offset="45%" stop-color="#f59e0b" stop-opacity="0.7"/>
                  <stop offset="75%" stop-color="#d97706" stop-opacity="0.5"/>
                  <stop offset="100%" stop-color="#78350f" stop-opacity="0.4"/>
                </radialGradient>
                <radialGradient id="metal-3d-inner" cx="40%" cy="35%" r="60%">
                  <stop offset="0%" stop-color="#fef9c3" stop-opacity="0.9"/>
                  <stop offset="30%" stop-color="#fde68a" stop-opacity="0.75"/>
                  <stop offset="60%" stop-color="#f59e0b" stop-opacity="0.5"/>
                  <stop offset="100%" stop-color="#92400e" stop-opacity="0.4"/>
                </radialGradient>
                <radialGradient id="core-glow" cx="50%" cy="50%" r="50%">
                  <stop offset="0%" stop-color="#f59e0b" stop-opacity="0.6"/>
                  <stop offset="50%" stop-color="#f59e0b" stop-opacity="0.2"/>
                  <stop offset="100%" stop-color="#f59e0b" stop-opacity="0"/>
                </radialGradient>
                <radialGradient id="sweep-fill">
                  <stop offset="0%" stop-color="var(--eq-primary)" stop-opacity="0.3"/>
                  <stop offset="100%" stop-color="var(--eq-primary)" stop-opacity="0"/>
                </radialGradient>
                <linearGradient id="tooth-grad" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#fef3c7" stop-opacity="0.95"/>
                  <stop offset="25%" stop-color="#fcd34d" stop-opacity="0.85"/>
                  <stop offset="55%" stop-color="#f59e0b" stop-opacity="0.65"/>
                  <stop offset="100%" stop-color="#78350f" stop-opacity="0.7"/>
                </linearGradient>
                <linearGradient id="tooth-side-grad" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#92400e" stop-opacity="0.8"/>
                  <stop offset="50%" stop-color="#78350f" stop-opacity="0.6"/>
                  <stop offset="100%" stop-color="#451a03" stop-opacity="0.7"/>
                </linearGradient>
                <filter id="metal-glow" x="-50%" y="-50%" width="200%" height="200%">
                  <feGaussianBlur stdDeviation="1.5" result="blur"/>
                  <feMerge>
                    <feMergeNode in="blur"/>
                    <feMergeNode in="SourceGraphic"/>
                  </feMerge>
                </filter>
                <filter id="inner-shadow">
                  <feOffset dx="0" dy="1"/>
                  <feGaussianBlur stdDeviation="1" result="offset-blur"/>
                  <feComposite operator="out" in="SourceGraphic" in2="offset-blur" result="inverse"/>
                  <feFlood flood-color="#000000" flood-opacity="0.4" result="color"/>
                  <feComposite operator="in" in="color" in2="inverse" result="shadow"/>
                  <feComposite operator="over" in="shadow" in2="SourceGraphic"/>
                </filter>
              </defs>
              
              <circle cx="110" cy="110" r="108" fill="url(#core-glow)" opacity="0.3"/>
              
              <circle cx="110" cy="112" r="105" fill="#1c1917" opacity="0.6"/>
              <circle cx="110" cy="110" r="105" fill="none" stroke="url(#metal-grad-1)" stroke-width="2" opacity="0.5"/>
              <circle cx="110" cy="110" r="102" fill="none" stroke="#78350f" stroke-width="1" opacity="0.3"/>
              
              <g fill="#78350f" opacity="0.6">
                <circle cx="110" cy="12" r="3"/>
                <circle cx="110" cy="208" r="3"/>
                <circle cx="12" cy="110" r="3"/>
                <circle cx="208" cy="110" r="3"/>
                <circle cx="42" cy="42" r="2.5"/>
                <circle cx="178" cy="42" r="2.5"/>
                <circle cx="42" cy="178" r="2.5"/>
                <circle cx="178" cy="178" r="2.5"/>
              </g>
              <g fill="#fef3c7" opacity="0.4">
                <circle cx="109" cy="11" r="1"/>
                <circle cx="11" cy="109" r="1"/>
                <circle cx="41" cy="41" r="0.8"/>
              </g>
              
              <g class="ring-rotate-cw" style="transform-origin: 110px 110px;">
                <circle cx="110" cy="110" r="100" fill="none" stroke="url(#metal-grad-1)" stroke-width="3" opacity="0.7"/>
                <circle cx="110" cy="110" r="96" fill="none" stroke="var(--eq-primary)" stroke-width="0.5" opacity="0.25"/>
                <g fill="url(#tooth-grad)" opacity="0.75">
                  <rect x="108" y="6" width="4" height="10" rx="1"/>
                  <rect x="108" y="204" width="4" height="10" rx="1"/>
                  <rect x="6" y="108" width="10" height="4" rx="1"/>
                  <rect x="204" y="108" width="10" height="4" rx="1"/>
                  <rect x="38" y="38" width="6" height="6" rx="1" transform="rotate(45 41 41)"/>
                  <rect x="176" y="38" width="6" height="6" rx="1" transform="rotate(-45 179 41)"/>
                  <rect x="38" y="176" width="6" height="6" rx="1" transform="rotate(-45 41 179)"/>
                  <rect x="176" y="176" width="6" height="6" rx="1" transform="rotate(45 179 179)"/>
                  <rect x="74" y="14" width="5" height="8" rx="1" transform="rotate(22.5 76.5 18)"/>
                  <rect x="141" y="14" width="5" height="8" rx="1" transform="rotate(-22.5 143.5 18)"/>
                  <rect x="74" y="198" width="5" height="8" rx="1" transform="rotate(-22.5 76.5 202)"/>
                  <rect x="141" y="198" width="5" height="8" rx="1" transform="rotate(22.5 143.5 202)"/>
                  <rect x="14" y="74" width="8" height="5" rx="1" transform="rotate(22.5 18 76.5)"/>
                  <rect x="14" y="141" width="8" height="5" rx="1" transform="rotate(-22.5 18 143.5)"/>
                  <rect x="198" y="74" width="8" height="5" rx="1" transform="rotate(-22.5 202 76.5)"/>
                  <rect x="198" y="141" width="8" height="5" rx="1" transform="rotate(22.5 202 143.5)"/>
                </g>
              </g>
              
              <g class="ring-rotate-ccw" style="transform-origin: 110px 110px;">
                <circle cx="110" cy="110" r="88" fill="none" stroke="url(#metal-grad-2)" stroke-width="2" opacity="0.45"/>
                <circle cx="110" cy="110" r="85" fill="none" stroke="var(--eq-primary)" stroke-width="0.5" opacity="0.2" stroke-dasharray="4 8"/>
                <g fill="var(--eq-primary)" opacity="0.45">
                  <circle cx="110" cy="30" r="2.5"/>
                  <circle cx="110" cy="190" r="2.5"/>
                  <circle cx="30" cy="110" r="2.5"/>
                  <circle cx="190" cy="110" r="2.5"/>
                  <circle cx="53" cy="53" r="2"/>
                  <circle cx="167" cy="53" r="2"/>
                  <circle cx="53" cy="167" r="2"/>
                  <circle cx="167" cy="167" r="2"/>
                </g>
              </g>
              
              <circle cx="110" cy="110" r="75" fill="none" stroke="var(--eq-primary)" stroke-width="0.8" opacity="0.2"/>
              <circle cx="110" cy="110" r="72" fill="none" stroke="var(--eq-primary)" stroke-width="0.3" opacity="0.12" stroke-dasharray="2 6"/>
              
              <g class="main-gear" style="transform-origin: 110px 110px;" filter="url(#metal-glow)">
                <circle cx="110" cy="112" r="54" fill="#451a03" opacity="0.4"/>
                <circle cx="110" cy="110" r="54" fill="url(#metal-3d)" opacity="0.85"/>
                <circle cx="110" cy="110" r="50" fill="none" stroke="#fef3c7" stroke-width="0.8" opacity="0.5"/>
                <circle cx="110" cy="110" r="48" fill="none" stroke="#78350f" stroke-width="0.6" opacity="0.4"/>
                <circle cx="110" cy="110" r="56" fill="none" stroke="#78350f" stroke-width="0.5" opacity="0.3"/>
                
                <g fill="url(#tooth-grad)" opacity="0.9">
                  <rect x="108" y="50" width="4" height="9" rx="1"/>
                  <rect x="108" y="161" width="4" height="9" rx="1"/>
                  <rect x="50" y="108" width="9" height="4" rx="1"/>
                  <rect x="161" y="108" width="9" height="4" rx="1"/>
                  <rect x="68" y="66" width="5.5" height="5.5" rx="1" transform="rotate(45 70.75 68.75)"/>
                  <rect x="146.5" y="66" width="5.5" height="5.5" rx="1" transform="rotate(-45 149.25 68.75)"/>
                  <rect x="68" y="148.5" width="5.5" height="5.5" rx="1" transform="rotate(-45 70.75 151.25)"/>
                  <rect x="146.5" y="148.5" width="5.5" height="5.5" rx="1" transform="rotate(45 149.25 151.25)"/>
                  <rect x="86" y="53" width="4" height="8" rx="1" transform="rotate(12 88 57)"/>
                  <rect x="130" y="53" width="4" height="8" rx="1" transform="rotate(-12 132 57)"/>
                  <rect x="86" y="159" width="4" height="8" rx="1" transform="rotate(-12 88 163)"/>
                  <rect x="130" y="159" width="4" height="8" rx="1" transform="rotate(12 132 163)"/>
                  <rect x="53" y="86" width="8" height="4" rx="1" transform="rotate(12 57 88)"/>
                  <rect x="53" y="130" width="8" height="4" rx="1" transform="rotate(-12 57 132)"/>
                  <rect x="159" y="86" width="8" height="4" rx="1" transform="rotate(-12 163 88)"/>
                  <rect x="159" y="130" width="8" height="4" rx="1" transform="rotate(12 163 132)"/>
                </g>
                
                <g fill="url(#tooth-side-grad)" opacity="0.6">
                  <rect x="112" y="50" width="2" height="9" rx="0.5"/>
                  <rect x="106" y="161" width="2" height="9" rx="0.5"/>
                  <rect x="50" y="112" width="9" height="2" rx="0.5"/>
                  <rect x="161" y="106" width="9" height="2" rx="0.5"/>
                </g>
                
                <g stroke="#78350f" stroke-width="1.2" opacity="0.35" stroke-linecap="round">
                  <line x1="110" y1="56" x2="110" y2="75"/>
                  <line x1="110" y1="145" x2="110" y2="164"/>
                  <line x1="56" y1="110" x2="75" y2="110"/>
                  <line x1="145" y1="110" x2="164" y2="110"/>
                  <line x1="71" y1="71" x2="85" y2="85"/>
                  <line x1="135" y1="71" x2="149" y2="85"/>
                  <line x1="71" y1="149" x2="85" y2="135"/>
                  <line x1="135" y1="149" x2="149" y2="135"/>
                </g>
                <g stroke="#fef3c7" stroke-width="0.6" opacity="0.4" stroke-linecap="round">
                  <line x1="110" y1="56" x2="110" y2="75"/>
                  <line x1="56" y1="110" x2="75" y2="110"/>
                  <line x1="71" y1="71" x2="85" y2="85"/>
                  <line x1="135" y1="71" x2="149" y2="85"/>
                </g>
                
                <circle cx="110" cy="110" r="25" fill="url(#metal-3d-inner)" opacity="0.9"/>
                <circle cx="110" cy="110" r="25" fill="none" stroke="#fef3c7" stroke-width="0.6" opacity="0.5"/>
                <circle cx="110" cy="110" r="22" fill="none" stroke="#78350f" stroke-width="0.5" opacity="0.4"/>
                <circle cx="110" cy="110" r="14" fill="url(#core-glow)" opacity="0.6"/>
                <circle cx="110" cy="110" r="8" fill="url(#metal-3d)" opacity="0.8"/>
                <circle cx="110" cy="110" r="4" fill="#fef3c7" opacity="0.7"/>
                <circle cx="110" cy="110" r="2" fill="#fffbeb" opacity="0.9"/>
                
                <g fill="#78350f" opacity="0.6">
                  <circle cx="110" cy="94" r="2.2"/>
                  <circle cx="110" cy="126" r="2.2"/>
                  <circle cx="94" cy="110" r="2.2"/>
                  <circle cx="126" cy="110" r="2.2"/>
                </g>
                <g fill="#fef3c7" opacity="0.5">
                  <circle cx="109" cy="93" r="0.8"/>
                  <circle cx="93" cy="109" r="0.8"/>
                </g>
                
                <g fill="#78350f" opacity="0.5">
                  <circle cx="98" cy="98" r="1.8"/>
                  <circle cx="122" cy="98" r="1.8"/>
                  <circle cx="98" cy="122" r="1.8"/>
                  <circle cx="122" cy="122" r="1.8"/>
                </g>
              </g>
              
              <g class="gear-1" style="transform-origin: 78px 78px;" filter="url(#metal-glow)">
                <circle cx="78" cy="80" r="23" fill="#451a03" opacity="0.35"/>
                <circle cx="78" cy="78" r="23" fill="url(#metal-3d)" opacity="0.8"/>
                <circle cx="78" cy="78" r="20" fill="none" stroke="#fef3c7" stroke-width="0.6" opacity="0.45"/>
                <circle cx="78" cy="78" r="18" fill="none" stroke="#78350f" stroke-width="0.5" opacity="0.35"/>
                
                <g fill="url(#tooth-grad)" opacity="0.85">
                  <rect x="76.5" y="54" width="3" height="7" rx="0.5"/>
                  <rect x="76.5" y="95" width="3" height="7" rx="0.5"/>
                  <rect x="54" y="76.5" width="7" height="3" rx="0.5"/>
                  <rect x="95" y="76.5" width="7" height="3" rx="0.5"/>
                  <rect x="63" y="63" width="4.5" height="4.5" rx="0.5" transform="rotate(45 65.25 65.25)"/>
                  <rect x="90.5" y="63" width="4.5" height="4.5" rx="0.5" transform="rotate(-45 92.75 65.25)"/>
                  <rect x="63" y="90.5" width="4.5" height="4.5" rx="0.5" transform="rotate(-45 65.25 92.75)"/>
                  <rect x="90.5" y="90.5" width="4.5" height="4.5" rx="0.5" transform="rotate(45 92.75 92.75)"/>
                </g>
                
                <circle cx="78" cy="78" r="10" fill="url(#metal-3d-inner)" opacity="0.85"/>
                <circle cx="78" cy="78" r="5" fill="url(#metal-3d)" opacity="0.75"/>
                <circle cx="78" cy="78" r="2" fill="#fef3c7" opacity="0.6"/>
                
                <g fill="#78350f" opacity="0.5">
                  <circle cx="78" cy="67" r="1.5"/>
                  <circle cx="78" cy="89" r="1.5"/>
                  <circle cx="67" cy="78" r="1.5"/>
                  <circle cx="89" cy="78" r="1.5"/>
                </g>
              </g>
              
              <g class="gear-2" style="transform-origin: 148px 142px;" filter="url(#metal-glow)">
                <circle cx="148" cy="144" r="31" fill="#451a03" opacity="0.35"/>
                <circle cx="148" cy="142" r="31" fill="url(#metal-3d)" opacity="0.75"/>
                <circle cx="148" cy="142" r="27" fill="none" stroke="#fef3c7" stroke-width="0.6" opacity="0.4"/>
                <circle cx="148" cy="142" r="24" fill="none" stroke="#78350f" stroke-width="0.5" opacity="0.3"/>
                
                <g fill="url(#tooth-grad)" opacity="0.8">
                  <rect x="146" y="108" width="4" height="8" rx="0.5"/>
                  <rect x="146" y="168" width="4" height="8" rx="0.5"/>
                  <rect x="114" y="140" width="8" height="4" rx="0.5"/>
                  <rect x="174" y="140" width="8" height="4" rx="0.5"/>
                  <rect x="125" y="119" width="5.5" height="5.5" rx="0.5" transform="rotate(45 127.75 121.75)"/>
                  <rect x="165.5" y="119" width="5.5" height="5.5" rx="0.5" transform="rotate(-45 168.25 121.75)"/>
                  <rect x="125" y="162.5" width="5.5" height="5.5" rx="0.5" transform="rotate(-45 127.75 165.25)"/>
                  <rect x="165.5" y="162.5" width="5.5" height="5.5" rx="0.5" transform="rotate(45 168.25 165.25)"/>
                  <rect x="134" y="112" width="3.5" height="6.5" rx="0.5" transform="rotate(18 135.75 115.25)"/>
                  <rect x="158.5" y="112" width="3.5" height="6.5" rx="0.5" transform="rotate(-18 160.25 115.25)"/>
                  <rect x="134" y="167.5" width="3.5" height="6.5" rx="0.5" transform="rotate(-18 135.75 170.75)"/>
                  <rect x="158.5" y="167.5" width="3.5" height="6.5" rx="0.5" transform="rotate(18 160.25 170.75)"/>
                </g>
                
                <g stroke="#78350f" stroke-width="0.9" opacity="0.3" stroke-linecap="round">
                  <line x1="148" y1="118" x2="148" y2="130"/>
                  <line x1="148" y1="154" x2="148" y2="166"/>
                  <line x1="122" y1="142" x2="134" y2="142"/>
                  <line x1="162" y1="142" x2="174" y2="142"/>
                </g>
                
                <circle cx="148" cy="142" r="12" fill="url(#metal-3d-inner)" opacity="0.8"/>
                <circle cx="148" cy="142" r="6" fill="url(#metal-3d)" opacity="0.7"/>
                <circle cx="148" cy="142" r="2.5" fill="#fef3c7" opacity="0.55"/>
                
                <g fill="#78350f" opacity="0.45">
                  <circle cx="148" cy="129" r="1.8"/>
                  <circle cx="148" cy="155" r="1.8"/>
                  <circle cx="135" cy="142" r="1.8"/>
                  <circle cx="161" cy="142" r="1.8"/>
                </g>
              </g>
              
              <g class="gear-3" style="transform-origin: 158px 76px;" filter="url(#metal-glow)">
                <circle cx="158" cy="77.5" r="16" fill="#451a03" opacity="0.3"/>
                <circle cx="158" cy="76" r="16" fill="url(#metal-3d)" opacity="0.7"/>
                <circle cx="158" cy="76" r="13" fill="none" stroke="#fef3c7" stroke-width="0.5" opacity="0.4"/>
                
                <g fill="url(#tooth-grad)" opacity="0.75">
                  <rect x="156.5" y="58" width="3" height="6" rx="0.5"/>
                  <rect x="156.5" y="88" width="3" height="6" rx="0.5"/>
                  <rect x="140" y="74.5" width="6" height="3" rx="0.5"/>
                  <rect x="170" y="74.5" width="6" height="3" rx="0.5"/>
                </g>
                
                <circle cx="158" cy="76" r="6" fill="url(#metal-3d-inner)" opacity="0.75"/>
                <circle cx="158" cy="76" r="2.5" fill="#78350f" opacity="0.5"/>
              </g>
              
              <g class="gear-4" style="transform-origin: 66px 152px;" filter="url(#metal-glow)">
                <circle cx="66" cy="153.5" r="19" fill="#451a03" opacity="0.3"/>
                <circle cx="66" cy="152" r="19" fill="url(#metal-3d)" opacity="0.65"/>
                <circle cx="66" cy="152" r="16" fill="none" stroke="#fef3c7" stroke-width="0.5" opacity="0.35"/>
                
                <g fill="url(#tooth-grad)" opacity="0.7">
                  <rect x="64.5" y="131" width="3" height="6" rx="0.5"/>
                  <rect x="64.5" y="167" width="3" height="6" rx="0.5"/>
                  <rect x="45" y="150.5" width="6" height="3" rx="0.5"/>
                  <rect x="81" y="150.5" width="6" height="3" rx="0.5"/>
                </g>
                
                <circle cx="66" cy="152" r="7" fill="url(#metal-3d-inner)" opacity="0.7"/>
                <circle cx="66" cy="152" r="3" fill="#78350f" opacity="0.45"/>
              </g>
              
              <g class="data-arc-1" style="transform-origin: 110px 110px;">
                <circle cx="110" cy="110" r="68" fill="none" stroke="var(--state-success)" stroke-width="3" stroke-dasharray="42 385" stroke-dashoffset="-10" opacity="0.55" stroke-linecap="round"/>
              </g>
              <g class="data-arc-2" style="transform-origin: 110px 110px;">
                <circle cx="110" cy="110" r="68" fill="none" stroke="var(--state-warning)" stroke-width="3" stroke-dasharray="28 399" stroke-dashoffset="-120" opacity="0.45" stroke-linecap="round"/>
              </g>
              
              <g class="sweep-beam" style="transform-origin: 110px 110px;">
                <path d="M110,110 L110,30 A80,80 0 0,1 170,45 Z" fill="url(#sweep-fill)" opacity="0.35"/>
              </g>
              
              <circle cx="50" cy="45" r="2" fill="var(--eq-primary)" class="particle p1"/>
              <circle cx="175" cy="55" r="1.5" fill="var(--state-success)" class="particle p2"/>
              <circle cx="165" cy="170" r="1.5" fill="var(--state-warning)" class="particle p3"/>
              <circle cx="45" cy="160" r="2" fill="var(--state-purple)" class="particle p4"/>
              <circle cx="95" cy="40" r="1" fill="var(--eq-primary)" class="particle p5"/>
              <circle cx="180" cy="100" r="1" fill="var(--state-info)" class="particle p6"/>
            </svg>
          </div>

          <!-- 顶部左侧仪表 -->
          <div class="top-gauges top-left-gauges">
            <div class="gauge-card">
              <div class="gauge-ring-lg" :style="{ '--gauge-pct': gauges.loadRate, '--gauge-color': 'var(--state-success)' }">
                <div class="gauge-inner-lg">
                  <span class="gauge-value" style="color: var(--state-success);">{{ gauges.loadRate }}%</span>
                </div>
              </div>
              <span class="gauge-label">负载率</span>
            </div>
            <div class="gauge-card">
              <div class="gauge-ring-lg" :style="{ '--gauge-pct': Math.min(gauges.temperature, 100), '--gauge-color': 'var(--state-warning)' }">
                <div class="gauge-inner-lg">
                  <span class="gauge-value" style="color: var(--state-warning);">{{ gauges.temperature }}°C</span>
                </div>
              </div>
              <span class="gauge-label">温度</span>
            </div>
          </div>

          <!-- 顶部右侧仪表 -->
          <div class="top-gauges top-right-gauges">
            <div class="gauge-card">
              <div class="gauge-ring-lg" :style="{ '--gauge-pct': Math.min(gauges.speed / 150, 100), '--gauge-color': 'var(--eq-primary)' }">
                <div class="gauge-inner-lg">
                  <span class="gauge-value" style="color: var(--eq-primary);">{{ formatSpeed(gauges.speed) }}</span>
                </div>
              </div>
              <span class="gauge-label">转速</span>
            </div>
            <div class="gauge-card">
              <div class="gauge-ring-lg" :style="{ '--gauge-pct': gauges.oee, '--gauge-color': 'var(--state-purple)' }">
                <div class="gauge-inner-lg">
                  <span class="gauge-value" style="color: var(--state-purple);">{{ gauges.oee }}%</span>
                </div>
              </div>
              <span class="gauge-label">OEE</span>
            </div>
          </div>

          <!-- 左侧设备台账信息 -->
          <div class="device-info-panel left-info-panel">
            <div class="info-panel-title">
              <span class="title-dot"></span>
              <span class="title-text">设备台账</span>
            </div>
            <div class="info-list">
              <div class="info-row">
                <span class="info-row-label">设备编号</span>
                <span class="info-row-value font-mono">{{ deviceInfo.equipmentCode }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">设备类型</span>
                <span class="info-row-value">{{ deviceInfo.categoryName }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">购置日期</span>
                <span class="info-row-value">{{ deviceInfo.purchaseDate }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">责任人</span>
                <span class="info-row-value">{{ deviceInfo.responsibleName }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">安装位置</span>
                <span class="info-row-value">{{ deviceInfo.installLocation }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">序列号</span>
                <span class="info-row-value font-mono">{{ deviceInfo.serialNumber }}</span>
              </div>
            </div>
          </div>

          <!-- 右侧设备台账信息 -->
          <div class="device-info-panel right-info-panel">
            <div class="info-panel-title">
              <span class="title-dot"></span>
              <span class="title-text">资产信息</span>
            </div>
            <div class="info-list">
              <div class="info-row">
                <span class="info-row-label">资产编号</span>
                <span class="info-row-value font-mono">{{ deviceInfo.assetCode }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">供应商</span>
                <span class="info-row-value">{{ deviceInfo.supplier }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">下次维保</span>
                <span class="info-row-value" style="color: var(--state-warning);">{{ nextPmDate }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">设备等级</span>
                <span class="info-row-value" :style="{ color: equipmentLevelColor }">{{ equipmentLevelText }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">质保期限</span>
                <span class="info-row-value">{{ deviceInfo.warrantyDate }}</span>
              </div>
              <div class="info-row">
                <span class="info-row-label">运行设备状态</span>
                <span class="info-row-value" :style="{ color: 'var(--state-success)', fontWeight: 600 }">{{ deviceStatusText }}</span>
              </div>
            </div>
          </div>

          <!-- 底部设备名 -->
          <div class="bottom-device-name">
            <span class="device-name-text">{{ deviceInfo.equipmentCode }} · {{ deviceInfo.equipmentName }}</span>
          </div>
        </div>

        <!-- 实时能耗图表 -->
        <div class="panel surface-panel energy-panel eq-fade-in-3">
          <div class="panel-header">
            <el-icon :size="14" color="var(--eq-primary)">
              <Lightning />
            </el-icon>
            <span class="panel-title">实时能耗监测</span>
          </div>
          <div class="chart-container" ref="energyChartRef"></div>
        </div>

        <!-- AI健康诊断 -->
        <div class="panel surface-panel health-panel eq-fade-in-3">
          <div class="panel-header">
            <el-icon :size="14" color="var(--eq-primary)">
              <FirstAidKit />
            </el-icon>
            <span class="panel-title">AI 健康诊断</span>
            <div class="health-badge">
              <span class="status-dot status-green" style="width: 6px; height: 6px;"></span>
              <span class="badge-text">良好</span>
            </div>
          </div>
          <div class="health-content">
            <!-- 状态分析 -->
            <div class="health-item">
              <el-icon :size="14" color="var(--eq-primary)">
                <Iphone />
              </el-icon>
              <span class="health-text">
                <span class="font-semibold">状态分析</span>：{{ healthDiagnosis.statusAnalysis }}
              </span>
            </div>
            <!-- 风险提示 -->
            <div class="health-item" :class="healthDiagnosis.hasRisk ? 'warning' : 'success'">
              <el-icon :size="14" :color="healthDiagnosis.hasRisk ? 'var(--state-warning)' : 'var(--state-success)'">
                <Warning v-if="healthDiagnosis.hasRisk" />
                <CircleCheck v-else />
              </el-icon>
              <span class="health-text" :class="healthDiagnosis.hasRisk ? 'warning-text' : 'muted-text'">
                <span class="font-semibold">风险提示</span>：{{ healthDiagnosis.riskAnalysis }}
                <span v-if="healthDiagnosis.hasRisk">建议在 <span class="font-mono">{{ healthDiagnosis.riskDays }}天内</span> 安排检查。</span>
              </span>
            </div>
            <!-- 维保建议 -->
            <div class="health-item success">
              <el-icon :size="14" color="var(--state-success)">
                <CircleCheck />
              </el-icon>
              <span class="health-text muted-text">
                <span class="success-label">维保建议</span>：{{ healthDiagnosis.maintenanceAdvice }} 预计剩余寿命约 <span class="highlight">{{ healthDiagnosis.remainingMonths }}个月</span>。
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="right-sidebar">
        <!-- KPI卡片 2x2 -->
        <div class="kpi-grid eq-fade-in-1">
          <div class="kpi-card surface-panel glow-box-amber">
            <el-icon :size="16" color="var(--eq-primary)">
              <Odometer />
            </el-icon>
            <span class="kpi-label text-caption">综合效率 OEE</span>
            <span class="kpi-value glow-amber" style="color: var(--eq-primary);">{{ kpiData.oee }}%</span>
          </div>
          <div class="kpi-card surface-panel">
            <el-icon :size="16" color="var(--eq-muted-foreground)">
              <Clock />
            </el-icon>
            <span class="kpi-label text-caption">累计运行时长</span>
            <span class="kpi-value">{{ formatRunTime(kpiData.runHours) }}</span>
          </div>
          <div class="kpi-card surface-panel" style="border-left: 2px solid var(--state-error);">
            <el-icon :size="16" color="var(--state-error)">
              <Warning />
            </el-icon>
            <span class="kpi-label text-caption">工单故障次数</span>
            <span class="kpi-value" style="color: var(--state-error);">{{ kpiData.faultCount }}</span>
          </div>
          <div class="kpi-card surface-panel">
            <el-icon :size="16" color="var(--eq-muted-foreground)">
              <ShieldCheck />
            </el-icon>
            <span class="kpi-label text-caption">平均故障间隔MTBF</span>
            <span class="kpi-value">{{ formatNumber(kpiData.mtbf) }}<span class="kpi-unit">h</span></span>
          </div>
        </div>

        <!-- 工单处理 -->
        <div class="panel surface-panel workorder-panel">
          <div class="panel-header">
            <el-icon :size="14" color="var(--eq-primary)">
              <Document />
            </el-icon>
            <span class="panel-title">工单处理</span>
            <span class="pending-badge font-mono">{{ processingCount }}处理中</span>
          </div>
          <div class="workorder-list">
            <div class="workorder-item surface-metallic" v-for="(item, index) in workorderList" :key="index" :style="{ borderLeft: '3px solid ' + item.borderColor }">
              <div class="workorder-top">
                <span class="workorder-title">{{ item.title }}</span>
                <span class="workorder-status" :class="item.statusClass">{{ item.statusLabel }}</span>
              </div>
              <span class="workorder-meta font-mono text-caption">优先级: {{ item.priority }} | {{ item.reportTime }} | 责任人: {{ item.assigneeName }}</span>
              <span class="workorder-desc" v-if="item.faultDescription">{{ item.faultDescription }}</span>
            </div>
          </div>
          
          <!-- 工单统计图表 -->
          <div class="workorder-stats">
            <div class="stats-item">
              <div class="pie-chart-wrap" @mouseleave="hoveredTypeIndex = -1">
                <svg viewBox="0 0 100 100" class="pie-svg">
                  <circle cx="50" cy="50" r="35" fill="rgba(245, 158, 11, 0.08)"/>
                  <!-- 动态渲染工单类型饼图扇区 -->
                  <path v-for="(slice, index) in typePieSlices" :key="index"
                        :d="slice.path"
                        :fill="slice.color"
                        class="pie-slice"
                        :class="'pie-slice-' + (index + 1)"
                        @mouseenter="hoveredTypeIndex = index"
                        :style="{ transform: hoveredTypeIndex === index ? slice.translate : 'none' }"/>
                  <circle cx="50" cy="50" r="18" fill="rgba(15, 23, 42, 0.9)"/>
                  <text x="50" y="47" text-anchor="middle" class="pie-center-value">{{ typePieTotal }}</text>
                  <text x="50" y="57" text-anchor="middle" class="pie-center-label">总计</text>
                </svg>
                <div v-if="hoveredTypeIndex >= 0" class="pie-tooltip">
                  <span class="tooltip-dot" :style="{ background: typePieData[hoveredTypeIndex]?.color }"></span>
                  <span class="tooltip-label">{{ typePieData[hoveredTypeIndex]?.label }}</span>
                  <span class="tooltip-value font-mono">{{ typePieData[hoveredTypeIndex]?.value }}</span>
                </div>
                <div class="stats-title">
                  <span class="stats-dot" style="background: var(--eq-primary);"></span>
                  <span class="stats-label">工单类型</span>
                </div>
              </div>
            </div>
            
            <div class="stats-item">
              <div class="pie-chart-wrap" @mouseleave="hoveredStatusIndex = -1">
                <svg viewBox="0 0 100 100" class="pie-svg">
                  <circle cx="50" cy="50" r="35" fill="rgba(34, 197, 94, 0.08)"/>
                  <!-- 动态渲染工单状态饼图扇区 -->
                  <path v-for="(slice, index) in statusPieSlices" :key="index"
                        :d="slice.path"
                        :fill="slice.color"
                        class="pie-slice"
                        :class="'pie-slice-' + (index + 1)"
                        @mouseenter="hoveredStatusIndex = index"
                        :style="{ transform: hoveredStatusIndex === index ? slice.translate : 'none' }"/>
                  <circle cx="50" cy="50" r="18" fill="rgba(15, 23, 42, 0.9)"/>
                  <text x="50" y="47" text-anchor="middle" class="pie-center-value success">{{ completionRate }}%</text>
                  <text x="50" y="57" text-anchor="middle" class="pie-center-label">完成率</text>
                </svg>
                <div v-if="hoveredStatusIndex >= 0" class="pie-tooltip">
                  <span class="tooltip-dot" :style="{ background: statusPieData[hoveredStatusIndex]?.color }"></span>
                  <span class="tooltip-label">{{ statusPieData[hoveredStatusIndex]?.label }}</span>
                  <span class="tooltip-value font-mono">{{ statusPieData[hoveredStatusIndex]?.value }}</span>
                </div>
                <div class="stats-title">
                  <span class="stats-dot" style="background: var(--state-success);"></span>
                  <span class="stats-label">工单状态</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useFullscreen } from '@vueuse/core'
import * as echarts from 'echarts'
import deviceImg from '@/assets/images/dms/device.jpg'
import { listDashboardEquipment, getEquipmentDashboard } from '@/api/dms/dashboard'

const route = useRoute()

// ========== 全屏控制 ==========
const dashboardRef = ref(null)
const { isFullscreen, toggle: toggleFullscreen } = useFullscreen(dashboardRef)

// ========== 响应式数据 ==========
const selectedDevice = ref(null)
const currentDate = ref('')
const currentTime = ref('')
const energyChartRef = ref(null)
let energyChart = null
let timer = null
const loading = ref(false)

const deviceImage = deviceImg

// 设备列表
const equipmentList = ref([])

// 设备基本信息
const deviceInfo = reactive({
  equipmentId: null,
  equipmentCode: '',
  equipmentName: '',
  assetCode: '',
  categoryName: '',
  model: '',
  serialNumber: '',
  manufacturer: '',
  supplier: '',
  purchaseDate: '',
  installDate: '',
  retireDate: '',
  deptName: '',
  installLocation: '',
  equipmentStatus: '',
  equipmentLevel: '',
  warrantyDate: '',
  responsibleName: ''
})

// 设备等级字典
const { dms_equipment_level } = useDict('dms_equipment_level')

// 设备状态
const deviceStatusText = ref('加载中')
const deviceStatusType = ref('stopped')
const deviceStatusEn = ref('LOADING')

// 生命周期
const lifecycleStages = ref([])
const lifecycleProgress = ref(0)

// 备件更换
const partsList = ref([])

// 维护费用
const costData = ref([])
const totalCost = ref(0)
const partsCost = ref(0)
const repairCost = ref(0)
const costTrend = ref(0)

// 能耗数据
const energyData = ref([])

// KPI
const kpiData = reactive({
  oee: 0,
  runHours: 0,
  faultCount: 0,
  mtbf: 0
})

// 仪表盘
const gauges = reactive({
  loadRate: 0,
  temperature: 0,
  speed: 0,
  oee: 0
})

// 健康诊断
const healthDiagnosis = reactive({
  installDate: '--',
  runHours: 0,
  hasRisk: false,
  riskDays: 0,
  remainingMonths: 0
})

// 工单
const workorderList = ref([])
const processingCount = ref(0)

// 饼图
const hoveredTypeIndex = ref(-1)
const hoveredStatusIndex = ref(-1)
const typePieData = ref([
  { label: '故障维修', value: '0', color: 'var(--eq-primary)' },
  { label: '定期维保', value: '0', color: 'var(--state-success)' },
  { label: '备件更换', value: '0', color: 'var(--state-purple)' },
  { label: '巡检', value: '0', color: 'var(--state-info)' }
])
const statusPieData = ref([
  { label: '已完成', value: '0', color: 'var(--state-success)' },
  { label: '处理中', value: '0', color: 'var(--state-warning)' },
  { label: '待处理', value: '0', color: 'var(--state-error)' }
])
const typePieTotal = ref(0)
const completionRate = ref(0)

// 下次维保日期
const nextPmDate = ref('--')

// ========== 计算属性 ==========
// 设备等级标签
const equipmentLevelText = computed(() => {
  if (deviceInfo.equipmentLevel === '' || deviceInfo.equipmentLevel == null) return '--'
  const item = dms_equipment_level.value.find(d => d.value == deviceInfo.equipmentLevel)
  return item ? item.label : deviceInfo.equipmentLevel
})

// 设备等级颜色
const equipmentLevelColor = computed(() => {
  const map = { '0': 'var(--state-error)', '1': 'var(--state-warning)', '2': 'var(--state-success)', '3': 'var(--state-info)' }
  return map[deviceInfo.equipmentLevel] || 'var(--state-success)'
})

const statusDotClass = computed(() => {
  const map = { running: 'status-green', stopped: 'status-amber', fault: 'status-red' }
  return map[deviceStatusType.value] || 'status-amber'
})

const partsCostPercent = computed(() => {
  if (totalCost.value === 0) return 50
  return Math.round(partsCost.value / totalCost.value * 100)
})

// 计算工单类型饼图扇区
const typePieSlices = computed(() => {
  // 只保留值大于0的数据项
  const data = typePieData.value.filter(item => parseInt(item.value) > 0)
  const total = data.reduce((sum, item) => sum + (parseInt(item.value) || 0), 0)
  if (total === 0) {
    // 没有数据时显示占位圆
    return [{
      path: describeArc(50, 50, 35, 0, 359.99),
      color: 'rgba(255,255,255,0.1)',
      translate: 'none'
    }]
  }

  let currentAngle = -90 // 从12点钟方向开始
  const slices = []
  const translates = [
    'translate(2px, -2px)',
    'translate(2px, 2px)',
    'translate(-2px, 2px)',
    'translate(-2px, -2px)'
  ]

  data.forEach((item, index) => {
    const value = parseInt(item.value) || 0
    const angle = (value / total) * 360
    const endAngle = currentAngle + angle

    slices.push({
      path: describeArc(50, 50, 35, currentAngle, endAngle),
      color: item.color,
      translate: translates[index % translates.length]
    })

    currentAngle = endAngle
  })

  return slices
})

// 计算工单状态饼图扇区
const statusPieSlices = computed(() => {
  // 只保留值大于0的数据项
  const data = statusPieData.value.filter(item => parseInt(item.value) > 0)
  const total = data.reduce((sum, item) => sum + (parseInt(item.value) || 0), 0)
  if (total === 0) {
    // 没有数据时显示占位圆
    return [{
      path: describeArc(50, 50, 35, 0, 359.99),
      color: 'rgba(255,255,255,0.1)',
      translate: 'none'
    }]
  }

  let currentAngle = -90 // 从12点钟方向开始
  const slices = []
  const translates = [
    'translate(2px, -2px)',
    'translate(-2px, 0px)',
    'translate(0px, -2px)'
  ]

  data.forEach((item, index) => {
    const value = parseInt(item.value) || 0
    const angle = (value / total) * 360
    const endAngle = currentAngle + angle

    slices.push({
      path: describeArc(50, 50, 35, currentAngle, endAngle),
      color: item.color,
      translate: translates[index % translates.length]
    })

    currentAngle = endAngle
  })

  return slices
})

// 计算圆弧路径的辅助函数
function polarToCartesian(centerX, centerY, radius, angleInDegrees) {
  const angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0
  return {
    x: centerX + (radius * Math.cos(angleInRadians)),
    y: centerY + (radius * Math.sin(angleInRadians))
  }
}

function describeArc(x, y, radius, startAngle, endAngle) {
  const start = polarToCartesian(x, y, radius, endAngle)
  const end = polarToCartesian(x, y, radius, startAngle)
  const largeArcFlag = endAngle - startAngle <= 180 ? '0' : '1'
  
  return [
    'M', x, y,
    'L', start.x, start.y,
    'A', radius, radius, 0, largeArcFlag, 0, end.x, end.y,
    'Z'
  ].join(' ')
}

// ========== 工具函数 ==========
function padZero(n) {
  return n < 10 ? '0' + n : n
}

function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  return Number(num).toLocaleString('en-US')
}

function formatSpeed(speed) {
  if (speed >= 1000) {
    return (speed / 1000).toFixed(0) + 'K'
  }
  return String(speed)
}

function formatRunTime(hours) {
  if (!hours || hours <= 0) return '0h'
  if (hours < 24) return hours + 'h'
  const days = Math.floor(hours / 24)
  const remainingHours = hours % 24
  if (remainingHours === 0) return days + '天'
  return days + '天' + remainingHours + 'h'
}

function updateDateTime() {
  const now = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  currentDate.value = `${now.getFullYear()}-${padZero(now.getMonth() + 1)}-${padZero(now.getDate())} ${weekDays[now.getDay()]}`
  currentTime.value = `${padZero(now.getHours())}:${padZero(now.getMinutes())}:${padZero(now.getSeconds())}`
}

// ========== 数据加载 ==========
function loadEquipmentList() {
  listDashboardEquipment().then(res => {
    equipmentList.value = res.data || []
    if (equipmentList.value.length > 0) {
      // 优先从 URL query 参数读取 equipmentId
      const queryId = route.query.equipmentId
      const matchItem = queryId
        ? equipmentList.value.find(e => String(e.equipmentId) === String(queryId))
        : null
      selectedDevice.value = matchItem
        ? matchItem.equipmentId
        : equipmentList.value[0].equipmentId
      loadDashboardData()
    }
  })
}

function loadDashboardData() {
  if (!selectedDevice.value) return
  loading.value = true
  getEquipmentDashboard(selectedDevice.value).then(res => {
    const data = res.data || {}
    applyDashboardData(data)
    loading.value = false
    nextTick(() => {
      initEnergyChart()
    })
  }).catch(() => {
    loading.value = false
  })
}

function applyDashboardData(data) {
  // 设备信息
  const di = data.deviceInfo || {}
  Object.assign(deviceInfo, di)

  // 设备状态
  deviceStatusText.value = data.deviceStatusText || '运行中'
  deviceStatusType.value = data.deviceStatusType || 'running'
  const enMap = { running: 'RUNNING', stopped: 'STOPPED', fault: 'FAULT' }
  deviceStatusEn.value = enMap[deviceStatusType.value] || 'RUNNING'

  // 生命周期
  lifecycleStages.value = data.lifecycleStages || []
  lifecycleProgress.value = data.lifecycleProgress || 0

  // 备件更换
  partsList.value = data.partsList || []

  // 维护费用
  costData.value = data.costData || []
  totalCost.value = data.totalCost || 0
  partsCost.value = data.partsCost || 0
  repairCost.value = data.repairCost || 0
  // 计算趋势（简单：如果有费用则显示固定趋势值）
  costTrend.value = totalCost.value > 0 ? 12.8 : 0

  // 能耗数据
  energyData.value = data.energyData || []

  // KPI
  kpiData.oee = data.oee || 0
  kpiData.runHours = data.runHours || 0
  kpiData.faultCount = data.faultCount || 0
  kpiData.mtbf = data.mtbf || 0

  // 仪表盘
  const g = data.gauges || {}
  gauges.loadRate = g.loadRate || 0
  gauges.temperature = g.temperature || 0
  gauges.speed = g.speed || 0
  gauges.oee = g.oee || 0

  // 健康诊断
  const hd = data.healthDiagnosis || {}
  Object.assign(healthDiagnosis, hd)

  // 工单
  workorderList.value = data.workorderList || []
  processingCount.value = data.processingCount || 0

  // 饼图
  typePieData.value = data.typePieData || typePieData.value
  statusPieData.value = data.statusPieData || statusPieData.value
  typePieTotal.value = data.typePieTotal || 0
  completionRate.value = data.completionRate || 0

  // 下次维保日期 - 暂用质保日期或默认
  nextPmDate.value = deviceInfo.warrantyDate || '--'
}

function onDeviceChange() {
  loadDashboardData()
}

// ========== 能耗图表 ==========
function initEnergyChart() {
  if (!energyChartRef.value) return

  if (energyChart) {
    energyChart.dispose()
  }
  energyChart = echarts.init(energyChartRef.value)

  const labels = energyData.value.map(d => d.time)
  const elecData = energyData.value.map(d => d.elec)
  const waterData = energyData.value.map(d => d.water)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(20, 23, 31, 0.95)',
      borderColor: 'rgba(245, 158, 11, 0.3)',
      textStyle: {
        color: '#dde0e5',
        fontSize: 11,
        fontFamily: 'JetBrains Mono, monospace'
      }
    },
    legend: {
      data: ['电力消耗(kWh)', '用水量(m³)'],
      textStyle: {
        color: '#8b91a1',
        fontSize: 10,
        fontFamily: 'JetBrains Mono, monospace'
      },
      itemWidth: 12,
      itemHeight: 8,
      top: 5,
      right: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '20%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLabel: {
        color: '#636a7e',
        fontSize: 9
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(42, 47, 62, 0.5)'
        }
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: 'rgba(42, 47, 62, 0.5)'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#636a7e',
        fontSize: 9
      },
      axisLine: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(42, 47, 62, 0.5)'
        }
      }
    },
    series: [
      {
        name: '电力消耗(kWh)',
        type: 'line',
        smooth: true,
        data: elecData,
        lineStyle: {
          color: '#f59e0b',
          width: 1.5
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 158, 11, 0.2)' },
            { offset: 1, color: 'rgba(245, 158, 11, 0.02)' }
          ])
        },
        showSymbol: false,
        emphasis: {
          focus: 'series'
        }
      },
      {
        name: '用水量(m³)',
        type: 'line',
        smooth: true,
        data: waterData,
        lineStyle: {
          color: '#3b82f6',
          width: 1.5
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.2)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.02)' }
          ])
        },
        showSymbol: false,
        emphasis: {
          focus: 'series'
        }
      }
    ]
  }

  energyChart.setOption(option)
}

function handleResize() {
  energyChart?.resize()
}

// ========== 全屏状态变化时自适应图表 ==========
watch(isFullscreen, () => {
  nextTick(() => {
    setTimeout(() => {
      handleResize()
    }, 200)
  })
})

// ========== 生命周期 ==========
onMounted(() => {
  updateDateTime()
  timer = setInterval(updateDateTime, 1000)

  loadEquipmentList()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (energyChart) {
    energyChart.dispose()
    energyChart = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--eq-background);
  overflow: hidden;
}

/* 顶部标题栏 */
.top-bar {
  height: 58px;
  background: linear-gradient(90deg, rgba(10, 12, 18, 0.98), rgba(20, 23, 31, 0.95));
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  flex-shrink: 0;
  
  .top-bar-left {
    display: flex;
    align-items: center;
    padding: 0 16px;
    gap: 12px;
    flex-shrink: 0;
    min-width: 280px;
    
    .decoration-line {
      width: 4px;
      height: 32px;
      border-radius: 2px;
      background: linear-gradient(180deg, var(--eq-primary), rgba(245, 158, 11, 0.1));
      flex-shrink: 0;
    }
    
    .sub-title {
      font-family: var(--eq-font-display);
      font-size: 10px;
      font-weight: 500;
      letter-spacing: 0.35em;
      text-transform: uppercase;
      color: var(--eq-primary);
      opacity: 0.7;
      white-space: nowrap;
    }
  }
  
  .top-bar-center {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    
    .main-title {
      font-family: var(--eq-font-display);
      font-size: 18px;
      font-weight: 800;
      letter-spacing: 0.18em;
      white-space: nowrap;
      background: linear-gradient(135deg, 
        #fef3c7 0%, 
        #fcd34d 15%, 
        #f59e0b 35%, 
        #fbbf24 50%, 
        #f59e0b 65%, 
        #fcd34d 85%, 
        #fef3c7 100%);
      background-size: 200% auto;
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
      text-shadow: none;
      filter: drop-shadow(0 0 12px rgba(245, 158, 11, 0.5)) drop-shadow(0 0 25px rgba(245, 158, 11, 0.25));
      animation: title-shimmer 3s ease-in-out infinite, title-glow-pulse 2s ease-in-out infinite;
      position: relative;
      
      &::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 10%;
        width: 80%;
        height: 1px;
        background: linear-gradient(90deg, 
          transparent, 
          rgba(245, 158, 11, 0.6), 
          rgba(252, 211, 77, 0.9), 
          rgba(245, 158, 11, 0.6), 
          transparent);
        animation: title-underline 2.5s ease-in-out infinite;
      }
    }
  }
  
  .top-bar-right {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 0 16px;
    flex-shrink: 0;
    min-width: 280px;
    justify-content: flex-end;
    
    .fullscreen-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      cursor: pointer;
      border-radius: 4px;
      transition: all 0.3s ease;

      .svg-icon {
        width: 18px;
        height: 18px;
        color: var(--eq-primary);
        opacity: 0.8;
      }

      &:hover {
        background: rgba(245, 158, 11, 0.12);

        .svg-icon {
          opacity: 1;
        }
      }
    }
    
    .datetime {
      display: flex;
      align-items: center;
      gap: 6px;
      font-family: var(--eq-font-mono);
      font-size: 12px;
      white-space: nowrap;
      
      .date-text {
        color: var(--eq-muted-foreground);
      }
      
      .time-text {
        color: var(--eq-foreground);
      }
    }
    
    .right-decoration {
      display: flex;
      align-items: center;
      gap: 8px;
      padding-left: 8px;
      
      .decoration-line-right {
        width: 4px;
        height: 32px;
        border-radius: 2px;
        background: linear-gradient(180deg, rgba(245, 158, 11, 0.1), var(--eq-primary));
      }
    }
  }
  
  .bottom-glow-line {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--eq-primary), transparent);
    opacity: 0.4;
  }
}

/* 设备信息栏 */
.device-info-bar {
  height: 32px;
  background: rgba(245, 158, 11, 0.03);
  border-bottom: 1px solid rgba(245, 158, 11, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  
  .device-info-left {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .device-select {
      width: auto;
      
      :deep(.el-select__wrapper) {
        background: rgba(245, 158, 11, 0.08);
        border: 1px solid rgba(245, 158, 11, 0.15);
        color: var(--eq-primary);
        border-radius: 4px;
      }
      
      :deep(.el-select__placeholder),
      :deep(.el-select__selected-item) {
        color: var(--eq-primary);
        font-size: 12px;
      }
    }
    
    .device-code {
      font-size: 12px;
      color: var(--eq-muted-foreground);
    }
    
    .divider {
      color: var(--eq-border);
      font-size: 12px;
    }
    
    .dept-name {
      font-size: 12px;
      color: var(--eq-muted-foreground);
    }
  }
  
  .device-status {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 2px 10px;
    border-radius: 999px;
    background: rgba(34, 197, 94, 0.1);
    border: 1px solid rgba(34, 197, 94, 0.2);
    
    .status-text {
      font-family: var(--eq-font-display);
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.05em;
      color: var(--state-success);
    }
  }
}

/* 状态点 */
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-green {
  background: var(--state-success);
  box-shadow: 0 0 6px rgba(34, 197, 94, 0.6);
  animation: status-pulse-green 2s ease-in-out infinite;
}

.status-amber {
  background: var(--state-warning);
  box-shadow: 0 0 6px rgba(245, 158, 11, 0.6);
  animation: status-pulse-amber 2s ease-in-out infinite;
}

.status-red {
  background: var(--state-error);
  box-shadow: 0 0 6px rgba(239, 68, 68, 0.6);
  animation: status-pulse-red 2s ease-in-out infinite;
}

@keyframes status-pulse-green {
  0%, 100% { box-shadow: 0 0 4px rgba(34, 197, 94, 0.4); }
  50% { box-shadow: 0 0 12px rgba(34, 197, 94, 0.8); }
}

@keyframes status-pulse-amber {
  0%, 100% { box-shadow: 0 0 4px rgba(245, 158, 11, 0.4); }
  50% { box-shadow: 0 0 12px rgba(245, 158, 11, 0.8); }
}

@keyframes status-pulse-red {
  0%, 100% { box-shadow: 0 0 4px rgba(239, 68, 68, 0.4); }
  50% { box-shadow: 0 0 12px rgba(239, 68, 68, 0.8); }
}

/* 主体布局 */
.main-content {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(230px, 25fr) minmax(400px, 48fr) minmax(250px, 27fr);
  gap: 12px;
  padding: 12px;
  overflow: hidden;
  width: 100%;
}

/* 左侧边栏 */
.left-sidebar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  min-width: 0;
  overflow-y: auto;
  
  &::-webkit-scrollbar { display: none; }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 右侧边栏 */
.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  overflow-y: auto;
  min-width: 0;
  
  &::-webkit-scrollbar { display: none; }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 中央区域 */
.center-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  min-width: 0;
  width: 100%;
}

/* 面板通用样式 */
.panel {
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-shrink: 0;
  
  .panel-title {
    font-family: var(--eq-font-display);
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.05em;
    color: var(--eq-primary);
  }
}

/* 时间线面板 */
.timeline-panel {
  flex: 0.9 1 0;
  min-height: 0;
  
  .timeline {
    display: flex;
    flex-direction: column;
    gap: 12px;
    flex: 1;
    min-height: 0;
  }
  
  .timeline-progress {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 10px 12px;
    background: rgba(245, 158, 11, 0.05);
    border-radius: 8px;
    border: 1px solid rgba(245, 158, 11, 0.1);
    
    .progress-bar {
      position: relative;
      height: 6px;
      background: rgba(245, 158, 11, 0.1);
      border-radius: 3px;
      overflow: visible;
      
      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, var(--eq-primary), var(--state-success));
        border-radius: 3px;
        transition: width 0.8s ease;
      }
      
      .progress-glow {
        position: absolute;
        top: 50%;
        transform: translate(-50%, -50%);
        width: 14px;
        height: 14px;
        background: var(--eq-primary);
        border-radius: 50%;
        box-shadow: 0 0 12px rgba(245, 158, 11, 0.7), 0 0 24px rgba(245, 158, 11, 0.3);
        animation: progress-glow 2s ease-in-out infinite;
      }
    }
    
    .progress-text {
      font-size: 12px;
      font-weight: 600;
      color: var(--eq-primary);
      text-align: right;
    }
  }
  
  .timeline-stages {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    flex: 1;
    position: relative;
    padding: 8px 4px 4px;
  }
  
  .timeline-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    position: relative;
    flex: 1;
  }
  
  .timeline-node {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    z-index: 2;
    flex-shrink: 0;
    
    .node-glow {
      position: absolute;
      inset: -4px;
      border-radius: 50%;
      opacity: 0.4;
      z-index: -1;
    }
    
    .node-inner {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--eq-card);
      border: 2px solid var(--eq-primary);
    }
    
    &.done {
      .node-glow {
        background: var(--eq-primary);
        box-shadow: 0 0 12px rgba(245, 158, 11, 0.5);
      }
      
      .node-inner {
        background: var(--eq-primary);
        border-color: var(--eq-primary);
        box-shadow: inset 0 0 8px rgba(255, 255, 255, 0.2);
      }
    }
    
    &.active {
      .node-glow {
        background: var(--state-success);
        box-shadow: 0 0 20px rgba(34, 197, 94, 0.6);
        animation: node-pulse 2s ease-in-out infinite;
      }
      
      .node-inner {
        background: var(--state-success);
        border-color: var(--state-success);
        box-shadow: inset 0 0 10px rgba(255, 255, 255, 0.3);
      }
    }
    
    &.future {
      .node-glow {
        background: var(--eq-muted-foreground);
        opacity: 0.2;
      }
      
      .node-inner {
        background: transparent;
        border-color: var(--eq-muted-foreground);
        border-style: dashed;
      }
    }
  }
  
  .timeline-label {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    text-align: center;
  }
  
  .label-text {
    font-size: 11px;
    color: var(--eq-foreground);
    font-weight: 500;
    
    &.active-label {
      font-weight: 700;
    }
    
    &.status-running {
      color: var(--state-success);
    }
    
    &.status-stopped {
      color: var(--state-warning);
    }
    
    &.status-fault {
      color: var(--state-error);
    }
  }
  
  .label-date {
    font-size: 9px;
    color: var(--eq-muted-foreground);
    
    &.active-label {
      opacity: 0.8;
    }
    
    &.status-running {
      color: var(--state-success);
    }
    
    &.status-stopped {
      color: var(--state-warning);
    }
    
    &.status-fault {
      color: var(--state-error);
    }
  }
  
  .timeline-node.active.status-running {
    .node-glow {
      background: var(--state-success);
      box-shadow: 0 0 20px rgba(34, 197, 94, 0.6);
    }
    .node-inner {
      background: var(--state-success);
      border-color: var(--state-success);
    }
  }
  
  .timeline-node.active.status-stopped {
    .node-glow {
      background: var(--state-warning);
      box-shadow: 0 0 20px rgba(245, 158, 11, 0.6);
    }
    .node-inner {
      background: var(--state-warning);
      border-color: var(--state-warning);
    }
  }
  
  .timeline-node.active.status-fault {
    .node-glow {
      background: var(--state-error);
      box-shadow: 0 0 20px rgba(239, 68, 68, 0.6);
      animation: fault-blink 1s ease-in-out infinite;
    }
    .node-inner {
      background: var(--state-error);
      border-color: var(--state-error);
    }
  }
  
  .timeline-connector {
    position: absolute;
    top: 18px;
    left: calc(50% + 18px);
    right: calc(-50% + 18px);
    height: 2px;
    z-index: 1;
  }
  
  .connector-line {
    width: 100%;
    height: 100%;
    background: var(--eq-border);
    border-radius: 1px;
    position: relative;
    
    &.done {
      background: linear-gradient(90deg, var(--eq-primary), var(--eq-primary));
      box-shadow: 0 0 4px rgba(245, 158, 11, 0.3);
    }
    
    &.active {
      background: linear-gradient(90deg, var(--eq-primary), var(--state-success));
      box-shadow: 0 0 4px rgba(34, 197, 94, 0.3);
    }
    
    &.future {
      background: linear-gradient(90deg, var(--eq-border), var(--eq-muted-foreground));
    }
  }
}

@keyframes node-pulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.3); opacity: 0.9; }
}

@keyframes progress-glow {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.3); }
}

@keyframes fault-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* 备件更换面板 */
.parts-panel {
  flex: 0.4 1 0;
  min-height: 0;
  
  .parts-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    flex: 1;
    justify-content: center;
    
    .parts-empty {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      
      .empty-text {
        font-size: 13px;
        color: var(--eq-muted-foreground);
        font-style: italic;
      }
    }
    
    .parts-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      
      .parts-name {
        font-size: 12px;
        color: var(--eq-card-foreground);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .parts-info {
        font-size: 11px;
        flex-shrink: 0;
        white-space: nowrap;
        color: var(--eq-foreground);
        
        .parts-qty {
          color: var(--eq-primary);
          font-weight: 600;
        }
      }
    }
  }
}

/* 维护费用面板 */
.cost-panel {
  flex: 1.2 1 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  
  .panel-badge {
    margin-left: auto;
    font-size: 9px;
    padding: 1px 6px;
    background: rgba(245, 158, 11, 0.15);
    color: var(--eq-primary);
    border-radius: 8px;
    border: 1px solid rgba(245, 158, 11, 0.2);
  }
  
  .cost-summary {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 4px 0 6px;
    
    .cost-total {
      display: flex;
      flex-direction: column;
      gap: 1px;
      
      .total-label {
        font-size: 9px;
        color: var(--eq-muted-foreground);
      }
      
      .total-value {
        font-size: 18px;
        font-weight: 700;
        color: var(--eq-primary);
        text-shadow: 0 0 20px rgba(245, 158, 11, 0.4);
      }
    }
    
    .cost-trend {
      display: flex;
      align-items: center;
      gap: 3px;
      padding: 3px 8px;
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid rgba(239, 68, 68, 0.2);
      border-radius: 12px;
      
      .trend-icon {
        font-size: 12px;
        color: var(--state-error);
        animation: trend-bounce 1.5s ease-in-out infinite;
      }
      
      .trend-text {
        font-size: 11px;
        font-weight: 600;
        color: var(--state-error);
      }
    }
  }
  
  .cost-chart {
    position: relative;
    flex: 1;
    min-height: 50px;
    padding: 4px 0;
    
    .chart-grid {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 16px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .grid-line {
        width: 100%;
        height: 1px;
        background: rgba(245, 158, 11, 0.06);
        position: relative;
        
        .grid-label {
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          font-size: 8px;
          color: rgba(245, 158, 11, 0.3);
        }
      }
    }
    
    .bar-group {
      position: relative;
      display: flex;
      align-items: flex-end;
      justify-content: space-around;
      height: 100%;
      padding-bottom: 16px;
      padding-left: 24px;
    }
    
    .bar-col {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-end;
      height: 100%;
      flex: 1;
      animation: bar-fade-in 0.6s ease-out forwards;
      opacity: 0;
      cursor: pointer;
      transition: transform 0.3s ease;
      
      &:hover {
        transform: scaleY(1.03);
        
        .bar-value {
          opacity: 1;
          transform: translateY(-2px);
        }
        
        .bar-fill {
          filter: brightness(1.15);
          
          .bar-shine {
            animation: shine 0.8s ease-in-out;
          }
        }
      }
      
      .bar-wrapper {
        position: relative;
        width: 60%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: flex-end;
        height: 100%;
      }
      
      .bar-value {
        font-size: 8px;
        color: var(--eq-primary);
        font-weight: 600;
        margin-bottom: 3px;
        opacity: 0.85;
        transition: all 0.3s ease;
        text-shadow: 0 0 8px rgba(245, 158, 11, 0.5);
      }
      
      .bar-fill {
        width: 100%;
        border-radius: 3px 3px 0 0;
        position: relative;
        transition: height 0.8s cubic-bezier(0.34, 1.56, 0.64, 1), filter 0.3s ease;
        overflow: hidden;
        animation: bar-grow-up 1s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
        animation-delay: var(--bar-delay, 0s);
        transform-origin: bottom;
        
        .bar-gradient {
          position: absolute;
          inset: 0;
          background: linear-gradient(180deg, 
            #fef3c7 0%, 
            #fcd34d 15%, 
            #f59e0b 45%, 
            #d97706 75%, 
            #92400e 100%);
          border-radius: 3px 3px 0 0;
        }
        
        .bar-highlight {
          position: absolute;
          top: 0;
          left: 8%;
          width: 25%;
          height: 100%;
          background: linear-gradient(180deg, 
            rgba(255, 255, 255, 0.6) 0%, 
            rgba(255, 255, 255, 0.1) 40%, 
            transparent 100%);
          border-radius: 3px 0 0 0;
        }
        
        .bar-glow-top {
          position: absolute;
          top: -3px;
          left: 50%;
          transform: translateX(-50%);
          width: 80%;
          height: 6px;
          background: radial-gradient(ellipse at center, 
            rgba(252, 211, 77, 0.8) 0%, 
            rgba(245, 158, 11, 0.4) 40%, 
            transparent 70%);
          filter: blur(2px);
          animation: glow-pulse 2s ease-in-out infinite;
        }
        
        .bar-shine {
          position: absolute;
          top: 0;
          left: -100%;
          width: 50%;
          height: 100%;
          background: linear-gradient(90deg, 
            transparent 0%, 
            rgba(255, 255, 255, 0.4) 50%, 
            transparent 100%);
        }
      }
      
      .bar-base {
        width: 120%;
        height: 4px;
        background: radial-gradient(ellipse at center, 
          rgba(245, 158, 11, 0.35) 0%, 
          rgba(245, 158, 11, 0.15) 50%, 
          transparent 80%);
        filter: blur(2px);
        margin-top: -2px;
      }
      
      .bar-month {
        position: absolute;
        bottom: 0;
        font-size: 9px;
        color: var(--eq-muted-foreground);
      }
    }
  }
  
  .cost-breakdown {
    display: flex;
    flex-direction: column;
    gap: 5px;
    padding-top: 8px;
    margin-top: 6px;
    border-top: 1px solid rgba(245, 158, 11, 0.1);
    
    .breakdown-item {
      display: flex;
      flex-direction: column;
      gap: 3px;
      
      .breakdown-bar {
        height: 4px;
        background: rgba(245, 158, 11, 0.08);
        border-radius: 2px;
        overflow: hidden;
        
        .breakdown-bar-fill {
          height: 100%;
          border-radius: 2px;
          animation: bar-fill-grow 1s ease-out forwards;
          transform-origin: left;
        }
      }
      
      .breakdown-info {
        display: flex;
        align-items: center;
        gap: 5px;
      }
      
      .breakdown-dot {
        width: 5px;
        height: 5px;
        border-radius: 50%;
        flex-shrink: 0;
        box-shadow: 0 0 6px currentColor;
      }
      
      .breakdown-label {
        font-size: 9px;
        color: var(--eq-muted-foreground);
        flex: 1;
      }
      
      .breakdown-value {
        font-size: 9px;
        color: var(--eq-foreground);
        font-weight: 600;
        margin-left: auto;
      }
    }
  }
  
  .cost-monthly {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
    padding-top: 8px;
    margin-top: 6px;
    border-top: 1px solid rgba(245, 158, 11, 0.08);
    
    .monthly-item {
      display: flex;
      flex-direction: column;
      gap: 2px;
      align-items: center;
      padding: 6px 4px;
      background: rgba(245, 158, 11, 0.04);
      border-radius: 6px;
      border: 1px solid rgba(245, 158, 11, 0.08);
      
      .monthly-label {
        font-size: 9px;
        color: var(--eq-muted-foreground);
      }
      
      .monthly-value {
        font-size: 11px;
        font-weight: 600;
        color: var(--eq-foreground);
        
        &.used {
          color: var(--eq-primary);
        }
        
        &.remain {
          color: var(--state-success);
        }
      }
    }
  }
}

@keyframes bar-fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bar-grow-up {
  from {
    transform: scaleY(0);
  }
  to {
    transform: scaleY(1);
  }
}

@keyframes bar-fill-grow {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

@keyframes glow-pulse {
  0%, 100% {
    opacity: 0.6;
    transform: translateX(-50%) scale(1);
  }
  50% {
    opacity: 1;
    transform: translateX(-50%) scale(1.2);
  }
}

@keyframes shine {
  0% {
    left: -100%;
  }
  100% {
    left: 150%;
  }
}

@keyframes trend-bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

/* 雷达面板 */
.radar-panel {
  flex: 1.5 1 0;
  min-height: 0;
  position: relative;
  overflow: hidden;
  padding: 0;
  
  .hero-border-anim {
    position: absolute;
    inset: 0;
    border-radius: var(--radius-md);
    z-index: 1;
    pointer-events: none;
    background: conic-gradient(from 0deg, transparent 60%, rgba(245, 158, 11, 0.3) 78%, transparent 96%);
    animation: rotate-border 4s linear infinite;
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask-composite: xor;
    -webkit-mask-composite: xor;
    padding: 2px;
  }
  
  @property --rot-angle {
    syntax: '<angle>';
    initial-value: 0deg;
    inherits: false;
  }
  
  @keyframes rotate-border {
    to {
      --rot-angle: 360deg;
    }
  }
  
  .radar-bg {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    opacity: 0.3;
  }
  
  .device-3d-img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: contain;
    opacity: 0.3;
    transform: scale(0.85);
  }
  
  .hologram-radar {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 220px;
    height: 220px;
    z-index: 10;
    
    .hologram-svg {
      width: 100%;
      height: 100%;
      filter: drop-shadow(0 0 25px rgba(245, 158, 11, 0.4));
      
      .ring-rotate-cw {
        transform-origin: 110px 110px;
        animation: rotate-cw 20s linear infinite;
      }
      
      .ring-rotate-ccw {
        transform-origin: 110px 110px;
        animation: rotate-ccw 15s linear infinite;
      }
      
      .sweep-beam {
        transform-origin: 110px 110px;
        animation: rotate-cw 6s linear infinite;
      }
      
      /* 主齿轮 - 中央大齿轮 顺时针慢速 */
      .main-gear {
        transform-origin: 110px 110px;
        animation: rotate-cw 25s linear infinite;
      }
      
      /* 副齿轮1 - 左上 逆时针快速 */
      .gear-1 {
        transform-origin: 78px 78px;
        animation: rotate-ccw 6s linear infinite;
      }
      
      /* 副齿轮2 - 右下 顺时针中速 */
      .gear-2 {
        transform-origin: 148px 142px;
        animation: rotate-cw 10s linear infinite;
      }
      
      /* 副齿轮3 - 右上 逆时针快速 */
      .gear-3 {
        transform-origin: 158px 76px;
        animation: rotate-ccw 4s linear infinite;
      }
      
      /* 副齿轮4 - 左下 顺时针中速 */
      .gear-4 {
        transform-origin: 66px 152px;
        animation: rotate-cw 7s linear infinite;
      }
      
      .data-arc-1 {
        transform-origin: 110px 110px;
        animation: rotate-cw 30s linear infinite;
      }
      
      .data-arc-2 {
        transform-origin: 110px 110px;
        animation: rotate-ccw 25s linear infinite;
      }
      
      .particle {
        &.p1 {
          animation: float-p1 3s ease-in-out infinite;
        }
        &.p2 {
          animation: float-p2 2.5s ease-in-out infinite;
        }
        &.p3 {
          animation: float-p3 2s ease-in-out infinite;
        }
        &.p4 {
          animation: float-p4 3.5s ease-in-out infinite;
        }
        &.p5 {
          animation: float-p5 2.8s ease-in-out infinite;
        }
        &.p6 {
          animation: float-p6 3.2s ease-in-out infinite;
        }
      }
    }
  }
  
  @keyframes rotate-cw {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
  
  @keyframes rotate-ccw {
    from { transform: rotate(360deg); }
    to { transform: rotate(0deg); }
  }
  
  @keyframes float-p1 {
    0%, 100% { opacity: 0.6; transform: translateY(0); }
    50% { opacity: 0.1; transform: translateY(-5px); }
  }
  
  @keyframes float-p2 {
    0%, 100% { opacity: 0.5; transform: translateX(0); }
    50% { opacity: 0.1; transform: translateX(5px); }
  }
  
  @keyframes float-p3 {
    0%, 100% { opacity: 0.4; }
    50% { opacity: 0.1; }
  }
  
  @keyframes float-p4 {
    0%, 100% { opacity: 0.5; transform: translateY(0); }
    50% { opacity: 0.15; transform: translateY(-5px); }
  }
  
  @keyframes float-p5 {
    0%, 100% { opacity: 0.7; transform: translate(0, 0); }
    50% { opacity: 0.2; transform: translate(3px, -3px); }
  }
  
  @keyframes float-p6 {
    0%, 100% { opacity: 0.6; transform: translate(0, 0); }
    50% { opacity: 0.15; transform: translate(-2px, 2px); }
  }
  
  /* 顶部仪表 */
  .top-gauges {
    position: absolute;
    top: 14px;
    display: flex;
    gap: 10px;
    z-index: 10;
    
    &.top-left-gauges {
      left: 14px;
    }
    
    &.top-right-gauges {
      right: 14px;
    }
    
    .gauge-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      padding: 6px 8px;
      border-radius: 8px;
      background: rgba(10, 12, 18, 0.85);
      backdrop-filter: blur(8px);
      border: 1px solid rgba(245, 158, 11, 0.12);
      
      .gauge-label {
        font-size: 10px;
        color: var(--eq-muted-foreground);
      }
    }
  }
  
  /* 设备台账信息面板 */
  .device-info-panel {
    position: absolute;
    top: 66%;
    transform: translateY(-50%);
    width: 165px;
    padding: 10px 14px;
    border-radius: 8px;
    background: rgba(10, 12, 18, 0.9);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(245, 158, 11, 0.15);
    z-index: 10;
    
    &.left-info-panel {
      left: 14px;
    }
    
    &.right-info-panel {
      right: 14px;
    }
    
    .info-panel-title {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 8px;
      padding-bottom: 6px;
      border-bottom: 1px solid rgba(245, 158, 11, 0.15);
      
      .title-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: var(--eq-primary);
        box-shadow: 0 0 6px rgba(245, 158, 11, 0.6);
      }
      
      .title-text {
        font-size: 11px;
        font-weight: 600;
        color: var(--eq-primary);
        letter-spacing: 1px;
      }
    }
    
    .info-list {
      display: flex;
      flex-direction: column;
      gap: 5px;
    }
    
    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 6px;
      
      .info-row-label {
        font-size: 10px;
        color: var(--eq-muted-foreground);
        white-space: nowrap;
      }
      
      .info-row-value {
        font-size: 10px;
        color: var(--eq-foreground);
        font-weight: 500;
        text-align: right;
        white-space: nowrap;
      }
    }
  }
  
  .gauge-ring-lg {
    width: 58px;
    height: 58px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    background: conic-gradient(var(--gauge-color) calc(var(--gauge-pct, 50) * 3.6deg), rgba(42, 47, 62, 0.5) 0);
    padding: 3px;
    box-shadow: 0 0 12px rgba(245, 158, 11, 0.1);
    
    &::after {
      content: '';
      position: absolute;
      inset: 3px;
      border-radius: 50%;
      background: var(--eq-card);
    }
    
    .gauge-inner-lg {
      position: relative;
      z-index: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      
      .gauge-value {
        font-family: var(--eq-font-display);
        font-weight: 700;
        font-size: 13px;
      }
    }
  }
  
  /* 底部设备名 */
  .bottom-device-name {
    position: absolute;
    bottom: 12px;
    left: 50%;
    transform: translateX(-50%);
    padding: 6px 16px;
    border-radius: 8px;
    background: rgba(10, 12, 18, 0.85);
    backdrop-filter: blur(6px);
    border: 1px solid rgba(245, 158, 11, 0.2);
    z-index: 10;
    white-space: nowrap;
    
    .device-name-text {
      font-family: var(--eq-font-display);
      font-size: 14px;
      font-weight: 600;
      letter-spacing: 0.05em;
      color: var(--eq-foreground);
      white-space: nowrap;
    }
  }
}

/* 能耗图表面板 */
.energy-panel {
  flex: 1 1 0;
  min-height: 0;
  
  .chart-container {
    flex: 1;
    min-height: 0;
    width: 100%;
  }
}

/* 健康诊断面板 */
.health-panel {
  flex: 0.9 1 0;
  min-height: 0;
  
  .panel-header {
    .health-badge {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 2px 8px;
      border-radius: 4px;
      background: rgba(34, 197, 94, 0.15);
      border: 1px solid rgba(34, 197, 94, 0.3);
      
      .badge-text {
        font-family: var(--eq-font-display);
        font-size: 12px;
        font-weight: 600;
        color: var(--state-success);
      }
    }
  }
  
  .health-content {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 10px;
    
    &::-webkit-scrollbar { display: none; }
    -ms-overflow-style: none;
    scrollbar-width: none;
    
    .health-item {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      
      .health-text {
        font-size: 12px;
        line-height: 1.6;
        color: var(--eq-card-foreground);
        
        .highlight {
          font-family: var(--eq-font-mono);
          color: var(--eq-primary);
        }
        
        &.warning-text {
          color: var(--state-warning);
        }
        
        &.muted-text {
          color: var(--eq-muted-foreground);
        }
        
        .success-label {
          font-weight: 600;
          color: var(--state-success);
        }
        
        .font-semibold {
          font-weight: 600;
        }
        
        .font-mono {
          font-family: var(--eq-font-mono);
        }
      }
    }
  }
}

/* KPI卡片 */
.kpi-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  flex-shrink: 0;
  
  .kpi-card {
    padding: 12px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    border-radius: 8px;
    
    .kpi-label {
      margin-top: 4px;
    }
    
    .kpi-value {
      margin-top: 2px;
      font-family: var(--eq-font-display);
      font-size: 24px;
      font-weight: 700;
      letter-spacing: 0.04em;
      color: var(--eq-foreground);
      
      .kpi-unit {
        font-size: 12px;
        font-weight: 400;
        color: var(--eq-muted-foreground);
        margin-left: 2px;
      }
    }
  }
}

/* 工单处理面板 */
.workorder-panel {
  flex: 1;
  min-height: 0;
  
  .panel-header {
    .pending-badge {
      margin-left: auto;
      padding: 2px 6px;
      border-radius: 4px;
      background: rgba(239, 68, 68, 0.15);
      color: var(--state-error);
      font-size: 11px;
    }
  }
  
  .workorder-list {
    flex: 1 1 0;
    min-height: 0;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 6px;
    
    &::-webkit-scrollbar { display: none; }
    -ms-overflow-style: none;
    scrollbar-width: none;
    
    .workorder-item {
      padding: 8px;
      display: flex;
      flex-direction: column;
      gap: 3px;
      border-radius: 2px;
      
      .workorder-top {
        display: flex;
        align-items: center;
        justify-content: space-between;
        
        .workorder-title {
          font-size: 12px;
          font-weight: 500;
          color: var(--eq-card-foreground);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .workorder-status {
          flex-shrink: 0;
          padding: 2px 6px;
          border-radius: 4px;
          font-size: 11px;
          
          &.status-warning {
            background: rgba(245, 158, 11, 0.15);
            color: var(--state-warning);
          }
          
          &.status-error {
            background: rgba(239, 68, 68, 0.15);
            color: var(--state-error);
          }
          
          &.status-success {
            background: rgba(34, 197, 94, 0.15);
            color: var(--state-success);
          }
        }
      }
      
      .workorder-meta {
        font-size: 11px;
      }
      
      .workorder-desc {
        font-size: 11px;
        color: var(--eq-muted-foreground);
      }
    }
  }
  
  .workorder-stats {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
    padding-top: 6px;
    margin-top: 6px;
    border-top: 1px solid rgba(245, 158, 11, 0.1);
    flex: 1.2 1 0;
    min-height: 0;
    
    .stats-item {
      display: flex;
      flex-direction: column;
      min-height: 0;
      
      .stats-title {
        display: flex;
        align-items: center;
        gap: 5px;
        flex-shrink: 0;
        
        .stats-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          box-shadow: 0 0 6px currentColor;
        }
        
        .stats-label {
          font-size: 10px;
          font-weight: 600;
          color: var(--eq-card-foreground);
        }
      }
      
      .pie-chart-wrap {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        position: relative;
        gap: 6px;
        width: 100%;
        flex: 1;
        min-height: 0;
        
        .pie-svg {
          width: 90%;
          height: auto;
          flex-shrink: 0;
          min-width: 0;
          max-height: calc(100% - 20px);
          
          .pie-slice {
            transform-origin: 50px 50px;
            animation: pie-slice-grow 1s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
            opacity: 0;
            transform: scale(0);
            filter: drop-shadow(0 0 4px rgba(0,0,0,0.3));
            cursor: pointer;
            transition: transform 0.2s ease, filter 0.2s ease;
            
            &:hover {
              filter: drop-shadow(0 0 8px currentColor);
            }
            
            &.pie-slice-1 {
              animation-delay: 0.15s;
            }
            &.pie-slice-2 {
              animation-delay: 0.3s;
            }
            &.pie-slice-3 {
              animation-delay: 0.45s;
            }
            &.pie-slice-4 {
              animation-delay: 0.6s;
            }
          }
          
          .pie-center-value {
            font-size: 16px;
            font-weight: 700;
            fill: var(--eq-primary);
            font-family: 'JetBrains Mono', monospace;
            
            &.success {
              fill: var(--state-success);
            }
          }
          
          .pie-center-label {
            font-size: 9px;
            fill: var(--eq-muted-foreground);
          }
        }
        
        .pie-tooltip {
          position: absolute;
          top: -24px;
          left: 50%;
          transform: translateX(-50%);
          background: rgba(15, 23, 42, 0.95);
          border: 1px solid rgba(245, 158, 11, 0.3);
          border-radius: 4px;
          padding: 4px 8px;
          display: flex;
          align-items: center;
          gap: 4px;
          white-space: nowrap;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
          animation: tooltip-fade-in 0.2s ease-out;
          z-index: 10;
          
          .tooltip-dot {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            flex-shrink: 0;
            box-shadow: 0 0 4px currentColor;
          }
          
          .tooltip-label {
            font-size: 10px;
            color: var(--eq-foreground);
          }
          
          .tooltip-value {
            font-size: 10px;
            color: var(--eq-primary);
            font-weight: 600;
          }
        }
      }
    }
  }
}

@keyframes pie-slice-grow {
  from {
    opacity: 0;
    transform: scale(0);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes tooltip-fade-in {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

/* 渐入动画 */
.eq-fade-in-1 {
  animation: fade-in 0.5s ease-out 0.1s both;
}

.eq-fade-in-2 {
  animation: fade-in 0.5s ease-out 0.25s both;
}

.eq-fade-in-3 {
  animation: fade-in 0.5s ease-out 0.4s both;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes title-shimmer {
  0%, 100% {
    background-position: 0% center;
  }
  50% {
    background-position: 200% center;
  }
}

@keyframes title-glow-pulse {
  0%, 100% {
    filter: drop-shadow(0 0 12px rgba(245, 158, 11, 0.5)) drop-shadow(0 0 25px rgba(245, 158, 11, 0.25));
  }
  50% {
    filter: drop-shadow(0 0 20px rgba(245, 158, 11, 0.7)) drop-shadow(0 0 40px rgba(245, 158, 11, 0.4));
  }
}

@keyframes title-underline {
  0%, 100% {
    opacity: 0.4;
    width: 60%;
    left: 20%;
  }
  50% {
    opacity: 1;
    width: 90%;
    left: 5%;
  }
}
</style>
