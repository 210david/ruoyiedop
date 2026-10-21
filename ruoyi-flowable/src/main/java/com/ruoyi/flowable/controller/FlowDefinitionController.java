package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.flowable.domain.FlowModel;
import com.ruoyi.flowable.service.IFlowModelService;

/**
 * 流程定义管理 Controller（已部署的流程版本）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/definition")
public class FlowDefinitionController extends BaseController
{
    @Autowired
    private IFlowModelService flowModelService;

    /**
     * 查询流程定义列表（最新版本）
     */
    @PreAuthorize("@ss.hasPermi('flowable:definition:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowModel model)
    {
        startPage();
        List<FlowModel> list = flowModelService.selectDefinitionList(model);
        return getDataTable(list);
    }

    /**
     * 查看流程定义 BPMN XML
     */
    @PreAuthorize("@ss.hasPermi('flowable:definition:query')")
    @GetMapping("/xml/{processDefinitionId}")
    public AjaxResult getXml(@PathVariable("processDefinitionId") String processDefinitionId)
    {
        return success(flowModelService.getDefinitionBpmnXml(processDefinitionId));
    }

    /**
     * 流程定义流程图（发起前预览：按流程 Key 取当前活动版本，无活动版本时取最高版本）
     * 供前台发起页「流程图」页签使用，尚无实例、不做节点高亮
     */
    @GetMapping("/diagram/{processKey}")
    public ResponseEntity<byte[]> diagram(@PathVariable("processKey") String processKey)
    {
        byte[] png = flowModelService.generateDefinitionDiagram(processKey);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    /**
     * 挂起/激活流程定义
     */
    @Log(title = "流程定义", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:definition:state')")
    @PutMapping("/state/{processDefinitionId}")
    public AjaxResult changeState(@PathVariable("processDefinitionId") String processDefinitionId,
            @RequestParam("suspend") boolean suspend)
    {
        return toAjax(flowModelService.changeDefinitionState(processDefinitionId, suspend));
    }

    /**
     * 更新流程定义版本备注
     */
    @Log(title = "流程定义", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:definition:state')")
    @PutMapping("/remark/{processDefinitionId}")
    public AjaxResult updateRemark(@PathVariable("processDefinitionId") String processDefinitionId,
            @RequestParam("remark") String remark)
    {
        return toAjax(flowModelService.updateDefinitionRemark(processDefinitionId, remark));
    }

    /**
     * 设为当前活动版本（发起流程时优先使用该版本）
     */
    @Log(title = "流程定义", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:definition:state')")
    @PutMapping("/active/{processDefinitionId}")
    public AjaxResult setActive(@PathVariable("processDefinitionId") String processDefinitionId)
    {
        return toAjax(flowModelService.setActiveDefinition(processDefinitionId));
    }
}
