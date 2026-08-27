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
import com.ruoyi.hr.domain.HrEmployeeWorkExp;
import com.ruoyi.hr.service.IHrEmployeeWorkExpService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/employeeworkexp")
public class HrEmployeeWorkExpController extends BaseController {
    @Autowired
    private IHrEmployeeWorkExpService hrEmployeeWorkExpService;

    @PreAuthorize("@ss.hasPermi('hr:employeeWorkExp:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrEmployeeWorkExp hrEmployeeWorkExp) {
        startPage();
        List<HrEmployeeWorkExp> list = hrEmployeeWorkExpService.selectHrEmployeeWorkExpList(hrEmployeeWorkExp);
        return getDataTable(list);
    }

    @Log(title = "员工工作经历", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:employeeWorkExp:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrEmployeeWorkExp hrEmployeeWorkExp) {
        List<HrEmployeeWorkExp> list = hrEmployeeWorkExpService.selectHrEmployeeWorkExpList(hrEmployeeWorkExp);
        ExcelUtil<HrEmployeeWorkExp> util = new ExcelUtil<>(HrEmployeeWorkExp.class);
        util.exportExcel(response, list, "员工工作经历");
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeWorkExp:query')")
    @GetMapping(value = "/{workExpId}")
    public AjaxResult getInfo(@PathVariable("workExpId") Long workExpId) {
        return AjaxResult.success(hrEmployeeWorkExpService.selectHrEmployeeWorkExpByWorkExpId(workExpId));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeWorkExp:add')")
    @Log(title = "员工工作经历", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrEmployeeWorkExp hrEmployeeWorkExp) {
        return toAjax(hrEmployeeWorkExpService.insertHrEmployeeWorkExp(hrEmployeeWorkExp));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeWorkExp:edit')")
    @Log(title = "员工工作经历", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrEmployeeWorkExp hrEmployeeWorkExp) {
        return toAjax(hrEmployeeWorkExpService.updateHrEmployeeWorkExp(hrEmployeeWorkExp));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeWorkExp:remove')")
    @Log(title = "员工工作经历", businessType = BusinessType.DELETE)
    @DeleteMapping("/{workExpIds}")
    public AjaxResult remove(@PathVariable Long[] workExpIds) {
        return toAjax(hrEmployeeWorkExpService.deleteHrEmployeeWorkExpByIds(workExpIds));
    }
}
