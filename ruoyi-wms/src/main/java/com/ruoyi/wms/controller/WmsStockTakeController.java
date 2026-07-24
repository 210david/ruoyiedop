package com.ruoyi.wms.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.WmsStockTake;
import com.ruoyi.wms.domain.WmsStockTakeDetail;
import com.ruoyi.wms.service.IWmsStockTakeService;
import jakarta.servlet.http.HttpServletResponse;

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

    @Log(title = "批量录入盘点明细", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping("/batch-submit/{takeId}")
    public AjaxResult batchSubmitDetail(@PathVariable Long takeId, @RequestBody List<WmsStockTakeDetail> detailList)
    {
        int count = wmsStockTakeService.batchSubmitDetail(takeId, detailList);
        return AjaxResult.success("批量录入成功，共" + count + "条");
    }

    @Log(title = "导入盘点明细", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PostMapping("/import/{takeId}")
    public AjaxResult importDetail(@PathVariable Long takeId, @RequestParam("file") MultipartFile file) throws Exception
    {
        ExcelUtil<WmsStockTakeDetail> util = new ExcelUtil<>(WmsStockTakeDetail.class);
        List<WmsStockTakeDetail> detailList = util.importExcel(file.getInputStream());
        String message = wmsStockTakeService.importDetail(takeId, detailList);
        return AjaxResult.success(message);
    }

    @Log(title = "导出盘点明细模板", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PostMapping("/export-template/{takeId}")
    public void exportTemplate(HttpServletResponse response, @PathVariable Long takeId)
    {
        WmsStockTake take = wmsStockTakeService.selectStockTakeById(takeId);
        List<WmsStockTakeDetail> list = take.getDetailList();
        ExcelUtil<WmsStockTakeDetail> util = new ExcelUtil<>(WmsStockTakeDetail.class);
        util.exportExcel(response, list, "盘点明细模板");
    }

    @Log(title = "提交盘点审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping("/submit-approve/{takeId}")
    public AjaxResult submitForApproval(@PathVariable Long takeId)
    {
        return toAjax(wmsStockTakeService.submitForApproval(takeId));
    }

    @Log(title = "盘点审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping("/approve/{takeId}")
    public AjaxResult approve(@PathVariable Long takeId, String approveOpinion)
    {
        return toAjax(wmsStockTakeService.approveTake(takeId, approveOpinion));
    }

    @Log(title = "盘点驳回", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping("/reject/{takeId}")
    public AjaxResult reject(@PathVariable Long takeId, String approveOpinion)
    {
        return toAjax(wmsStockTakeService.rejectTake(takeId, approveOpinion));
    }

    @Log(title = "盘点作废", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:stocktake:edit')")
    @PutMapping("/void/{takeId}")
    public AjaxResult voidTake(@PathVariable Long takeId)
    {
        return toAjax(wmsStockTakeService.voidTake(takeId));
    }
}
