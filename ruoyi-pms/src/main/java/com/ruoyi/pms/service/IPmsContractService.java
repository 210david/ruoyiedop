package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.pms.domain.PmsContract;

/**
 * 采购合同 Service接口
 *
 * @author ruoyi
 */
public interface IPmsContractService
{
    public List<PmsContract> selectContractList(PmsContract contract);
    public PmsContract selectContractById(Long contractId);
    public int insertContract(PmsContract contract);
    public int updateContract(PmsContract contract);
    public int deleteContractByIds(Long[] contractIds);
    public int addContractChange(com.ruoyi.pms.domain.PmsContractChange change);
    public int auditContractChange(Long changeId, String auditStatus, String auditRemark);
    public int auditContractChangeByContractId(Long contractId, String auditStatus, String auditRemark);

    /** 提交审核 */
    public int submitContract(Long contractId);

    /** 审核合同 */
    public int auditContract(Long contractId, String status, String auditOpinion);

    /** 终止合同 */
    public int terminateContract(Long contractId, String terminateReason);

    /** 检查并标记到期合同 */
    public int checkExpiredContracts();
}
