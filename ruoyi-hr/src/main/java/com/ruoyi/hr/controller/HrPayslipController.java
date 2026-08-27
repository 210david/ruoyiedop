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
import com.ruoyi.hr.domain.HrPayslip;
import com.ruoyi.hr.service.IHrPayslipService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/payslip")
public class HrPayslipController extends BaseController
{
    @Autowired
    private IHrPayslipService hrPayslipService;

    @PreAuthorize("@ss.hasPermi('hr:payslip:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrPayslip hrPayslip)
    {
        startPage();
        List<HrPayslip> list = hrPayslipService.selectHrPayslipList(hrPayslip);
        return getDataTable(list);
    }

    @Log(title = "工资条", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:payslip:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrPayslip hrPayslip)
    {
        List<HrPayslip> list = hrPayslipService.selectHrPayslipList(hrPayslip);
        ExcelUtil<HrPayslip> util = new ExcelUtil<>(HrPayslip.class);
        util.exportExcel(response, list, "工资条");
    }

    @PreAuthorize("@ss.hasPermi('hr:payslip:query')")
    @GetMapping(value = "/{payslipId}")
    public AjaxResult getInfo(@PathVariable("payslipId") Long payslipId)
    {
        return AjaxResult.success(hrPayslipService.selectHrPayslipById(payslipId));
    }

    @PreAuthorize("@ss.hasPermi('hr:payslip:add')")
    @Log(title = "工资条", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrPayslip hrPayslip)
    {
        return toAjax(hrPayslipService.insertHrPayslip(hrPayslip));
    }

    @PreAuthorize("@ss.hasPermi('hr:payslip:edit')")
    @Log(title = "工资条", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrPayslip hrPayslip)
    {
        return toAjax(hrPayslipService.updateHrPayslip(hrPayslip));
    }

    @PreAuthorize("@ss.hasPermi('hr:payslip:remove')")
    @Log(title = "工资条", businessType = BusinessType.DELETE)
    @DeleteMapping("/{payslipIds}")
    public AjaxResult remove(@PathVariable Long[] payslipIds)
    {
        return toAjax(hrPayslipService.deleteHrPayslipByIds(payslipIds));
    }
}
