package com.ruoyi.hr.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hr.domain.HrPerfManual;
import com.ruoyi.hr.domain.HrPerfMonth;
import com.ruoyi.hr.service.IHrSalesPerfService;

/**
 * 业绩归集/手工补录 Controller（需求三）
 */
@RestController
@RequestMapping("/hr/perf")
public class HrPerfController extends BaseController {
    @Autowired
    private IHrSalesPerfService perfService;

    @PreAuthorize("@ss.hasPermi('hr:perf:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrPerfMonth perf) { return getDataTable(perfService.selectPerfList(perf)); }

    @PreAuthorize("@ss.hasPermi('hr:perf:query')")
    @GetMapping("/rank")
    public AjaxResult rank(@RequestParam String period) { return success(perfService.selectPerfRank(period)); }

    /** 手动归集（幂等重算） */
    @Log(title = "业绩归集", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:perf:aggregate')")
    @PostMapping("/aggregate/{period}")
    public AjaxResult aggregate(@PathVariable String period) { return success(perfService.aggregate(period)); }

    // ---- 线下补录（需审批） ----
    @PreAuthorize("@ss.hasPermi('hr:perf:query')")
    @GetMapping("/manual/list")
    public TableDataInfo manualList(HrPerfManual manual) { return getDataTable(perfService.selectManualList(manual)); }

    @Log(title = "业绩补录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('hr:perf:manualAdd')")
    @PostMapping("/manual")
    public AjaxResult addManual(@RequestBody HrPerfManual manual) { return toAjax(perfService.insertManual(manual)); }

    @Log(title = "业绩补录审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:perf:approve')")
    @PutMapping("/manual/approve")
    public AjaxResult approveManual(@RequestBody HrPerfManual manual) {
        boolean approved = "1".equals(manual.getPerfStatus());
        return toAjax(perfService.approveManual(manual.getManualId(), approved, manual.getApproveOpinion()));
    }
}
