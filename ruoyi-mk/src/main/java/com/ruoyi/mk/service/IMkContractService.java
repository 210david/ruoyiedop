package com.ruoyi.mk.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.ruoyi.mk.domain.MkContract;
import com.ruoyi.mk.domain.MkContractChangeLog;

/**
 * 合同 Service接口
 *
 * @author ruoyi
 */
public interface IMkContractService
{
    public List<MkContract> selectContractList(MkContract contract);
    public MkContract selectContractById(Long contractId);
    public int insertContract(MkContract contract);
    public int updateContract(MkContract contract);
    public int deleteContractByIds(Long[] contractIds);

    /** 提交审批：草稿(0) -> 审批中(1) */
    public int submitApproval(Long contractId);

    /** 审批通过：审批中(1) -> 已生效(2) */
    public int approveContract(Long contractId, String approveOpinion);

    /** 审批驳回：审批中(1) -> 草稿(0) */
    public int rejectContract(Long contractId, String approveOpinion);

    /** 终止合同：已生效(2) -> 已终止(4) */
    public int terminateContract(Long contractId, String terminateReason);

    /** 检查并更新过期合同：已生效(2) -> 已过期(3) */
    public int checkExpiredContracts();

    /** 查询即将到期的合同（指定天数内） */
    public List<MkContract> selectExpiringContracts(int days);

    /** 合同续签 */
    public int renewContract(Long contractId, com.ruoyi.mk.domain.dto.ContractRenewDTO renewDTO);

    /** 查询合同变更记录 */
    public List<MkContractChangeLog> selectContractChangeLogs(Long contractId);

    /** 提交合同变更 */
    public int submitContractChange(MkContractChangeLog changeLog);
}
