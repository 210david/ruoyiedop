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
import com.ruoyi.hr.domain.HrRegular;
import com.ruoyi.hr.domain.HrRegularAuditLog;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrRegularMapper;
import com.ruoyi.hr.mapper.HrRegularAuditLogMapper;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.service.IHrRegularService;

@Service
public class HrRegularServiceImpl implements IHrRegularService
{
    @Autowired
    private HrRegularMapper hrRegularMapper;
    @Autowired
    private HrRegularAuditLogMapper hrRegularAuditLogMapper;
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrRegular selectHrRegularById(Long regularId)
    {
        HrRegular regular = hrRegularMapper.selectHrRegularById(regularId);
        if (regular != null)
        {
            regular.setAuditLogList(hrRegularAuditLogMapper.selectHrRegularAuditLogByRegularId(regularId));
        }
        return regular;
    }

    @Override
    public List<HrRegular> selectHrRegularList(HrRegular hrRegular) { return hrRegularMapper.selectHrRegularList(hrRegular); }

    @Override
    public int insertHrRegular(HrRegular hrRegular)
    {
        if (StringUtils.isEmpty(hrRegular.getRegularNo()))
        {
            hrRegular.setRegularNo(mkNumberRuleService.generateNumber("hr_regular"));
        }
        if (StringUtils.isEmpty(hrRegular.getAuditStatus()))
        {
            hrRegular.setAuditStatus("0");
        }
        return hrRegularMapper.insertHrRegular(hrRegular);
    }

    @Override
    public int updateHrRegular(HrRegular hrRegular)
    {
        // 驳回后修改重新提交：状态重置为待审批
        if ("2".equals(hrRegular.getAuditStatus()))
        {
            hrRegular.setAuditStatus("0");
        }
        return hrRegularMapper.updateHrRegular(hrRegular);
    }

    @Override
    public int deleteHrRegularByIds(Long[] regularIds)
    {
        int rows = hrRegularMapper.deleteHrRegularByIds(regularIds);
        for (Long regularId : regularIds)
        {
            hrRegularAuditLogMapper.deleteHrRegularAuditLogByRegularId(regularId);
        }
        return rows;
    }

    @Override
    public int deleteHrRegularById(Long regularId)
    {
        hrRegularAuditLogMapper.deleteHrRegularAuditLogByRegularId(regularId);
        return hrRegularMapper.deleteHrRegularById(regularId);
    }

    /**
     * 转正审核
     *
     * @param regularId 转正单ID
     * @param auditAction 审核动作（1通过 2驳回）
     * @param auditRemark 审核意见
     * @return 结果
     */
    @Override
    @Transactional
    public int auditHrRegular(Long regularId, String auditAction, String auditRemark)
    {
        HrRegular exist = hrRegularMapper.selectHrRegularById(regularId);
        if (exist == null)
        {
            throw new ServiceException("转正单不存在");
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
        HrRegular update = new HrRegular();
        update.setRegularId(regularId);
        update.setAuditStatus(auditAction);
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(new Date());
        update.setAuditRemark(auditRemark);
        int rows = hrRegularMapper.updateHrRegular(update);
        if (rows > 0)
        {
            HrRegularAuditLog log = new HrRegularAuditLog();
            log.setRegularId(regularId);
            log.setAuditAction(auditAction);
            log.setAuditBy(SecurityUtils.getUsername());
            log.setAuditTime(update.getAuditTime());
            log.setAuditRemark(auditRemark);
            hrRegularAuditLogMapper.insertHrRegularAuditLog(log);
        }
        // 审批通过：同步员工档案（状态置为在职，并更新转正日期）
        if (rows > 0 && "1".equals(auditAction))
        {
            syncEmployeeArchive(exist);
        }
        return rows;
    }

    /** 审批通过后同步员工档案 */
    private void syncEmployeeArchive(HrRegular regular)
    {
        if (regular.getEmployeeId() == null)
        {
            return;
        }
        HrEmployee emp = new HrEmployee();
        emp.setEmployeeId(regular.getEmployeeId());
        emp.setEmployeeStatus("0");
        emp.setRegularDate(regular.getRegularDate());
        hrEmployeeMapper.updateHrEmployee(emp);
    }
}
