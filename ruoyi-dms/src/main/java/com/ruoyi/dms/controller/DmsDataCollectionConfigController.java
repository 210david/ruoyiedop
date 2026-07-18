package com.ruoyi.dms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.dms.domain.DmsDataCollectionConfig;
import com.ruoyi.dms.service.IDmsDataCollectionConfigService;

@RestController
@RequestMapping("/dms/data/config")
public class DmsDataCollectionConfigController extends BaseController
{
    @Autowired
    private IDmsDataCollectionConfigService service;

    @PreAuthorize("@ss.hasPermi('dms:data:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(DmsDataCollectionConfig config) { startPage(); return getDataTable(service.selectConfigList(config)); }

    @PreAuthorize("@ss.hasPermi('dms:data:config:query')")
    @GetMapping("/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId) { return AjaxResult.success(service.selectConfigById(configId)); }

    @Log(title = "采集配置", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:data:config:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DmsDataCollectionConfig config) { return toAjax(service.insertConfig(config)); }

    @Log(title = "采集配置", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:data:config:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DmsDataCollectionConfig config) { return toAjax(service.updateConfig(config)); }

    @Log(title = "采集配置", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:data:config:remove')")
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds) { return toAjax(service.deleteConfigByIds(configIds)); }
}
