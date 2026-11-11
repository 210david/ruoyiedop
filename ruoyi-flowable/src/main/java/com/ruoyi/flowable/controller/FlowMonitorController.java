package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flowable.domain.FlowInstance;
import com.ruoyi.flowable.service.IFlowProcessService;

/**
 * 流程监控 Controller（系统管理员对全流程的查看与干预，对标泛微「流程监控」）
 *
 * 与流程中心（我发起的/待办/已办）的区别：这里能看到全公司所有流程实例，
 * 并支持强制干预 —— 跳转到任意节点、改派办理人、强制结束、撤销、删除。
 * 所有接口都要求 {@code flowable:monitor:*} 权限，默认只授给系统管理员角色。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/monitor")
public class FlowMonitorController extends BaseController
{
    @Autowired
    private IFlowProcessService flowProcessService;

    /**
     * 流程实例列表（全部流程，支持标题/流程名称/创建人/当前节点/状态/创建日期筛选）
     */
    @PreAuthorize("@ss.hasPermi('flowable:monitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowInstance query)
    {
        List<FlowInstance> list = flowProcessService.selectMonitorProcessList(query);
        com.ruoyi.common.core.page.PageDomain page = com.ruoyi.common.core.page.TableSupport.buildPageRequest();
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(200);
        rsp.setMsg("查询成功");
        rsp.setTotal(list.size());
        int pageNum = page.getPageNum() == null ? 1 : page.getPageNum();
        int pageSize = page.getPageSize() == null ? 10 : page.getPageSize();
        int from = Math.min((pageNum - 1) * pageSize, list.size());
        int to = Math.min(from + pageSize, list.size());
        rsp.setRows(list.subList(from, to));
        return rsp;
    }

    /**
     * 状态页签数量（全部 / 进行中 / 已结束 / 已撤销）
     */
    @PreAuthorize("@ss.hasPermi('flowable:monitor:list')")
    @GetMapping("/counts")
    public AjaxResult counts()
    {
        return success(flowProcessService.countMonitor());
    }

    /**
     * 干预面板数据：当前停留节点、当前待办任务、全部可跳转节点
     */
    @PreAuthorize("@ss.hasPermi('flowable:monitor:query')")
    @GetMapping("/nodes/{instanceId}")
    public AjaxResult nodes(@PathVariable("instanceId") String instanceId)
    {
        return success(flowProcessService.selectMonitorNodes(instanceId));
    }

    /**
     * 流程干预：强制跳转到指定节点（可选同时指定办理人）
     */
    @Log(title = "流程监控", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:monitor:intervene')")
    @PutMapping("/intervene/{instanceId}")
    public AjaxResult intervene(@PathVariable("instanceId") String instanceId,
            @RequestParam("targetActivityId") String targetActivityId,
            @RequestParam(value = "assigneeId", required = false) Long assigneeId,
            @RequestParam(value = "comment", required = false) String comment)
    {
        return toAjax(flowProcessService.interveneInstance(instanceId, targetActivityId, assigneeId, comment));
    }

    /**
     * 强制收回：把流程从当前节点收回（回退）到上一节点，由上一节点原办理人重新办理
     */
    @Log(title = "流程监控", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:monitor:intervene')")
    @PutMapping("/recall/{instanceId}")
    public AjaxResult recall(@PathVariable("instanceId") String instanceId,
            @RequestParam(value = "reason", required = false) String reason)
    {
        java.util.Map<String, Object> data = flowProcessService.recallInstance(instanceId, reason);
        return AjaxResult.success("已强制收回到「" + data.get("prevActivityName") + "」", data);
    }

    /**
     * 改派当前节点办理人
     */
    @Log(title = "流程监控", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:monitor:intervene')")
    @PutMapping("/assign/{taskId}")
    public AjaxResult assign(@PathVariable("taskId") String taskId,
            @RequestParam("targetUserId") Long targetUserId,
            @RequestParam(value = "comment", required = false) String comment)
    {
        return toAjax(flowProcessService.adminAssignTask(taskId, targetUserId, comment));
    }

    /**
     * 强制结束流程（action=end） / 撤销流程（action=cancel）
     */
    @Log(title = "流程监控", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:monitor:end')")
    @PutMapping("/end/{instanceId}")
    public AjaxResult end(@PathVariable("instanceId") String instanceId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "reason", required = false) String reason)
    {
        flowProcessService.adminEndProcess(instanceId, action, reason);
        return AjaxResult.success("cancel".equals(action) ? "流程已撤销" : "流程已强制结束");
    }

    /**
     * 删除流程实例（运行中会先终止，再连同历史记录一起删除）
     */
    @Log(title = "流程监控", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('flowable:monitor:remove')")
    @DeleteMapping("/{instanceId}")
    public AjaxResult remove(@PathVariable("instanceId") String instanceId)
    {
        return toAjax(flowProcessService.deleteProcessInstance(instanceId));
    }
}
