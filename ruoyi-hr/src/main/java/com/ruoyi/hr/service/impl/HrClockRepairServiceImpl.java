package com.ruoyi.hr.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.text.SimpleDateFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrAttendanceRecord;
import com.ruoyi.hr.domain.HrClockRepair;
import com.ruoyi.hr.mapper.HrAttendanceRecordMapper;
import com.ruoyi.hr.mapper.HrClockRepairMapper;
import com.ruoyi.hr.mapper.HrClockRepairAuditLogMapper;
import com.ruoyi.hr.service.IHrClockRepairService;

@Service
public class HrClockRepairServiceImpl implements IHrClockRepairService
{
    @Autowired
    private HrClockRepairMapper hrClockRepairMapper;
    @Autowired
    private HrClockRepairAuditLogMapper hrClockRepairAuditLogMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;
    @Autowired
    private HrAttendanceRecordMapper hrAttendanceRecordMapper;

    @Override
    public HrClockRepair selectHrClockRepairById(Long clockRepairId)
    {
        HrClockRepair repair = hrClockRepairMapper.selectHrClockRepairById(clockRepairId);
        if (repair != null)
        {
            repair.setAuditLogList(hrClockRepairAuditLogMapper.selectHrClockRepairAuditLogByClockRepairId(clockRepairId));
        }
        return repair;
    }

    @Override
    @DataScope(deptAlias = "biz_hr_clock_repair")
    public List<HrClockRepair> selectHrClockRepairList(HrClockRepair hrClockRepair) { return hrClockRepairMapper.selectHrClockRepairList(hrClockRepair); }

    @Override
    public int insertHrClockRepair(HrClockRepair hrClockRepair)
    {
        if (StringUtils.isEmpty(hrClockRepair.getClockRepairNo()))
        {
            hrClockRepair.setClockRepairNo(mkNumberRuleService.generateNumber("hr_clock_repair"));
        }
        if (StringUtils.isEmpty(hrClockRepair.getAuditStatus()))
        {
            hrClockRepair.setAuditStatus("0");
        }
        // 校验：当天已有对应时段的打卡记录则不允许补卡
        checkExistingClockRecord(hrClockRepair);
        return hrClockRepairMapper.insertHrClockRepair(hrClockRepair);
    }

    @Override
    public int updateHrClockRepair(HrClockRepair hrClockRepair)
    {
        if ("2".equals(hrClockRepair.getAuditStatus()))
        {
            hrClockRepair.setAuditStatus("0");
            hrClockRepair.setAuditBy(null);
            hrClockRepair.setAuditTime(null);
            hrClockRepair.setAuditRemark("");
        }
        // 校验：当天已有对应时段的打卡记录则不允许补卡
        checkExistingClockRecord(hrClockRepair);
        return hrClockRepairMapper.updateHrClockRepair(hrClockRepair);
    }

    /**
     * 校验：如果当天已有考勤记录且对应时段已有打卡，则提示不允许补卡
     */
    private void checkExistingClockRecord(HrClockRepair repair)
    {
        if (repair.getEmployeeNo() == null || repair.getRepairDate() == null || repair.getRepairType() == null)
        {
            return;
        }
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(repair.getRepairDate());
        HrAttendanceRecord record = hrAttendanceRecordMapper.selectByEmployeeNoAndDate(repair.getEmployeeNo(), dateStr);
        if (record == null)
        {
            return;
        }
        // repairType: 0=上班卡 1=下班卡，逗号分隔可多选
        String[] types = repair.getRepairType().split(",");
        for (String type : types)
        {
            if ("0".equals(type) && record.getClockIn() != null)
            {
                throw new ServiceException("该员工在 " + dateStr + " 已有上班打卡记录，无需补卡");
            }
            if ("1".equals(type) && record.getClockOut() != null)
            {
                throw new ServiceException("该员工在 " + dateStr + " 已有下班打卡记录，无需补卡");
            }
        }
    }

    @Override
    public int deleteHrClockRepairByIds(Long[] clockRepairIds)
    {
        int rows = hrClockRepairMapper.deleteHrClockRepairByIds(clockRepairIds);
        for (Long clockRepairId : clockRepairIds)
        {
            hrClockRepairAuditLogMapper.deleteHrClockRepairAuditLogByClockRepairId(clockRepairId);
        }
        return rows;
    }

    @Override
    public int deleteHrClockRepairById(Long clockRepairId)
    {
        hrClockRepairAuditLogMapper.deleteHrClockRepairAuditLogByClockRepairId(clockRepairId);
        return hrClockRepairMapper.deleteHrClockRepairById(clockRepairId);
    }
}
