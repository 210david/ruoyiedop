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
import com.ruoyi.hr.domain.HrEntry;
import com.ruoyi.hr.domain.HrEntryAuditLog;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrEntryMapper;
import com.ruoyi.hr.mapper.HrEntryAuditLogMapper;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.service.IHrEntryService;

@Service
public class HrEntryServiceImpl implements IHrEntryService
{
    @Autowired
    private HrEntryMapper hrEntryMapper;
    @Autowired
    private HrEntryAuditLogMapper hrEntryAuditLogMapper;
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrEntry selectHrEntryById(Long entryId)
    {
        HrEntry entry = hrEntryMapper.selectHrEntryById(entryId);
        if (entry != null)
        {
            entry.setAuditLogList(hrEntryAuditLogMapper.selectHrEntryAuditLogByEntryId(entryId));
        }
        return entry;
    }

    @Override
    public List<HrEntry> selectHrEntryList(HrEntry hrEntry) { return hrEntryMapper.selectHrEntryList(hrEntry); }

    @Override
    public int insertHrEntry(HrEntry hrEntry)
    {
        if (StringUtils.isEmpty(hrEntry.getEntryNo()))
        {
            hrEntry.setEntryNo(mkNumberRuleService.generateNumber("hr_entry"));
        }
        if (StringUtils.isEmpty(hrEntry.getAuditStatus()))
        {
            hrEntry.setAuditStatus("0");
        }
        return hrEntryMapper.insertHrEntry(hrEntry);
    }

    @Override
    public int updateHrEntry(HrEntry hrEntry)
    {
        // 驳回后修改重新提交：状态重置为待审批
        if ("2".equals(hrEntry.getAuditStatus()))
        {
            hrEntry.setAuditStatus("0");
        }
        return hrEntryMapper.updateHrEntry(hrEntry);
    }

    @Override
    public int deleteHrEntryByIds(Long[] entryIds)
    {
        int rows = hrEntryMapper.deleteHrEntryByIds(entryIds);
        for (Long entryId : entryIds)
        {
            hrEntryAuditLogMapper.deleteHrEntryAuditLogByEntryId(entryId);
        }
        return rows;
    }

    @Override
    public int deleteHrEntryById(Long entryId)
    {
        hrEntryAuditLogMapper.deleteHrEntryAuditLogByEntryId(entryId);
        return hrEntryMapper.deleteHrEntryById(entryId);
    }

    /**
     * 入职审核
     *
     * @param entryId 入职单ID
     * @param auditAction 审核动作（1通过 2驳回）
     * @param auditRemark 审核意见
     * @return 结果
     */
    @Override
    @Transactional
    public int auditHrEntry(Long entryId, String auditAction, String auditRemark)
    {
        HrEntry exist = hrEntryMapper.selectHrEntryById(entryId);
        if (exist == null)
        {
            throw new ServiceException("入职单不存在");
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
        HrEntry update = new HrEntry();
        update.setEntryId(entryId);
        update.setAuditStatus(auditAction);
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(new Date());
        update.setAuditRemark(auditRemark);
        int rows = hrEntryMapper.updateHrEntry(update);
        if (rows > 0)
        {
            HrEntryAuditLog log = new HrEntryAuditLog();
            log.setEntryId(entryId);
            log.setAuditAction(auditAction);
            log.setAuditBy(SecurityUtils.getUsername());
            log.setAuditTime(update.getAuditTime());
            log.setAuditRemark(auditRemark);
            hrEntryAuditLogMapper.insertHrEntryAuditLog(log);
        }
        // 审批通过：同步员工档案（状态置为试用期，并更新入职日期/部门/岗位）
        if (rows > 0 && "1".equals(auditAction))
        {
            syncEmployeeArchive(exist);
        }
        return rows;
    }

    /** 审批通过后同步员工档案 */
    private void syncEmployeeArchive(HrEntry entry)
    {
        if (entry.getEmployeeId() == null)
        {
            return;
        }
        HrEmployee emp = new HrEmployee();
        emp.setEmployeeId(entry.getEmployeeId());
        emp.setEmployeeStatus("1");
        emp.setEntryDate(entry.getEntryDate());
        emp.setOrgId(entry.getOrgId());
        emp.setPositionId(entry.getPositionId());
        if (StringUtils.isNotEmpty(entry.getPositionName()))
        {
            emp.setPositionName(entry.getPositionName());
        }
        hrEmployeeMapper.updateHrEmployee(emp);
    }
}
