package com.ruoyi.mk.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.domain.MkContract;
import com.ruoyi.mk.domain.MkContractChangeLog;
import com.ruoyi.mk.mapper.MkContractChangeLogMapper;
import com.ruoyi.mk.mapper.MkContractMapper;
import com.ruoyi.mk.service.IMkContractChangeService;

/**
 * 合同变更 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkContractChangeServiceImpl implements IMkContractChangeService
{
    @Autowired
    private MkContractChangeLogMapper mkContractChangeLogMapper;

    @Autowired
    private MkContractMapper mkContractMapper;

    @Override
    public List<MkContractChangeLog> selectChangeLogList(MkContractChangeLog changeLog)
    {
        return mkContractChangeLogMapper.selectChangeLogList(changeLog);
    }

    @Override
    public List<MkContractChangeLog> selectChangeLogByContractId(Long contractId)
    {
        return mkContractChangeLogMapper.selectChangeLogByContractId(contractId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertChangeLog(MkContractChangeLog changeLog)
    {
        if (changeLog.getChangeStatus() == null)
        {
            changeLog.setChangeStatus("0");
        }
        // 从合同补充合同编号
        if (changeLog.getContractId() != null)
        {
            MkContract contract = mkContractMapper.selectContractById(changeLog.getContractId());
            if (contract != null)
            {
                changeLog.setContractNo(contract.getContractNo());
            }
        }
        changeLog.setCreateBy(SecurityUtils.getUsername());
        return mkContractChangeLogMapper.insertChangeLog(changeLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveChange(Long logId, boolean approved, String opinion)
    {
        MkContractChangeLog changeLog = mkContractChangeLogMapper.selectChangeLogById(logId);
        if (changeLog == null)
        {
            throw new ServiceException("变更记录不存在");
        }
        if (!"0".equals(changeLog.getChangeStatus()))
        {
            throw new ServiceException("非待审批状态的变更不能操作");
        }
        changeLog.setChangeStatus(approved ? "1" : "2");
        changeLog.setApproveOpinion(opinion);
        changeLog.setApproveBy(SecurityUtils.getUsername());
        changeLog.setApproveTime(new Date());

        // 如果批准，执行变更
        if (approved)
        {
            applyChange(changeLog);
            changeLog.setChangeStatus("3");
        }
        // 无论批准还是驳回，都将合同状态恢复为"已生效"
        mkContractMapper.updateContractStatus(changeLog.getContractId(), "2");
        return mkContractChangeLogMapper.updateChangeLog(changeLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveChangesByContractId(Long contractId, boolean approved, String opinion)
    {
        List<MkContractChangeLog> pendingLogs = mkContractChangeLogMapper.selectChangeLogByContractId(contractId);
        if (pendingLogs == null || pendingLogs.isEmpty())
        {
            throw new ServiceException("未找到变更记录");
        }
        String username = SecurityUtils.getUsername();
        Date now = new Date();
        int rows = 0;
        for (MkContractChangeLog log : pendingLogs)
        {
            if (!"0".equals(log.getChangeStatus()))
            {
                continue;
            }
            log.setApproveOpinion(opinion);
            log.setApproveBy(username);
            log.setApproveTime(now);
            if (approved)
            {
                applyChange(log);
                log.setChangeStatus("3");
            }
            else
            {
                log.setChangeStatus("2");
            }
            rows += mkContractChangeLogMapper.updateChangeLog(log);
        }
        // 无论批准还是驳回，都将合同状态恢复为"已生效"
        mkContractMapper.updateContractStatus(contractId, "2");
        return rows;
    }

    /**
     * 执行合同变更
     */
    private void applyChange(MkContractChangeLog changeLog)
    {
        MkContract update = new MkContract();
        update.setContractId(changeLog.getContractId());
        update.setUpdateBy(SecurityUtils.getUsername());

        String field = changeLog.getFieldName();
        if (field != null)
        {
            switch (field)
            {
                case "合同金额":
                case "contractAmount":
                    update.setContractAmount(new java.math.BigDecimal(changeLog.getNewValue()));
                    break;
                case "生效日期":
                case "effectiveDate":
                    update.setEffectiveDate(java.sql.Date.valueOf(changeLog.getNewValue()));
                    break;
                case "到期日期":
                case "expireDate":
                    update.setExpireDate(java.sql.Date.valueOf(changeLog.getNewValue()));
                    break;
                case "签约日期":
                case "signDate":
                    update.setSignDate(java.sql.Date.valueOf(changeLog.getNewValue()));
                    break;
                case "付款方式":
                case "paymentMethod":
                    update.setPaymentMethod(changeLog.getNewValue());
                    break;
                case "合同名称":
                case "contractName":
                    update.setContractName(changeLog.getNewValue());
                    break;
            }
            mkContractMapper.updateContract(update);
        }
    }
}
