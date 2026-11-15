package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowNodeForm;

/**
 * 流程节点表单模板 Service 接口
 *
 * @author ruoyi
 */
public interface IFlowNodeFormService
{
    /** 查询模型下全部节点模板 */
    public List<FlowNodeForm> selectByModelId(String modelId);

    /** 按流程Key取发起节点模板 */
    public FlowNodeForm selectStartByModelKey(String modelKey);

    /** 按流程Key + 节点ID取节点模板 */
    public FlowNodeForm selectByModelKeyAndNodeId(String modelKey, String nodeId);

    /** 保存（存在则更新，不存在则新增） */
    public int saveNodeForm(FlowNodeForm nodeForm);

    /** 恢复默认表单（删除节点模板） */
    public int deleteByModelIdAndNodeId(String modelId, String nodeId);
}
