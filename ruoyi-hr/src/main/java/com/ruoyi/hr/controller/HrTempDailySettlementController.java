package com.ruoyi.hr.controller;

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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.hr.domain.HrTempDailySettlement;
import com.ruoyi.hr.service.IHrTempDailySettlementService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/tempSettlement")
public class HrTempDailySettlementController extends BaseController
{
    @Autowired
    private IHrTempDailySettlementService hrTempDailySettlementService;

    @PreAuthorize("@ss.hasPermi('hr:tempSettlement:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrTempDailySettlement hrTempDailySettlement)
    {
        startPage();
        List<HrTempDailySettlement> list = hrTempDailySettlementService.selectHrTempDailySettlementList(hrTempDailySettlement);
        return getDataTable(list);
    }

    @Log(title = "临时工日结", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:tempSettlement:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrTempDailySettlement hrTempDailySettlement)
    {
        List<HrTempDailySettlement> list = hrTempDailySettlementService.selectHrTempDailySettlementList(hrTempDailySettlement);
        ExcelUtil<HrTempDailySettlement> util = new ExcelUtil<>(HrTempDailySettlement.class);
        util.exportExcel(response, list, "临时工日结");
    }

    @PreAuthorize("@ss.hasPermi('hr:tempSettlement:query')")
    @GetMapping(value = "/{settlementId}")
    public AjaxResult getInfo(@PathVariable("settlementId") Long settlementId)
    {
        return AjaxResult.success(hrTempDailySettlementService.selectHrTempDailySettlementById(settlementId));
    }

    @PreAuthorize("@ss.hasPermi('hr:tempSettlement:add')")
    @Log(title = "临时工日结", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrTempDailySettlement hrTempDailySettlement)
    {
        return toAjax(hrTempDailySettlementService.insertHrTempDailySettlement(hrTempDailySettlement));
    }

    @PreAuthorize("@ss.hasPermi('hr:tempSettlement:edit')")
    @Log(title = "临时工日结", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrTempDailySettlement hrTempDailySettlement)
    {
        return toAjax(hrTempDailySettlementService.updateHrTempDailySettlement(hrTempDailySettlement));
    }

    @PreAuthorize("@ss.hasPermi('hr:tempSettlement:remove')")
    @Log(title = "临时工日结", businessType = BusinessType.DELETE)
    @DeleteMapping("/{settlementIds}")
    public AjaxResult remove(@PathVariable Long[] settlementIds)
    {
        return toAjax(hrTempDailySettlementService.deleteHrTempDailySettlementByIds(settlementIds));
    }
}
