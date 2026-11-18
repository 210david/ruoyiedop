package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrLeaveRequest;

public interface IHrLeaveRequestService {
    public HrLeaveRequest selectHrLeaveRequestById(Long leaveReqId);
    public List<HrLeaveRequest> selectHrLeaveRequestList(HrLeaveRequest hrLeaveRequest);
    public int insertHrLeaveRequest(HrLeaveRequest hrLeaveRequest);
    public int updateHrLeaveRequest(HrLeaveRequest hrLeaveRequest);
    public int deleteHrLeaveRequestByIds(Long[] leaveReqIds);
    public int deleteHrLeaveRequestById(Long leaveReqId);

    /** 流程办结：解冻发起时占用的假期额度，并转为已使用 */
    public void settleLeaveBalanceByFlow(String instanceId);

    /** 流程终止/撤销：解冻发起时占用的假期额度 */
    public void cancelLeaveBalanceByFlow(String instanceId);

    /** 流程复活：把已办结扣减的额度退回并重新冻结 */
    public void rollbackLeaveBalanceByFlow(String instanceId);
}
