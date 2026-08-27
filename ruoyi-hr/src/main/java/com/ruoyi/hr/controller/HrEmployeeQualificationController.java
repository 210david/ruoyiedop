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
import com.ruoyi.hr.domain.HrEmployeeQualification;
import com.ruoyi.hr.service.IHrEmployeeQualificationService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/employeequalification")
public class HrEmployeeQualificationController extends BaseController {
    @Autowired
    private IHrEmployeeQualificationService hrEmployeeQualificationService;

    @PreAuthorize("@ss.hasPermi('hr:employeeQualification:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrEmployeeQualification hrEmployeeQualification) {
        startPage();
        List<HrEmployeeQualification> list = hrEmployeeQualificationService.selectHrEmployeeQualificationList(hrEmployeeQualification);
        return getDataTable(list);
    }

    @Log(title = "员工培训/职称/资格认证", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:employeeQualification:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrEmployeeQualification hrEmployeeQualification) {
        List<HrEmployeeQualification> list = hrEmployeeQualificationService.selectHrEmployeeQualificationList(hrEmployeeQualification);
        ExcelUtil<HrEmployeeQualification> util = new ExcelUtil<>(HrEmployeeQualification.class);
        util.exportExcel(response, list, "员工培训/职称/资格认证");
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeQualification:query')")
    @GetMapping(value = "/{qualificationId}")
    public AjaxResult getInfo(@PathVariable("qualificationId") Long qualificationId) {
        return AjaxResult.success(hrEmployeeQualificationService.selectHrEmployeeQualificationByQualificationId(qualificationId));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeQualification:add')")
    @Log(title = "员工培训/职称/资格认证", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrEmployeeQualification hrEmployeeQualification) {
        return toAjax(hrEmployeeQualificationService.insertHrEmployeeQualification(hrEmployeeQualification));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeQualification:edit')")
    @Log(title = "员工培训/职称/资格认证", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrEmployeeQualification hrEmployeeQualification) {
        return toAjax(hrEmployeeQualificationService.updateHrEmployeeQualification(hrEmployeeQualification));
    }

    @PreAuthorize("@ss.hasPermi('hr:employeeQualification:remove')")
    @Log(title = "员工培训/职称/资格认证", businessType = BusinessType.DELETE)
    @DeleteMapping("/{qualificationIds}")
    public AjaxResult remove(@PathVariable Long[] qualificationIds) {
        return toAjax(hrEmployeeQualificationService.deleteHrEmployeeQualificationByIds(qualificationIds));
    }
}
