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
import com.ruoyi.hr.domain.HrEmployeeFamily;
import com.ruoyi.hr.service.IHrEmployeeFamilyService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/employeefamily")
public class HrEmployeeFamilyController extends BaseController {
    @Autowired
    private IHrEmployeeFamilyService hrEmployeeFamilyService;

    @PreAuthorize("@ss.hasPermi('hr:employeeFamily:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrEmployeeFamily hrEmployeeFamily) {
        startPage();
        List<HrEmployeeFamily> list = hrEmployeeFamilyService.selectHrEmployeeFamilyList(hrEmployeeFamily);
        return getDataTable(list);
    }

    @Log(title = "员工家庭成员", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:employeeFamily:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrEmployeeFamily hrEmployeeFamily) {
        List<HrEmployeeFamily> list = hrEmployeeFamilyService.selectHrEmployeeFamilyList(hrEmployeeFamily);
        ExcelUtil<HrEmployeeFamily> util = new ExcelUtil<>(HrEmployeeFamily.class);
        util.exportExcel(response, list, "员工家庭成员");
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeFamily:query')")
    @GetMapping(value = "/{familyId}")
    public AjaxResult getInfo(@PathVariable("familyId") Long familyId) {
        return AjaxResult.success(hrEmployeeFamilyService.selectHrEmployeeFamilyByFamilyId(familyId));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeFamily:add')")
    @Log(title = "员工家庭成员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrEmployeeFamily hrEmployeeFamily) {
        return toAjax(hrEmployeeFamilyService.insertHrEmployeeFamily(hrEmployeeFamily));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeFamily:edit')")
    @Log(title = "员工家庭成员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrEmployeeFamily hrEmployeeFamily) {
        return toAjax(hrEmployeeFamilyService.updateHrEmployeeFamily(hrEmployeeFamily));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeFamily:remove')")
    @Log(title = "员工家庭成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{familyIds}")
    public AjaxResult remove(@PathVariable Long[] familyIds) {
        return toAjax(hrEmployeeFamilyService.deleteHrEmployeeFamilyByIds(familyIds));
    }
}
