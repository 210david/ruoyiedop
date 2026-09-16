package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flowable.domain.FlowLeave;
import com.ruoyi.flowable.service.IFlowLeaveService;

/**
 * 请假单 Controller（工作流示例业务）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/leave")
public class FlowLeaveController extends BaseController
{
    @Autowired
    private IFlowLeaveService flowLeaveService;

    /**
     * 查询我的请假单列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:leave:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowLeave leave)
    {
        startPage();
        List<FlowLeave> list = flowLeaveService.selectFlowLeaveList(leave);
        return getDataTable(list);
    }

    /**
     * 获取请假单详情
     */
    @PreAuthorize("@ss.hasPermi('flowable:leave:query')")
    @GetMapping("/{leaveId}")
    public AjaxResult getInfo(@PathVariable("leaveId") Long leaveId)
    {
        return success(flowLeaveService.selectFlowLeaveByLeaveId(leaveId));
    }

    /**
     * 新增请假单（草稿）
     */
    @Log(title = "请假单", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('flowable:leave:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FlowLeave leave)
    {
        return toAjax(flowLeaveService.insertFlowLeave(leave));
    }

    /**
     * 修改请假单（草稿/驳回）
     */
    @Log(title = "请假单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:leave:add')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FlowLeave leave)
    {
        return toAjax(flowLeaveService.updateFlowLeave(leave));
    }

    /**
     * 删除请假单（草稿/驳回）
     */
    @Log(title = "请假单", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('flowable:leave:remove')")
    @DeleteMapping("/{leaveIds}")
    public AjaxResult remove(@PathVariable Long[] leaveIds)
    {
        return toAjax(flowLeaveService.deleteFlowLeaveByLeaveIds(leaveIds));
    }

    /**
     * 提交审批（发起流程）
     */
    @Log(title = "请假单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:leave:submit')")
    @PostMapping("/submit/{leaveId}")
    public AjaxResult submit(@PathVariable("leaveId") Long leaveId,
            @RequestParam("approverUserId") Long approverUserId)
    {
        return success(flowLeaveService.submitFlowLeave(leaveId, approverUserId));
    }
}
