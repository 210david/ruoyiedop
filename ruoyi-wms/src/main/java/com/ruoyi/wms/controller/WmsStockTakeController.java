package com.ruoyi.wms.controller;

import java.math.BigDecimal;
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
import com.ruoyi.wms.domain.WmsStockTake;
import com.ruoyi.wms.service.IWmsStockTakeService;

@RestController
@RequestMapping("/wms/stocktake")
public class WmsStockTakeController extends BaseController
{
    @Autowired
    private IWmsStockTakeService wmsStockTakeService;

    @PreAuthorize("@ss.hasPermi('wms:stocktake:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsStockTake take)
    {
        startPage();
        List<WmsStockTake> list = wmsStockTakeService.selectStockTakeList(take);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('wms:stocktake:query')")
    @GetMapping(value = "/{takeId}")
    public AjaxResult getInfo(@PathVariable("takeId") Long takeId)
    {
        return AjaxResult.success(wmsStockTakeService.selectStockTakeById(takeId));
    }

    @Log(title = "盘点管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsStockTake take)
    {
        return toAjax(wmsStockTakeService.insertStockTake(take));
    }

    @Log(title = "盘点管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsStockTake take)
    {
        return toAjax(wmsStockTakeService.updateStockTake(take));
    }

    @Log(title = "盘点管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:remove')")
    @DeleteMapping("/{takeIds}")
    public AjaxResult remove(@PathVariable Long[] takeIds)
    {
        return toAjax(wmsStockTakeService.deleteStockTakeByIds(takeIds));
    }

    @Log(title = "开始盘点", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping("/start/{takeId}")
    public AjaxResult start(@PathVariable Long takeId)
    {
        return toAjax(wmsStockTakeService.startTake(takeId));
    }

    @Log(title = "提交盘点明细", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping("/submit/{takeId}/{detailId}")
    public AjaxResult submitDetail(@PathVariable Long takeId, @PathVariable Long detailId,
                                   @RequestParam BigDecimal actualQty, @RequestParam(required = false) String diffReason)
    {
        return toAjax(wmsStockTakeService.submitDetail(takeId, detailId, actualQty, diffReason));
    }

    @Log(title = "盘点审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping("/approve/{takeId}")
    public AjaxResult approve(@PathVariable Long takeId)
    {
        return toAjax(wmsStockTakeService.approveTake(takeId));
    }

    @Log(title = "盘点作废", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping("/void/{takeId}")
    public AjaxResult voidTake(@PathVariable Long takeId)
    {
        return toAjax(wmsStockTakeService.voidTake(takeId));
    }
}
