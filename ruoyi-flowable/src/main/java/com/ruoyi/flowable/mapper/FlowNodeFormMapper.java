package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowNodeForm;

/**
 * 流程节点表单模板 Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowNodeFormMapper
{
    public List<FlowNodeForm> selectByModelId(String modelId);

    public FlowNodeForm selectByModelIdAndNodeId(@Param("modelId") String modelId, @Param("nodeId") String nodeId);

    public FlowNodeForm selectStartByModelKey(String modelKey);

    public FlowNodeForm selectByModelKeyAndNodeId(@Param("modelKey") String modelKey, @Param("nodeId") String nodeId);

    public int insertFlowNodeForm(FlowNodeForm nodeForm);

    public int updateFlowNodeForm(FlowNodeForm nodeForm);

    public int deleteByModelIdAndNodeId(@Param("modelId") String modelId, @Param("nodeId") String nodeId);
}
