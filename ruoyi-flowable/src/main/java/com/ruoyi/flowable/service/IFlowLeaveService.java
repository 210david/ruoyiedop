package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowLeave;

/**
 * 请假单 Service 接口（工作流示例业务）
 *
 * @author ruoyi
 */
public interface IFlowLeaveService
{
    /** 查询我的请假单列表 */
    public List<FlowLeave> selectFlowLeaveList(FlowLeave leave);

    /** 查询请假单详情 */
    public FlowLeave selectFlowLeaveByLeaveId(Long leaveId);

    /** 新增请假单（草稿） */
    public int insertFlowLeave(FlowLeave leave);

    /** 修改请假单（仅草稿/驳回可改） */
    public int updateFlowLeave(FlowLeave leave);

    /** 删除请假单（草稿/驳回可删） */
    public int deleteFlowLeaveByLeaveIds(Long[] leaveIds);

    /** 提交审批：发起流程并回写实例ID与状态 */
    public String submitFlowLeave(Long leaveId, Long approverUserId);

    /** 流程结束回调：按最终审批结果回写业务状态（2通过 3驳回） */
    public void onProcessFinished(String instanceId, String businessKey);
}
