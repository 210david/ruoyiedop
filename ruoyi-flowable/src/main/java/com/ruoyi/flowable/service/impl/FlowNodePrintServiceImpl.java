package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowForm;
import com.ruoyi.flowable.domain.FlowNodePrint;
import com.ruoyi.flowable.mapper.FlowNodePrintMapper;
import com.ruoyi.flowable.service.IFlowFormService;
import com.ruoyi.flowable.service.IFlowNodePrintService;

/**
 * 流程节点打印模板 Service 实现
 *
 * @author ruoyi
 */
@Service
public class FlowNodePrintServiceImpl implements IFlowNodePrintService
{
    @Autowired
    private FlowNodePrintMapper flowNodePrintMapper;

    @Autowired
    private IFlowFormService flowFormService;

    @Override
    public List<FlowNodePrint> selectByModelId(String modelId)
    {
        return flowNodePrintMapper.selectByModelId(modelId);
    }

    @Override
    public FlowNodePrint selectByModelIdAndNodeId(String modelId, String nodeId)
    {
        return flowNodePrintMapper.selectByModelIdAndNodeId(modelId, nodeId);
    }

    @Override
    public FlowNodePrint selectByModelKeyAndNodeId(String modelKey, String nodeId)
    {
        if (StringUtils.isEmpty(modelKey) || StringUtils.isEmpty(nodeId))
        {
            return null;
        }
        return flowNodePrintMapper.selectByModelKeyAndNodeId(modelKey, nodeId);
    }

    @Override
    public int saveNodePrint(FlowNodePrint nodePrint)
    {
        if (nodePrint == null || StringUtils.isEmpty(nodePrint.getModelId()) || StringUtils.isEmpty(nodePrint.getNodeId()))
        {
            return 0;
        }
        // 只给了来源表单时，从表单库取字段/布局快照，保证打印模板与配置当时一致
        if (StringUtils.isNotEmpty(nodePrint.getTemplateFormId()) && StringUtils.isEmpty(nodePrint.getLayoutJson()))
        {
            try
            {
                FlowForm form = flowFormService.selectFlowFormByFormId(Long.valueOf(nodePrint.getTemplateFormId()));
                if (form != null)
                {
                    nodePrint.setFieldsJson(form.getFieldsJson());
                    nodePrint.setLayoutJson(form.getLayoutJson());
                    if (StringUtils.isEmpty(nodePrint.getTemplateName()))
                    {
                        nodePrint.setTemplateName(form.getFormName());
                    }
                }
            }
            catch (NumberFormatException ignored)
            {
                // 表单ID非法时保留原有快照
            }
        }

        FlowNodePrint exist = flowNodePrintMapper.selectByModelIdAndNodeId(nodePrint.getModelId(), nodePrint.getNodeId());
        if (exist == null)
        {
            nodePrint.setCreateBy(SecurityUtils.getUsername());
            return flowNodePrintMapper.insertFlowNodePrint(nodePrint);
        }
        nodePrint.setId(exist.getId());
        nodePrint.setUpdateBy(SecurityUtils.getUsername());
        return flowNodePrintMapper.updateFlowNodePrint(nodePrint);
    }

    @Override
    public int deleteByModelIdAndNodeId(String modelId, String nodeId)
    {
        return flowNodePrintMapper.deleteByModelIdAndNodeId(modelId, nodeId);
    }
}
