package com.ruoyi.wms.controller;

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
import com.ruoyi.wms.domain.WmsWave;
import com.ruoyi.wms.domain.WmsOutboundOrder;
import com.ruoyi.wms.service.IWmsWaveService;

@RestController
@RequestMapping("/wms/wave")
public class WmsWaveController extends BaseController
{
    @Autowired
    private IWmsWaveService wmsWaveService;

    @PreAuthorize("@ss.hasPermi('wms:wave:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsWave wave)
    {
        startPage();
        List<WmsWave> list = wmsWaveService.selectWaveList(wave);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('wms:wave:query')")
    @GetMapping(value = "/{waveId}")
    public AjaxResult getInfo(@PathVariable("waveId") Long waveId)
    {
        return AjaxResult.success(wmsWaveService.selectWaveById(waveId));
    }

    @Log(title = "波次管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('wms:wave:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsWave wave)
    {
        return toAjax(wmsWaveService.insertWave(wave));
    }

    @Log(title = "波次管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:wave:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsWave wave)
    {
        return toAjax(wmsWaveService.updateWave(wave));
    }

    @Log(title = "波次管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:wave:remove')")
    @DeleteMapping("/{waveIds}")
    public AjaxResult remove(@PathVariable Long[] waveIds)
    {
        return toAjax(wmsWaveService.deleteWaveByIds(waveIds));
    }

    @Log(title = "波次生成", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:wave:edit')")
    @PutMapping("/generate/{waveId}")
    public AjaxResult generate(@PathVariable Long waveId, @RequestBody Long[] orderIds)
    {
        return toAjax(wmsWaveService.generateWave(waveId, orderIds));
    }

    @Log(title = "波次释放", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:wave:edit')")
    @PutMapping("/release/{waveId}")
    public AjaxResult release(@PathVariable Long waveId)
    {
        return toAjax(wmsWaveService.releaseWave(waveId));
    }

    @PreAuthorize("@ss.hasPermi('wms:wave:query')")
    @GetMapping("/orders/{waveId}")
    public AjaxResult orders(@PathVariable Long waveId)
    {
        List<WmsOutboundOrder> list = wmsWaveService.selectWaveOrders(waveId);
        return AjaxResult.success(list);
    }
}
