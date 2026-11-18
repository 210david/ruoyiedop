package com.ruoyi.hr.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrOvertime;
import com.ruoyi.hr.domain.HrOvertimeAuditLog;
import com.ruoyi.hr.mapper.HrOvertimeMapper;
import com.ruoyi.hr.mapper.HrOvertimeAuditLogMapper;
import com.ruoyi.hr.service.IHrOvertimeService;

@Service
public class HrOvertimeServiceImpl implements IHrOvertimeService
{
    @Autowired
    private HrOvertimeMapper hrOvertimeMapper;
    @Autowired
    private HrOvertimeAuditLogMapper hrOvertimeAuditLogMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrOvertime selectHrOvertimeById(Long overtimeId)
    {
        HrOvertime overtime = hrOvertimeMapper.selectHrOvertimeById(overtimeId);
        if (overtime != null)
        {
            overtime.setAuditLogList(hrOvertimeAuditLogMapper.selectHrOvertimeAuditLogByOvertimeId(overtimeId));
        }
        return overtime;
    }

    @Override
    @DataScope(deptAlias = "biz_hr_overtime")
    public List<HrOvertime> selectHrOvertimeList(HrOvertime hrOvertime) { return hrOvertimeMapper.selectHrOvertimeList(hrOvertime); }

    @Override
    public int insertHrOvertime(HrOvertime hrOvertime)
    {
        if (StringUtils.isEmpty(hrOvertime.getOvertimeNo()))
        {
            hrOvertime.setOvertimeNo(mkNumberRuleService.generateNumber("hr_overtime"));
        }
        computeOvertimeHours(hrOvertime);
        if (StringUtils.isEmpty(hrOvertime.getAuditStatus()))
        {
            hrOvertime.setAuditStatus("0");
        }
        return hrOvertimeMapper.insertHrOvertime(hrOvertime);
    }

    @Override
    public int updateHrOvertime(HrOvertime hrOvertime)
    {
        // 驳回后修改重新提交：状态重置为待审批，清空审批信息
        if ("2".equals(hrOvertime.getAuditStatus()))
        {
            hrOvertime.setAuditStatus("0");
            hrOvertime.setAuditBy(null);
            hrOvertime.setAuditTime(null);
            hrOvertime.setAuditRemark("");
        }
        computeOvertimeHours(hrOvertime);
        return hrOvertimeMapper.updateHrOvertime(hrOvertime);
    }

    /** 根据开始/结束时间自动计算加班小时 */
    private void computeOvertimeHours(HrOvertime overtime)
    {
        if (overtime.getStartTime() == null || overtime.getEndTime() == null
                || !overtime.getEndTime().after(overtime.getStartTime()))
        {
            return;
        }
        long millis = overtime.getEndTime().getTime() - overtime.getStartTime().getTime();
        overtime.setOvertimeHours(java.math.BigDecimal.valueOf(Math.round(millis / 3600000.0 * 100.0) / 100.0));
    }

    @Override
    public int deleteHrOvertimeByIds(Long[] overtimeIds)
    {
        int rows = hrOvertimeMapper.deleteHrOvertimeByIds(overtimeIds);
        for (Long overtimeId : overtimeIds)
        {
            hrOvertimeAuditLogMapper.deleteHrOvertimeAuditLogByOvertimeId(overtimeId);
        }
        return rows;
    }

    @Override
    public int deleteHrOvertimeById(Long overtimeId)
    {
        hrOvertimeAuditLogMapper.deleteHrOvertimeAuditLogByOvertimeId(overtimeId);
        return hrOvertimeMapper.deleteHrOvertimeById(overtimeId);
    }
}
