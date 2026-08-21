package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsDemand;
import com.ruoyi.mms.service.IMmsDemandService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Demand Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/demand")
public class MmsDemandController extends BaseController
{
    @Autowired
    private IMmsDemandService mmsDemandService;

    @PreAuthorize("@ss.hasPermi('mms:demand:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsDemand demand)
    {
        startPage();
        List<MmsDemand> list = mmsDemandService.selectDemandList(demand);
        return getDataTable(list);
    }

    @Log(title = "Demand", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:demand:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsDemand demand)
    {
        List<MmsDemand> list = mmsDemandService.selectDemandList(demand);
        ExcelUtil<MmsDemand> util = new ExcelUtil<>(MmsDemand.class);
        util.exportExcel(response, list, "Demand");
    }

    @PreAuthorize("@ss.hasPermi('mms:demand:query')")
    @GetMapping(value = "/{DemandId}")
    public AjaxResult getInfo(@PathVariable("DemandId") Long DemandId)
    {
        return AjaxResult.success(mmsDemandService.selectDemandById(DemandId));
    }

    @Log(title = "Demand", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:demand:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsDemand demand)
    {
        return toAjax(mmsDemandService.insertDemand(demand));
    }

    @Log(title = "Demand", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:demand:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsDemand demand)
    {
        return toAjax(mmsDemandService.updateDemand(demand));
    }

    @Log(title = "Demand", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:demand:remove')")
    @DeleteMapping("/{DemandIds}")
    public AjaxResult remove(@PathVariable Long[] DemandIds)
    {
        return toAjax(mmsDemandService.deleteDemandByIds(DemandIds));
    }

    // ========== 业务流程操作 ==========

    /**
     * 需求确认
     * 状态：0(草稿) → 1(已确认)
     */
    @Log(title = "生产需求-确认", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:demand:confirm')")
    @PutMapping("/confirm/{demandId}")
    public AjaxResult confirm(@PathVariable("demandId") Long demandId)
    {
        return toAjax(mmsDemandService.confirmDemand(demandId));
    }

    /**
     * 需求取消确认
     * 状态：1(已确认) → 0(草稿)
     */
    @Log(title = "生产需求-取消确认", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:demand:confirm')")
    @PutMapping("/unconfirm/{demandId}")
    public AjaxResult unconfirm(@PathVariable("demandId") Long demandId)
    {
        return toAjax(mmsDemandService.unconfirmDemand(demandId));
    }

    /**
     * 需求转计划
     * 根据需求创建MPS草稿，并回写需求状态为已排产(2)
     */
    @Log(title = "生产需求-转计划", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:demand:toplan')")
    @PostMapping("/toMps/{demandId}")
    public AjaxResult toMps(@PathVariable("demandId") Long demandId)
    {
        return AjaxResult.success(mmsDemandService.convertToMps(demandId));
    }
}
