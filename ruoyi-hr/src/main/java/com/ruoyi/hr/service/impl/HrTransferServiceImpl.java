package com.ruoyi.hr.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrTransfer;
import com.ruoyi.hr.domain.HrTransferAuditLog;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrTransferMapper;
import com.ruoyi.hr.mapper.HrTransferAuditLogMapper;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.service.IHrTransferService;

@Service
public class HrTransferServiceImpl implements IHrTransferService
{
    @Autowired
    private HrTransferMapper hrTransferMapper;
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;
    @Autowired
    private HrTransferAuditLogMapper hrTransferAuditLogMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrTransfer selectHrTransferById(Long transferId)
    {
        HrTransfer transfer = hrTransferMapper.selectHrTransferById(transferId);
        if (transfer != null)
        {
            transfer.setAuditLogList(hrTransferAuditLogMapper.selectHrTransferAuditLogByTransferId(transferId));
        }
        return transfer;
    }

    @Override
    public List<HrTransfer> selectHrTransferList(HrTransfer hrTransfer) { return hrTransferMapper.selectHrTransferList(hrTransfer); }

    @Override
    public int insertHrTransfer(HrTransfer hrTransfer)
    {
        if (StringUtils.isEmpty(hrTransfer.getTransferNo()))
        {
            hrTransfer.setTransferNo(mkNumberRuleService.generateNumber("hr_transfer"));
        }
        if (StringUtils.isEmpty(hrTransfer.getAuditStatus()))
        {
            hrTransfer.setAuditStatus("0");
        }
        return hrTransferMapper.insertHrTransfer(hrTransfer);
    }

    @Override
    public int updateHrTransfer(HrTransfer hrTransfer)
    {
        // 驳回后修改重新提交：状态重置为待审批
        if ("2".equals(hrTransfer.getAuditStatus()))
        {
            hrTransfer.setAuditStatus("0");
        }
        return hrTransferMapper.updateHrTransfer(hrTransfer);
    }

    @Override
    public int deleteHrTransferByIds(Long[] transferIds)
    {
        int rows = hrTransferMapper.deleteHrTransferByIds(transferIds);
        for (Long transferId : transferIds)
        {
            hrTransferAuditLogMapper.deleteHrTransferAuditLogByTransferId(transferId);
        }
        return rows;
    }

    @Override
    public int deleteHrTransferById(Long transferId)
    {
        hrTransferAuditLogMapper.deleteHrTransferAuditLogByTransferId(transferId);
        return hrTransferMapper.deleteHrTransferById(transferId);
    }

    /**
     * 调动审核
     *
     * @param transferId 调动单ID
     * @param auditAction 审核动作（1通过 2驳回）
     * @param auditRemark 审核意见
     * @return 结果
     */
    @Override
    @Transactional
    public int auditHrTransfer(Long transferId, String auditAction, String auditRemark)
    {
        HrTransfer exist = hrTransferMapper.selectHrTransferById(transferId);
        if (exist == null)
        {
            throw new ServiceException("调动单不存在");
        }
        if (!"1".equals(auditAction) && !"2".equals(auditAction))
        {
            throw new ServiceException("审核动作不合法");
        }
        // 仅待审批状态可审批；已驳回需修改后重新提交（状态回到待审批）才能再审
        if (!"0".equals(exist.getAuditStatus()))
        {
            throw new ServiceException("当前状态不允许审批" + ("2".equals(exist.getAuditStatus()) ? "，请修改后重新提交" : ""));
        }
        HrTransfer update = new HrTransfer();
        update.setTransferId(transferId);
        update.setAuditStatus(auditAction);
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(new Date());
        update.setAuditRemark(auditRemark);
        int rows = hrTransferMapper.updateHrTransfer(update);
        if (rows > 0)
        {
            HrTransferAuditLog log = new HrTransferAuditLog();
            log.setTransferId(transferId);
            log.setAuditAction(auditAction);
            log.setAuditBy(SecurityUtils.getUsername());
            log.setAuditTime(update.getAuditTime());
            log.setAuditRemark(auditRemark);
            hrTransferAuditLogMapper.insertHrTransferAuditLog(log);
        }
        // 审批通过：同步员工档案（目标部门/目标岗位）
        if (rows > 0 && "1".equals(auditAction))
        {
            syncEmployeeArchive(exist);
        }
        return rows;
    }

    /** 审批通过后同步员工档案 */
    private void syncEmployeeArchive(HrTransfer transfer)
    {
        if (transfer.getEmployeeId() == null)
        {
            return;
        }
        HrEmployee emp = new HrEmployee();
        emp.setEmployeeId(transfer.getEmployeeId());
        emp.setOrgId(transfer.getToOrgId());
        if (StringUtils.isNotEmpty(transfer.getToPosition()))
        {
            emp.setPositionName(transfer.getToPosition());
        }
        hrEmployeeMapper.updateHrEmployee(emp);
    }
}
