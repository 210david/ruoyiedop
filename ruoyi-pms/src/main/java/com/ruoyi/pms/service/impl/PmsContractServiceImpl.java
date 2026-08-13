package com.ruoyi.pms.service.impl;

import java.util.HashMap;
import java.util.Map;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pms.domain.PmsContract;
import com.ruoyi.pms.domain.PmsContractChange;
import com.ruoyi.pms.domain.PmsContractAuditLog;
import com.ruoyi.pms.mapper.PmsContractMapper;
import com.ruoyi.pms.mapper.PmsContractAuditLogMapper;
import com.ruoyi.pms.service.IPmsContractService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 采购合同 Service实现
 *
 * @author ruoyi
 */
@Service
public class PmsContractServiceImpl implements IPmsContractService
{
    @Autowired
    private PmsContractMapper pmsContractMapper;

    @Autowired
    private PmsContractAuditLogMapper pmsContractAuditLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<PmsContract> selectContractList(PmsContract contract)
    {
        return pmsContractMapper.selectContractList(contract);
    }

    @Override
    public PmsContract selectContractById(Long contractId)
    {
        PmsContract contract = pmsContractMapper.selectContractById(contractId);
        if (contract != null)
        {
            contract.setChangeList(pmsContractMapper.selectContractChangeByContractId(contractId));
            contract.setAuditLogList(pmsContractAuditLogMapper.selectAuditLogByContractId(contractId));
        }
        return contract;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertContract(PmsContract contract)
    {
        if (StringUtils.isEmpty(contract.getContractNo()))
        {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(contract.getContractType())) { params.put("contractType", contract.getContractType()); }
        contract.setContractNo(mkNumberRuleService.generateNumber("pms_contract", params));
        }
        contract.setDelFlag("0");
        if (contract.getStatus() == null)
        {
            contract.setStatus("0");
        }
        if (contract.getChangeCount() == null)
        {
            contract.setChangeCount(0);
        }
        return pmsContractMapper.insertContract(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateContract(PmsContract contract)
    {
        // 驳回状态修改后重置为待审核，清空审核信息（审核日志保留）
        PmsContract existing = pmsContractMapper.selectContractById(contract.getContractId());
        if (existing != null && "6".equals(existing.getStatus()))
        {
            contract.setStatus("5");
            contract.setAuditBy(null);
            contract.setAuditTime(null);
            contract.setAuditOpinion(null);
        }
        return pmsContractMapper.updateContract(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteContractByIds(Long[] contractIds)
    {
        return pmsContractMapper.deleteContractByIds(contractIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addContractChange(PmsContractChange change)
    {
        PmsContract contract = pmsContractMapper.selectContractById(change.getContractId());
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        change.setContractId(contract.getContractId());
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(change.getChangeType())) { params.put("changeType", change.getChangeType()); }
        change.setChangeNo(mkNumberRuleService.generateNumber("pms_contract_change", params));
        change.setChangeDate(new Date());
        change.setChangeBy(SecurityUtils.getUsername());
        change.setAuditStatus("0");
        change.setDelFlag("0");
        int rows = pmsContractMapper.insertContractChange(change);
        // 更新合同变更次数
        contract.setChangeCount((contract.getChangeCount() == null ? 0 : contract.getChangeCount()) + 1);
        // 将合同状态改为"变更审批中"
        contract.setStatus("2");
        pmsContractMapper.updateContract(contract);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditContractChange(Long changeId, String auditStatus, String auditRemark)
    {
        PmsContractChange change = pmsContractMapper.selectContractChangeById(changeId);
        if (change == null)
        {
            throw new ServiceException("变更记录不存在");
        }
        if (!"0".equals(change.getAuditStatus()))
        {
            throw new ServiceException("只有待审核的变更记录才能审核");
        }
        change.setAuditStatus(auditStatus);
        change.setAuditBy(SecurityUtils.getUsername());
        change.setAuditTime(new Date());
        change.setAuditRemark(auditRemark);
        int rows = pmsContractMapper.updateContractChange(change);
        // 审核通过时，将变更应用到合同
        if ("1".equals(auditStatus))
        {
            applyChangeToContract(change);
        }
        // 写入审核日志（变更审核）
        PmsContractAuditLog auditLog = new PmsContractAuditLog();
        auditLog.setContractId(change.getContractId());
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditTime(new Date());
        auditLog.setAuditAction(auditStatus);
        String changeDesc = change.getChangeContent() != null ? change.getChangeContent() : "";
        auditLog.setAuditRemark("【变更审核" + ("1".equals(auditStatus) ? "通过" : "驳回") + "】" + changeDesc + (auditRemark != null && !auditRemark.isEmpty() ? " - " + auditRemark : ""));
        pmsContractAuditLogMapper.insertAuditLog(auditLog);
        // 检查该合同是否还有待审核的变更记录，如果没有则恢复合同状态为"已签订"
        List<PmsContractChange> remainingChanges = pmsContractMapper.selectContractChangeByContractId(change.getContractId());
        boolean hasPending = remainingChanges.stream().anyMatch(c -> "0".equals(c.getAuditStatus()));
        if (!hasPending)
        {
            PmsContract contract = pmsContractMapper.selectContractById(change.getContractId());
            if (contract != null && "2".equals(contract.getStatus()))
            {
                contract.setStatus("1");
                pmsContractMapper.updateContract(contract);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditContractChangeByContractId(Long contractId, String auditStatus, String auditRemark)
    {
        List<PmsContractChange> changes = pmsContractMapper.selectContractChangeByContractId(contractId);
        int rows = 0;
        for (PmsContractChange change : changes)
        {
            if ("0".equals(change.getAuditStatus()))
            {
                change.setAuditStatus(auditStatus);
                change.setAuditBy(SecurityUtils.getUsername());
                change.setAuditTime(new Date());
                change.setAuditRemark(auditRemark);
                rows += pmsContractMapper.updateContractChange(change);
                if ("1".equals(auditStatus))
                {
                    applyChangeToContract(change);
                }
                // 写入审核日志（变更审核）
                PmsContractAuditLog auditLog = new PmsContractAuditLog();
                auditLog.setContractId(change.getContractId());
                auditLog.setAuditBy(SecurityUtils.getUsername());
                auditLog.setAuditTime(new Date());
                auditLog.setAuditAction(auditStatus);
                String changeDesc = change.getChangeContent() != null ? change.getChangeContent() : "";
                auditLog.setAuditRemark("【变更审核" + ("1".equals(auditStatus) ? "通过" : "驳回") + "】" + changeDesc + (auditRemark != null && !auditRemark.isEmpty() ? " - " + auditRemark : ""));
                pmsContractAuditLogMapper.insertAuditLog(auditLog);
            }
        }
        // 检查该合同是否还有待审核的变更记录，如果没有则恢复合同状态为"已签订"
        List<PmsContractChange> remainingChanges = pmsContractMapper.selectContractChangeByContractId(contractId);
        boolean hasPending = remainingChanges.stream().anyMatch(c -> "0".equals(c.getAuditStatus()));
        if (!hasPending)
        {
            PmsContract contract = pmsContractMapper.selectContractById(contractId);
            if (contract != null && "2".equals(contract.getStatus()))
            {
                contract.setStatus("1");
                pmsContractMapper.updateContract(contract);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitContract(Long contractId)
    {
        PmsContract contract = pmsContractMapper.selectContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"0".equals(contract.getStatus()))
        {
            throw new ServiceException("只有草稿状态的合同才能提交审核");
        }
        contract.setStatus("5"); // 待审核
        return pmsContractMapper.updateContract(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditContract(Long contractId, String status, String auditOpinion)
    {
        PmsContract contract = pmsContractMapper.selectContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"5".equals(contract.getStatus()))
        {
            throw new ServiceException("只有待审核状态的合同才能审核");
        }
        contract.setStatus(status);
        contract.setAuditBy(SecurityUtils.getUsername());
        contract.setAuditTime(new Date());
        contract.setAuditOpinion(auditOpinion);
        int rows = pmsContractMapper.updateContract(contract);

        // 写入审核日志（永久留存）
        PmsContractAuditLog auditLog = new PmsContractAuditLog();
        auditLog.setContractId(contractId);
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark("【合同审批" + ("1".equals(status) ? "通过" : "驳回") + "】" + (auditOpinion != null ? auditOpinion : ""));
        // auditAction: 1=通过 2=驳回
        if ("1".equals(status))
        {
            auditLog.setAuditAction("1");
        }
        else
        {
            auditLog.setAuditAction("2");
        }
        pmsContractAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int terminateContract(Long contractId, String terminateReason)
    {
        PmsContract contract = pmsContractMapper.selectContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"1".equals(contract.getStatus()))
        {
            throw new ServiceException("只有已签订的合同才能终止");
        }
        contract.setStatus("4"); // 已终止
        contract.setTerminateDate(new Date());
        contract.setTerminateReason(terminateReason);
        contract.setUpdateBy(SecurityUtils.getUsername());
        return pmsContractMapper.updateContract(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkExpiredContracts()
    {
        List<PmsContract> expiredList = pmsContractMapper.selectExpiredContracts();
        if (expiredList == null || expiredList.isEmpty())
        {
            return 0;
        }
        int count = 0;
        for (PmsContract contract : expiredList)
        {
            contract.setStatus("3"); // 已到期
            contract.setUpdateBy("system");
            pmsContractMapper.updateContract(contract);
            count++;
        }
        return count;
    }

    /**
     * 将变更应用到合同
     */
    private void applyChangeToContract(PmsContractChange change)
    {
        PmsContract update = new PmsContract();
        update.setContractId(change.getContractId());
        update.setUpdateBy(SecurityUtils.getUsername());
        String field = change.getFieldName();
        String newVal = change.getNewValue();
        if (field != null && newVal != null)
        {
            switch (field)
            {
                case "contractAmount":
                    update.setContractAmount(new BigDecimal(newVal));
                    break;
                case "signDate":
                    update.setSignDate(java.sql.Date.valueOf(newVal));
                    break;
                case "expireDate":
                    update.setExpireDate(java.sql.Date.valueOf(newVal));
                    break;
                case "paymentTerms":
                    update.setPaymentTerms(newVal);
                    break;
                case "deliveryTerms":
                    update.setDeliveryTerms(newVal);
                    break;
            }
            pmsContractMapper.updateContract(update);
        }
    }
}
