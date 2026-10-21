package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkContractChangeLog;

/**
 * 合同变更 Service接口
 *
 * @author ruoyi
 */
public interface IMkContractChangeService
{
    List<MkContractChangeLog> selectChangeLogList(MkContractChangeLog changeLog);
    List<MkContractChangeLog> selectChangeLogByContractId(Long contractId);
    int insertChangeLog(MkContractChangeLog changeLog);
    /** 审批变更（单条） */
    int approveChange(Long logId, boolean approved, String opinion);

    /** 批量审批变更（按合同ID） */
    int approveChangesByContractId(Long contractId, boolean approved, String opinion);
}
