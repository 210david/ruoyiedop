package com.ruoyi.hr.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrAttendanceRecord;
import com.ruoyi.hr.domain.HrClockRepair;
import com.ruoyi.hr.domain.HrClockRepairAuditLog;
import com.ruoyi.hr.mapper.HrAttendanceRecordMapper;
import com.ruoyi.hr.mapper.HrClockRepairMapper;
import com.ruoyi.hr.mapper.HrClockRepairAuditLogMapper;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.mapper.HrScheduleMapper;
import com.ruoyi.hr.mapper.HrShiftMapper;
import com.ruoyi.hr.service.IHrClockRepairService;
import com.ruoyi.common.utils.SecurityUtils;

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
    @Autowired
    private HrAttendanceRecordServiceImpl hrAttendanceRecordService;
    @Autowired
    private HrScheduleMapper hrScheduleMapper;
    @Autowired
    private HrShiftMapper hrShiftMapper;

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

    @Override
    @Transactional
    public int auditHrClockRepair(Long clockRepairId, String auditAction, String auditRemark)
    {
        HrClockRepair exist = hrClockRepairMapper.selectHrClockRepairById(clockRepairId);
        if (exist == null) { throw new ServiceException("补卡单不存在"); }
        if (!"1".equals(auditAction) && !"2".equals(auditAction)) { throw new ServiceException("审核动作不合法"); }
        if (!"0".equals(exist.getAuditStatus())) { throw new ServiceException("当前状态不允许审批" + ("2".equals(exist.getAuditStatus()) ? "，请修改后重新提交" : "")); }

        String username = SecurityUtils.getUsername();
        Date auditTime = new Date();

        HrClockRepair update = new HrClockRepair();
        update.setClockRepairId(clockRepairId);
        update.setAuditStatus(auditAction);
        update.setAuditBy(username);
        update.setAuditTime(auditTime);
        update.setAuditRemark(auditRemark);
        int rows = hrClockRepairMapper.updateHrClockRepair(update);

        if (rows > 0)
        {
            HrClockRepairAuditLog log = new HrClockRepairAuditLog();
            log.setClockRepairId(clockRepairId);
            log.setAuditAction(auditAction);
            log.setAuditBy(username);
            log.setAuditTime(auditTime);
            log.setAuditRemark(auditRemark);
            hrClockRepairAuditLogMapper.insertHrClockRepairAuditLog(log);

            // 审批通过：将补卡时间写入考勤记录
            if ("1".equals(auditAction))
            {
                applyClockRepairToAttendance(exist);
            }
        }
        return rows;
    }

    /**
     * 审批通过后，将补卡时间写入考勤记录并重新计算考勤状态
     */
    private void applyClockRepairToAttendance(HrClockRepair repair)
    {
        if (repair.getEmployeeNo() == null || repair.getRepairDate() == null || repair.getRepairType() == null)
        {
            return;
        }
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(repair.getRepairDate());
        HrAttendanceRecord record = hrAttendanceRecordMapper.selectByEmployeeNoAndDate(repair.getEmployeeNo(), dateStr);

        // 查找班次（优先从排班表查，其次默认班次）
        com.ruoyi.hr.domain.HrShift shift = null;
        try
        {
            com.ruoyi.hr.domain.HrSchedule schedule = hrScheduleMapper.selectByEmployeeNoAndDate(repair.getEmployeeNo(), dateStr);
            if (schedule != null && schedule.getShiftId() != null)
            {
                shift = hrShiftMapper.selectHrShiftById(schedule.getShiftId());
            }
        }
        catch (Exception e) { /* 忽略 */ }
        if (shift == null)
        {
            shift = hrShiftMapper.selectDefaultShift();
        }

        // 使用补卡的实际时间（actualClockTime），如果没有则用应打卡时间（expectedClockTime），
        // 如果也没有，则根据班次的上下班时间自动推算
        java.util.Date repairTime = repair.getActualClockTime();
        if (repairTime == null)
        {
            repairTime = repair.getExpectedClockTime();
        }
        if (repairTime == null && shift != null)
        {
            // 根据补卡时段和班次时间自动推算
            String[] types = repair.getRepairType().split(",");
            // 如果只补一个时段，用对应时间；如果全天补，上班用startTime下班用endTime
            for (String type : types)
            {
                java.util.Date inferredTime = inferRepairTime(repair.getRepairDate(), type, shift);
                if (inferredTime != null)
                {
                    if ("0".equals(type))
                    {
                        repairTime = inferredTime; // 临时存放上班卡时间
                    }
                    else if ("1".equals(type) && types.length == 1)
                    {
                        repairTime = inferredTime; // 只补下班卡时用下班时间
                    }
                }
            }
            // 全天补卡时repairTime设为null，后面按type分别赋值
            if (types.length > 1)
            {
                repairTime = null; // 全天补卡时不使用单一时间
            }
        }

        if (record == null)
        {
            // 无考勤记录，新建一条
            record = new HrAttendanceRecord();
            record.setRecordDate(repair.getRepairDate());
            record.setEmployeeId(repair.getEmployeeId());
            record.setEmployeeName(repair.getEmployeeName());
            record.setEmployeeNo(repair.getEmployeeNo());
            record.setDelFlag("0");
            record.setCreateBy(SecurityUtils.getUsername());

            // 尝试匹配班次
            if (shift != null)
            {
                record.setShiftId(shift.getShiftId());
                record.setShiftName(shift.getShiftName());
            }

            // 根据补卡时段设置对应打卡时间（逗号分隔可多选）
            String[] types = repair.getRepairType().split(",");
            StringBuilder remarkParts = new StringBuilder();
            for (String type : types)
            {
                if ("0".equals(type))
                {
                    record.setClockIn(repairTime != null ? repairTime : inferRepairTime(repair.getRepairDate(), "0", shift));
                    record.setClockInSource("1"); // 补卡
                    remarkParts.append("上班卡");
                }
                else if ("1".equals(type))
                {
                    record.setClockOut(repairTime != null ? repairTime : inferRepairTime(repair.getRepairDate(), "1", shift));
                    record.setClockOutSource("1"); // 补卡
                    remarkParts.append(remarkParts.length() > 0 ? "、下班卡" : "下班卡");
                }
            }
            record.setRemark("补卡生成（" + remarkParts.toString() + "）");

            // 计算考勤状态
            hrAttendanceRecordService.computeAttendanceResultPublic(record);
            hrAttendanceRecordMapper.insertHrAttendanceRecord(record);
        }
        else
        {
            // 已有考勤记录，补卡写入对应时段（逗号分隔可多选）
            String[] types = repair.getRepairType().split(",");
            boolean needUpdate = false;
            for (String type : types)
            {
                if ("0".equals(type) && record.getClockIn() == null)
                {
                    record.setClockIn(repairTime != null ? repairTime : inferRepairTime(repair.getRepairDate(), "0", shift));
                    record.setClockInSource("1"); // 补卡
                    needUpdate = true;
                }
                else if ("1".equals(type) && record.getClockOut() == null)
                {
                    record.setClockOut(repairTime != null ? repairTime : inferRepairTime(repair.getRepairDate(), "1", shift));
                    record.setClockOutSource("1"); // 补卡
                    needUpdate = true;
                }
            }
            if (!needUpdate)
            {
                // 对应时段已有打卡记录，不需要覆写
                return;
            }
            // 更新备注，标注补卡信息
            String existRemark = record.getRemark() != null ? record.getRemark() : "";
            StringBuilder remarkParts = new StringBuilder();
            for (String type : types)
            {
                if ("0".equals(type) && "1".equals(record.getClockInSource() != null ? record.getClockInSource() : ""))
                {
                    remarkParts.append(remarkParts.length() > 0 ? "、上班卡" : "上班卡");
                }
                else if ("1".equals(type) && "1".equals(record.getClockOutSource() != null ? record.getClockOutSource() : ""))
                {
                    remarkParts.append(remarkParts.length() > 0 ? "、下班卡" : "下班卡");
                }
            }
            if (remarkParts.length() > 0)
            {
                String repairRemark = "补卡写入（" + remarkParts.toString() + "）";
                record.setRemark(existRemark.isEmpty() ? repairRemark : existRemark + "；" + repairRemark);
            }
            record.setUpdateBy(SecurityUtils.getUsername());
            // 重新计算考勤状态
            hrAttendanceRecordService.computeAttendanceResultPublic(record);
            hrAttendanceRecordMapper.updateHrAttendanceRecord(record);
        }
    }

    /**
     * 根据补卡日期和班次时间推算应打卡时间
     * @param repairDate 补卡日期
     * @param type "0"=上班卡用startTime，"1"=下班卡用endTime
     * @param shift 班次
     * @return 推算的打卡时间
     */
    private java.util.Date inferRepairTime(java.util.Date repairDate, String type, com.ruoyi.hr.domain.HrShift shift)
    {
        if (shift == null || repairDate == null)
        {
            return null;
        }
        String timeStr = "0".equals(type) ? shift.getStartTime() : shift.getEndTime();
        if (timeStr == null || timeStr.isEmpty())
        {
            return null;
        }
        // timeStr 格式如 "08:30" 或 "17:30" 或 "24:00"
        try
        {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(repairDate);
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            // 处理 24:00 的情况
            if (hour >= 24)
            {
                hour = 0;
                // 次日
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(repairDate);
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
                cal.set(java.util.Calendar.MINUTE, minute);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                return cal.getTime();
            }
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(repairDate);
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
            cal.set(java.util.Calendar.MINUTE, minute);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
