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
import com.ruoyi.hr.domain.HrLeave;
import com.ruoyi.hr.domain.HrLeaveAuditLog;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrLeaveMapper;
import com.ruoyi.hr.mapper.HrLeaveAuditLogMapper;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.service.IHrLeaveService;

@Service
public class HrLeaveServiceImpl implements IHrLeaveService
{
    @Autowired
    private HrLeaveMapper hrLeaveMapper;
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;
    @Autowired
    private HrLeaveAuditLogMapper hrLeaveAuditLogMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrLeave selectHrLeaveById(Long leaveId)
    {
        HrLeave leave = hrLeaveMapper.selectHrLeaveById(leaveId);
        if (leave != null)
        {
            leave.setAuditLogList(hrLeaveAuditLogMapper.selectHrLeaveAuditLogByLeaveId(leaveId));
        }
        return leave;
    }

    @Override
    public List<HrLeave> selectHrLeaveList(HrLeave hrLeave) { return hrLeaveMapper.selectHrLeaveList(hrLeave); }

    @Override
    public int insertHrLeave(HrLeave hrLeave)
    {
        if (StringUtils.isEmpty(hrLeave.getLeaveNo()))
        {
            hrLeave.setLeaveNo(mkNumberRuleService.generateNumber("hr_leave"));
        }
        if (StringUtils.isEmpty(hrLeave.getAuditStatus()))
        {
            hrLeave.setAuditStatus("0");
        }
        return hrLeaveMapper.insertHrLeave(hrLeave);
    }

    @Override
    public int updateHrLeave(HrLeave hrLeave)
    {
        // 驳回后修改重新提交：状态重置为待审批
        if ("2".equals(hrLeave.getAuditStatus()))
        {
            hrLeave.setAuditStatus("0");
        }
        return hrLeaveMapper.updateHrLeave(hrLeave);
    }

    @Override
    public int deleteHrLeaveByIds(Long[] leaveIds)
    {
        int rows = hrLeaveMapper.deleteHrLeaveByIds(leaveIds);
        for (Long leaveId : leaveIds)
        {
            hrLeaveAuditLogMapper.deleteHrLeaveAuditLogByLeaveId(leaveId);
        }
        return rows;
    }

    @Override
    public int deleteHrLeaveById(Long leaveId)
    {
        hrLeaveAuditLogMapper.deleteHrLeaveAuditLogByLeaveId(leaveId);
        return hrLeaveMapper.deleteHrLeaveById(leaveId);
    }

    /**
     * 离职审核
     *
     * @param leaveId 离职单ID
     * @param auditAction 审核动作（1通过 2驳回）
     * @param auditRemark 审核意见
     * @return 结果
     */
    @Override
    @Transactional
    public int auditHrLeave(Long leaveId, String auditAction, String auditRemark)
    {
        HrLeave exist = hrLeaveMapper.selectHrLeaveById(leaveId);
        if (exist == null)
        {
            throw new ServiceException("离职单不存在");
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
        HrLeave update = new HrLeave();
        update.setLeaveId(leaveId);
        update.setAuditStatus(auditAction);
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(new Date());
        update.setAuditRemark(auditRemark);
        int rows = hrLeaveMapper.updateHrLeave(update);
        if (rows > 0)
        {
            HrLeaveAuditLog log = new HrLeaveAuditLog();
            log.setLeaveId(leaveId);
            log.setAuditAction(auditAction);
            log.setAuditBy(SecurityUtils.getUsername());
            log.setAuditTime(update.getAuditTime());
            log.setAuditRemark(auditRemark);
            hrLeaveAuditLogMapper.insertHrLeaveAuditLog(log);
        }
        // 审批通过：同步员工档案（状态置为离职，并更新离职日期/离职原因）
        if (rows > 0 && "1".equals(auditAction))
        {
            syncEmployeeArchive(exist);
        }
        return rows;
    }

    /** 审批通过后同步员工档案 */
    private void syncEmployeeArchive(HrLeave leave)
    {
        if (leave.getEmployeeId() == null)
        {
            return;
        }
        HrEmployee emp = new HrEmployee();
        emp.setEmployeeId(leave.getEmployeeId());
        emp.setEmployeeStatus("2");
        emp.setLeaveDate(leave.getLeaveDate());
        if (StringUtils.isNotEmpty(leave.getReason()))
        {
            emp.setLeaveReason(leave.getReason());
        }
        hrEmployeeMapper.updateHrEmployee(emp);
    }
}
