$content = @'
<template>
  <div class="eq-screen" v-loading="loading" element-loading-background="rgba(5,11,31,0.95)">
    <div class="bg-layer bg-grid"></div>
    <div class="bg-layer bg-nebula"></div>
    <div class="bg-layer bg-vignette"></div>
    <div class="bg-layer bg-scanlines"></div>
    <div class="bg-scan"></div>
    <div class="bg-particles">
      <span v-for="i in 32" :key="i" class="bg-particles__dot" :style="particleStyle(i)"></span>
    </div>

    <header class="hud-header">
      <div class="hud-header__left">
        <span class="hud-header__logo">⚙</span>
        <span class="hud-header__title">设备全生命周期管理平台</span>
      </div>
      <div class="hud-header__center">
        <span class="hud-clock">{{ clockDate }} {{ clockTime }}</span>
      </div>
      <div class="hud-header__right">
        <span class="hud-status hud-status--online"><span class="hud-status__dot"></span>运行中</span>
        <span class="hud-status hud-status--warning"><span class="hud-status__dot"></span>{{ animatedValues.woTotal - animatedValues.woDone }}台待维保</span>
        <span class="hud-status hud-status--online"><span class="hud-status__dot"></span>网络正常</span>
      </div>
    </header>

    <main class="screen-body" v-if="!loading && info.equipmentId">
      <section class="screen-col screen-col--left">
        <div class="hud-card">
          <div class="eq-selector" v-click-outside="closeEqSelect">
            <div class="eq-selector__trigger" @click="eqSelectOpen = !eqSelectOpen">
              <span class="eq-selector__name">{{ info.equipmentName }}</span>
              <el-icon class="eq-selector__arrow"><Right /></el-icon>
            </div>
            <transition name="eq-dropdown">
              <div class="eq-selector__dropdown" v-if="eqSelectOpen">
                <div class="eq-selector__search">
                  <el-input v-model="eqFilterText" placeholder="搜索设备名称/编号" clearable size="small" :prefix-icon="Search" />
                </div>
                <div class="eq-selector__list">
                  <div class="eq-selector__item" :class="{ isActive: item.equipmentId === currentEquipmentId }" v-for="item in filteredEquipment" :key="item.equipmentId" @click="switch