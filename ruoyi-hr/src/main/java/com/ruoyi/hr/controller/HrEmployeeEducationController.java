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
import com.ruoyi.hr.domain.HrEmployeeEducation;
import com.ruoyi.hr.service.IHrEmployeeEducationService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/employeeeducation")
public class HrEmployeeEducationController extends BaseController {
    @Autowired
    private IHrEmployeeEducationService hrEmployeeEducationService;

    @PreAuthorize("@ss.hasPermi('hr:employeeEducation:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrEmployeeEducation hrEmployeeEducation) {
        startPage();
        List<HrEmployeeEducation> list = hrEmployeeEducationService.selectHrEmployeeEducationList(hrEmployeeEducation);
        return getDataTable(list);
    }

    @Log(title = "员工教育背景", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:employeeEducation:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrEmployeeEducation hrEmployeeEducation) {
        List<HrEmployeeEducation> list = hrEmployeeEducationService.selectHrEmployeeEducationList(hrEmployeeEducation);
        ExcelUtil<HrEmployeeEducation> util = new ExcelUtil<>(HrEmployeeEducation.class);
        util.exportExcel(response, list, "员工教育背景");
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeEducation:query')")
    @GetMapping(value = "/{educationId}")
    public AjaxResult getInfo(@PathVariable("educationId") Long educationId) {
        return AjaxResult.success(hrEmployeeEducationService.selectHrEmployeeEducationByEducationId(educationId));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeEducation:add')")
    @Log(title = "员工教育背景", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrEmployeeEducation hrEmployeeEducation) {
        return toAjax(hrEmployeeEducationService.insertHrEmployeeEducation(hrEmployeeEducation));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeEducation:edit')")
    @Log(title = "员工教育背景", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrEmployeeEducation hrEmployeeEducation) {
        return toAjax(hrEmployeeEducationService.updateHrEmployeeEducation(hrEmployeeEducation));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeEducation:remove')")
    @Log(title = "员工教育背景", businessType = BusinessType.DELETE)
    @DeleteMapping("/{educationIds}")
    public AjaxResult remove(@PathVariable Long[] educationIds) {
        return toAjax(hrEmployeeEducationService.deleteHrEmployeeEducationByIds(educationIds));
    }
}
