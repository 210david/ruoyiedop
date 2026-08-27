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
import com.ruoyi.hr.domain.HrLeaveRequest;
import com.ruoyi.hr.service.IHrLeaveRequestService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/leaveRequest")
public class HrLeaveRequestController extends BaseController
{
    @Autowired
    private IHrLeaveRequestService hrLeaveRequestService;

    @PreAuthorize("@ss.hasPermi('hr:leaveRequest:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrLeaveRequest hrLeaveRequest)
    {
        startPage();
        List<HrLeaveRequest> list = hrLeaveRequestService.selectHrLeaveRequestList(hrLeaveRequest);
        return getDataTable(list);
    }

    @Log(title = "请假", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:leaveRequest:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrLeaveRequest hrLeaveRequest)
    {
        List<HrLeaveRequest> list = hrLeaveRequestService.selectHrLeaveRequestList(hrLeaveRequest);
        ExcelUtil<HrLeaveRequest> util = new ExcelUtil<>(HrLeaveRequest.class);
        util.exportExcel(response, list, "请假");
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveRequest:query')")
    @GetMapping(value = "/{leaveReqId}")
    public AjaxResult getInfo(@PathVariable("leaveReqId") Long leaveReqId)
    {
        return AjaxResult.success(hrLeaveRequestService.selectHrLeaveRequestById(leaveReqId));
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveRequest:add')")
    @Log(title = "请假", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrLeaveRequest hrLeaveRequest)
    {
        return toAjax(hrLeaveRequestService.insertHrLeaveRequest(hrLeaveRequest));
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveRequest:edit')")
    @Log(title = "请假", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrLeaveRequest hrLeaveRequest)
    {
        return toAjax(hrLeaveRequestService.updateHrLeaveRequest(hrLeaveRequest));
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveRequest:remove')")
    @Log(title = "请假", businessType = BusinessType.DELETE)
    @DeleteMapping("/{leaveReqIds}")
    public AjaxResult remove(@PathVariable Long[] leaveReqIds)
    {
        return toAjax(hrLeaveRequestService.deleteHrLeaveRequestByIds(leaveReqIds));
    }
}
