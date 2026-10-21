package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsDispatch;
import org.apache.ibatis.annotations.Param;

public interface MmsDispatchMapper
{
    public List<MmsDispatch> selectDispatchList(MmsDispatch dispatch);
    public MmsDispatch selectDispatchById(Long dispatchId);
    public int insertDispatch(MmsDispatch dispatch);
    public int updateDispatch(MmsDispatch dispatch);
    public int deleteDispatchByIds(Long[] dispatchIds);

    /**
     * 按工单ID批量更新派工单状态（仅更新未完成的派工单）
     *
     * @param workOrderId 工单ID
     * @param targetStatus 目标状态：3-已取消
     * @param updateBy 操作人
     * @return 受影响行数
     */
    public int updateDispatchStatusByWorkOrder(@Param("workOrderId") Long workOrderId, @Param("targetStatus") String targetStatus, @Param("updateBy") String updateBy);

    /**
     * 按工单ID查询未完成的派工单列表（状态非已完成/已取消）
     *
     * @param workOrderId 工单ID
     * @return 派工单列表
     */
    public List<MmsDispatch> selectActiveDispatchByWorkOrder(@Param("workOrderId") Long workOrderId);

    /**
     * 按工单ID查询所有已完工的派工单列表（状态=已完成2），按op_seq升序
     *
     * @param workOrderId 工单ID
     * @return 派工单列表
     */
    public List<MmsDispatch> selectCompletedDispatchByWorkOrder(@Param("workOrderId") Long workOrderId);
}
