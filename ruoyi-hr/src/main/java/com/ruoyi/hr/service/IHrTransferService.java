package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrTransfer;

public interface IHrTransferService {
    public HrTransfer selectHrTransferById(Long transferId);
    public List<HrTransfer> selectHrTransferList(HrTransfer hrTransfer);
    public int insertHrTransfer(HrTransfer hrTransfer);
    public int updateHrTransfer(HrTransfer hrTransfer);
    public int deleteHrTransferByIds(Long[] transferIds);
    public int deleteHrTransferById(Long transferId);

    /**
     * 调动审核
     *
     * @param transferId 调动单ID
     * @param auditAction 审核动作（1通过 2驳回）
     * @param auditRemark 审核意见
     * @return 结果
     */
    public int auditHrTransfer(Long transferId, String auditAction, String auditRemark);
}
