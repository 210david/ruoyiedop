package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import com.ruoyi.flowable.domain.FlowInstance;
import com.ruoyi.flowable.domain.FlowTask;
import com.ruoyi.flowable.domain.dto.FlowStartDTO;
import com.ruoyi.flowable.service.IFlowProcessService;

/**
 * 流程实例 Controller（我发起的/流程监控）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/process")
public class FlowProcessController extends BaseController
{
    @Autowired
    private IFlowProcessService flowProcessService;

    /**
     * 发起流程
     */
    @Log(title = "流程实例", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('flowable:process:start')")
    @PostMapping("/start")
    public AjaxResult start(@Validated @RequestBody FlowStartDTO dto)
    {
        return success(flowProcessService.startProcess(dto));
    }

    /**
     * 查询我发起的流程（支持流程标题/创建日期等条件，内存过滤后手动分页）
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:myList')")
    @GetMapping("/myList")
    public TableDataInfo myList(FlowInstance query)
    {
        return buildPage(flowProcessService.selectMyProcessList(query));
    }

    /**
     * 查询我参与过的流程（发起+审批过）
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:query')")
    @GetMapping("/involved")
    public TableDataInfo involved(FlowInstance query)
    {
        return buildPage(flowProcessService.selectInvolvedProcessList(query));
    }

    /** 对已过滤的完整列表手动分页（Flowable 历史查询不支持 PageHelper 拦截） */
    private TableDataInfo buildPage(List<FlowInstance> list)
    {
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
     * 流程分类树（左侧树）
     */
    @PreAuthorize("@ss.hasAnyPermi('flowable:task:todo,flowable:task:done,flowable:process:query,flowable:process:start')")
    @GetMapping("/tree")
    public AjaxResult tree()
    {
        return success(flowProcessService.selectProcessTree());
    }

    /**
     * 流程实例详情
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:query')")
    @GetMapping("/{instanceId}")
    public AjaxResult getInfo(@PathVariable("instanceId") String instanceId)
    {
        return success(flowProcessService.selectInstanceDetail(instanceId));
    }

    /**
     * 流程实例流转记录
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:query')")
    @GetMapping("/history/{instanceId}")
    public AjaxResult history(@PathVariable("instanceId") String instanceId)
    {
        return success(flowProcessService.selectHistoryTasks(instanceId));
    }

    /**
     * 流程图（高亮当前节点）
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:query')")
    @GetMapping("/diagram/{instanceId}")
    public ResponseEntity<byte[]> diagram(@PathVariable("instanceId") String instanceId)
    {
        byte[] png = flowProcessService.generateProcessDiagram(instanceId);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    /**
     * 撤销流程
     */
    @Log(title = "流程实例", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('flowable:process:cancel')")
    @PutMapping("/cancel/{instanceId}")
    public AjaxResult cancel(@PathVariable("instanceId") String instanceId,
            @RequestParam(value = "reason", required = false) String reason)
    {
        return toAjax(flowProcessService.cancelProcess(instanceId, reason));
    }
}
