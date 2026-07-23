package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkContractApproveLog;

public interface MkContractApproveLogMapper
{
    /** 根据合同ID查询审批记录 */
    public List<MkContractApproveLog> selectApproveLogByContractId(Long contractId);

    /** 新增审批记录 */
    public int insertApproveLog(MkContractApproveLog log);

    /** 根据合同ID删除审批记录 */
    public int deleteApproveLogByContractId(Long contractId);
}
