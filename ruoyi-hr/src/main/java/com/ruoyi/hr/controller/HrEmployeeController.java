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
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.service.IHrEmployeeService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/employee")
public class HrEmployeeController extends BaseController
{
    @Autowired
    private IHrEmployeeService hrEmployeeService;

    @PreAuthorize("@ss.hasPermi('hr:employee:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrEmployee hrEmployee)
    {
        startPage();
        List<HrEmployee> list = hrEmployeeService.selectHrEmployeeList(hrEmployee);
        return getDataTable(list);
    }

    @Log(title = "员工档案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:employee:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrEmployee hrEmployee)
    {
        List<HrEmployee> list = hrEmployeeService.selectHrEmployeeList(hrEmployee);
        ExcelUtil<HrEmployee> util = new ExcelUtil<>(HrEmployee.class);
        util.exportExcel(response, list, "员工档案");
    }

    @PreAuthorize("@ss.hasPermi('hr:employee:query')")
    @GetMapping(value = "/{employeeId}")
    public AjaxResult getInfo(@PathVariable("employeeId") Long employeeId)
    {
        return AjaxResult.success(hrEmployeeService.selectHrEmployeeById(employeeId));
    }

    @PreAuthorize("@ss.hasPermi('hr:employee:add')")
    @Log(title = "员工档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrEmployee hrEmployee)
    {
        return toAjax(hrEmployeeService.insertHrEmployee(hrEmployee));
    }

    @PreAuthorize("@ss.hasPermi('hr:employee:edit')")
    @Log(title = "员工档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrEmployee hrEmployee)
    {
        return toAjax(hrEmployeeService.updateHrEmployee(hrEmployee));
    }

    @PreAuthorize("@ss.hasPermi('hr:employee:remove')")
    @Log(title = "员工档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{employeeIds}")
    public AjaxResult remove(@PathVariable Long[] employeeIds)
    {
        return toAjax(hrEmployeeService.deleteHrEmployeeByIds(employeeIds));
    }
}
