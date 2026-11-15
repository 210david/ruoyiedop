package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.flowable.domain.FlowNodeForm;
import com.ruoyi.flowable.service.IFlowNodeFormService;

/**
 * 流程节点表单模板 Controller（节点表单设计落库，轻量修改无需重新部署流程）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/nodeForm")
public class FlowNodeFormController extends BaseController
{
    @Autowired
    private IFlowNodeFormService flowNodeFormService;

    /** 设计器加载：模型下全部节点模板 */
    @GetMapping("/list/{modelId}")
    public AjaxResult list(@PathVariable("modelId") String modelId)
    {
        List<FlowNodeForm> list = flowNodeFormService.selectByModelId(modelId);
        return success(list);
    }

    /** 发起页：按流程Key取发起节点模板 */
    @GetMapping("/startForm/{processKey}")
    public AjaxResult startForm(@PathVariable("processKey") String processKey)
    {
        return success(flowNodeFormService.selectStartByModelKey(processKey));
    }

    /** 保存节点模板（存在则更新） */
    @PostMapping("/save")
    public AjaxResult save(@RequestBody FlowNodeForm nodeForm)
    {
        return toAjax(flowNodeFormService.saveNodeForm(nodeForm));
    }

    /** 恢复使用数据表默认表单（删除该节点模板；不存在也视为成功） */
    @DeleteMapping("/{modelId}/{nodeId}")
    public AjaxResult remove(@PathVariable("modelId") String modelId, @PathVariable("nodeId") String nodeId)
    {
        flowNodeFormService.deleteByModelIdAndNodeId(modelId, nodeId);
        return success();
    }
}
