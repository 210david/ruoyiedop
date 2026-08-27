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
import com.ruoyi.hr.domain.HrPayrollBatch;
import com.ruoyi.hr.service.IHrPayrollBatchService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/payroll")
public class HrPayrollBatchController extends BaseController
{
    @Autowired
    private IHrPayrollBatchService hrPayrollBatchService;

    @PreAuthorize("@ss.hasPermi('hr:payroll:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrPayrollBatch hrPayrollBatch)
    {
        startPage();
        List<HrPayrollBatch> list = hrPayrollBatchService.selectHrPayrollBatchList(hrPayrollBatch);
        return getDataTable(list);
    }

    @Log(title = "薪酬发放", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:payroll:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrPayrollBatch hrPayrollBatch)
    {
        List<HrPayrollBatch> list = hrPayrollBatchService.selectHrPayrollBatchList(hrPayrollBatch);
        ExcelUtil<HrPayrollBatch> util = new ExcelUtil<>(HrPayrollBatch.class);
        util.exportExcel(response, list, "薪酬发放");
    }

    @PreAuthorize("@ss.hasPermi('hr:payroll:query')")
    @GetMapping(value = "/{batchId}")
    public AjaxResult getInfo(@PathVariable("batchId") Long batchId)
    {
        return AjaxResult.success(hrPayrollBatchService.selectHrPayrollBatchById(batchId));
    }

    @PreAuthorize("@ss.hasPermi('hr:payroll:add')")
    @Log(title = "薪酬发放", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrPayrollBatch hrPayrollBatch)
    {
        return toAjax(hrPayrollBatchService.insertHrPayrollBatch(hrPayrollBatch));
    }

    @PreAuthorize("@ss.hasPermi('hr:payroll:edit')")
    @Log(title = "薪酬发放", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrPayrollBatch hrPayrollBatch)
    {
        return toAjax(hrPayrollBatchService.updateHrPayrollBatch(hrPayrollBatch));
    }

    @PreAuthorize("@ss.hasPermi('hr:payroll:remove')")
    @Log(title = "薪酬发放", businessType = BusinessType.DELETE)
    @DeleteMapping("/{batchIds}")
    public AjaxResult remove(@PathVariable Long[] batchIds)
    {
        return toAjax(hrPayrollBatchService.deleteHrPayrollBatchByIds(batchIds));
    }
}
