package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.flowable.domain.FlowNodePrint;
import com.ruoyi.flowable.service.IFlowNodePrintService;

/**
 * 流程节点打印模板 Controller
 *
 * <p>流程设计器里按节点配置打印模板；办理页打印时优先使用这里的配置，
 * 没有配置则回退到该节点的显示模板。</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/nodePrint")
public class FlowNodePrintController extends BaseController
{
    @Autowired
    private IFlowNodePrintService flowNodePrintService;

    /** 某流程模型下的全部节点打印模板（设计器加载时一并取回） */
    @PreAuthorize("@ss.hasPermi('flowable:model:edit')")
    @GetMapping("/list/{modelId}")
    public AjaxResult list(@PathVariable String modelId)
    {
        List<FlowNodePrint> list = flowNodePrintService.selectByModelId(modelId);
        return success(list);
    }

    /** 保存节点打印模板（同一节点覆盖） */
    @PreAuthorize("@ss.hasPermi('flowable:model:edit')")
    @PostMapping("/save")
    public AjaxResult save(@RequestBody FlowNodePrint nodePrint)
    {
        return toAjax(flowNodePrintService.saveNodePrint(nodePrint));
    }

    /** 删除节点打印模板：恢复为「跟随节点显示模板」 */
    @PreAuthorize("@ss.hasPermi('flowable:model:edit')")
    @DeleteMapping("/{modelId}/{nodeId}")
    public AjaxResult remove(@PathVariable String modelId, @PathVariable String nodeId)
    {
        return toAjax(flowNodePrintService.deleteByModelIdAndNodeId(modelId, nodeId));
    }
}
