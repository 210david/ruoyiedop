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
import com.ruoyi.hr.domain.HrLeave;
import com.ruoyi.hr.service.IHrLeaveService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/hr/leave")
public class HrLeaveController extends BaseController
{
    @Autowired
    private IHrLeaveService hrLeaveService;

    @PreAuthorize("@ss.hasPermi('hr:leave:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrLeave hrLeave)
    {
        startPage();
        List<HrLeave> list = hrLeaveService.selectHrLeaveList(hrLeave);
        return getDataTable(list);
    }

    @Log(title = "离职", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:leave:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrLeave hrLeave)
    {
        List<HrLeave> list = hrLeaveService.selectHrLeaveList(hrLeave);
        ExcelUtil<HrLeave> util = new ExcelUtil<>(HrLeave.class);
        util.exportExcel(response, list, "离职");
    }

    @PreAuthorize("@ss.hasPermi('hr:leave:query')")
    @GetMapping(value = "/{leaveId}")
    public AjaxResult getInfo(@PathVariable("leaveId") Long leaveId)
    {
        return AjaxResult.success(hrLeaveService.selectHrLeaveById(leaveId));
    }

    @PreAuthorize("@ss.hasPermi('hr:leave:add')")
    @Log(title = "离职", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrLeave hrLeave)
    {
        return toAjax(hrLeaveService.insertHrLeave(hrLeave));
    }

    @PreAuthorize("@ss.hasPermi('hr:leave:edit')")
    @Log(title = "离职", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrLeave hrLeave)
    {
        return toAjax(hrLeaveService.updateHrLeave(hrLeave));
    }

    @PreAuthorize("@ss.hasPermi('hr:leave:remove')")
    @Log(title = "离职", businessType = BusinessType.DELETE)
    @DeleteMapping("/{leaveIds}")
    public AjaxResult remove(@PathVariable Long[] leaveIds)
    {
        return toAjax(hrLeaveService.deleteHrLeaveByIds(leaveIds));
    }

    @PreAuthorize("@ss.hasPermi('hr:leave:audit')")
    @Log(title = "离职审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{leaveId}")
    public AjaxResult audit(@PathVariable Long leaveId, @RequestParam String auditAction, @RequestParam(required = false) String auditRemark)
    {
        return toAjax(hrLeaveService.auditHrLeave(leaveId, auditAction, auditRemark));
    }
}
