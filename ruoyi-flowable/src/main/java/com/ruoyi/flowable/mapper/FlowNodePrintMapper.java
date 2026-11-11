package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowNodePrint;

/**
 * 流程节点打印模板 Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowNodePrintMapper
{
    public List<FlowNodePrint> selectByModelId(String modelId);

    public FlowNodePrint selectByModelIdAndNodeId(@Param("modelId") String modelId, @Param("nodeId") String nodeId);

    public FlowNodePrint selectByModelKeyAndNodeId(@Param("modelKey") String modelKey, @Param("nodeId") String nodeId);

    public int insertFlowNodePrint(FlowNodePrint nodePrint);

    public int updateFlowNodePrint(FlowNodePrint nodePrint);

    public int deleteByModelIdAndNodeId(@Param("modelId") String modelId, @Param("nodeId") String nodeId);
}
