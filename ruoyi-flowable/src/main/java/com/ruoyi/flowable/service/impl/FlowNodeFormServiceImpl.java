package com.ruoyi.flowable.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowNodeForm;
import com.ruoyi.flowable.mapper.FlowNodeFormMapper;
import com.ruoyi.flowable.service.IFlowNodeFormService;

/**
 * 流程节点表单模板 Service 实现
 *
 * @author ruoyi
 */
@Service
public class FlowNodeFormServiceImpl implements IFlowNodeFormService
{
    @Autowired
    private FlowNodeFormMapper flowNodeFormMapper;

    @Override
    public List<FlowNodeForm> selectByModelId(String modelId)
    {
        return flowNodeFormMapper.selectByModelId(modelId);
    }

    @Override
    public FlowNodeForm selectStartByModelKey(String modelKey)
    {
        return flowNodeFormMapper.selectStartByModelKey(modelKey);
    }

    @Override
    public FlowNodeForm selectByModelKeyAndNodeId(String modelKey, String nodeId)
    {
        return flowNodeFormMapper.selectByModelKeyAndNodeId(modelKey, nodeId);
    }

    @Override
    @Transactional
    public int saveNodeForm(FlowNodeForm nodeForm)
    {
        if (StringUtils.isEmpty(nodeForm.getModelId()) || StringUtils.isEmpty(nodeForm.getNodeId()))
        {
            throw new ServiceException("缺少模型ID或节点ID");
        }
        String username = SecurityUtils.getUsername();
        nodeForm.setUpdateBy(username);
        nodeForm.setUpdateTime(new Date());
        FlowNodeForm exist = flowNodeFormMapper.selectByModelIdAndNodeId(nodeForm.getModelId(), nodeForm.getNodeId());
        if (exist == null)
        {
            nodeForm.setCreateBy(username);
            nodeForm.setCreateTime(new Date());
            return flowNodeFormMapper.insertFlowNodeForm(nodeForm);
        }
        return flowNodeFormMapper.updateFlowNodeForm(nodeForm);
    }

    @Override
    @Transactional
    public int deleteByModelIdAndNodeId(String modelId, String nodeId)
    {
        return flowNodeFormMapper.deleteByModelIdAndNodeId(modelId, nodeId);
    }
}
