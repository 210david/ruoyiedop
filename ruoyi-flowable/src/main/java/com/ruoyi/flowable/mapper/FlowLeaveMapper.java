package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowLeave;

/**
 * 请假单 Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowLeaveMapper
{
    public FlowLeave selectFlowLeaveByLeaveId(Long leaveId);

    public List<FlowLeave> selectFlowLeaveList(FlowLeave leave);

    public int insertFlowLeave(FlowLeave leave);

    public int updateFlowLeave(FlowLeave leave);

    public int deleteFlowLeaveByLeaveIds(Long[] leaveIds);
}
