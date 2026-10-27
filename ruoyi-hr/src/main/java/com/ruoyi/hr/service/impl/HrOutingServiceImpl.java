package com.ruoyi.hr.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrOuting;
import com.ruoyi.hr.domain.HrOutingAuditLog;
import com.ruoyi.hr.domain.HrAttendanceRecord;
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.domain.HrShift;
import com.ruoyi.hr.mapper.HrOutingMapper;
import com.ruoyi.hr.mapper.HrOutingAuditLogMapper;
import com.ruoyi.hr.mapper.HrAttendanceRecordMapper;
import com.ruoyi.hr.mapper.HrScheduleMapper;
import com.ruoyi.hr.mapper.HrShiftMapper;
import com.ruoyi.hr.service.IHrOutingService;

@Service
public class HrOutingServiceImpl implements IHrOutingService
{
    @Autowired
    private HrOutingMapper hrOutingMapper;
    @Autowired
    private HrOutingAuditLogMapper hrOutingAuditLogMapper;
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
    public HrOuting selectHrOutingById(Long outingId)
    {
        HrOuting outing = hrOutingMapper.selectHrOutingById(outingId);
        if (outing != null)
        {
            outing.setAuditLogList(hrOutingAuditLogMapper.selectHrOutingAuditLogByOutingId(outingId));
        }
        return outing;
    }

    @Override
    @DataScope(deptAlias = "biz_hr_outing")
    public List<HrOuting> selectHrOutingList(HrOuting hrOuting) { return hrOutingMapper.selectHrOutingList(hrOuting); }

    @Override
    public int insertHrOuting(HrOuting hrOuting)
    {
        if (StringUtils.isEmpty(hrOuting.getOutingNo()))
        {
            hrOuting.setOutingNo(mkNumberRuleService.generateNumber("hr_outing"));
        }
        if (StringUtils.isEmpty(hrOuting.getAuditStatus()))
        {
            hrOuting.setAuditStatus("0");
        }
        return hrOutingMapper.insertHrOuting(hrOuting);
    }

    @Override
    public int updateHrOuting(HrOuting hrOuting)
    {
        if ("2".equals(hrOuting.getAuditStatus()))
        {
            hrOuting.setAuditStatus("0");
            hrOuting.setAuditBy(null);
            hrOuting.setAuditTime(null);
            hrOuting.setAuditRemark("");
        }
        return hrOutingMapper.updateHrOuting(hrOuting);
    }

    @Override
    public int deleteHrOutingByIds(Long[] outingIds)
    {
        int rows = hrOutingMapper.deleteHrOutingByIds(outingIds);
        for (Long outingId : outingIds)
        {
            hrOutingAuditLogMapper.deleteHrOutingAuditLogByOutingId(outingId);
        }
        return rows;
    }

    @Override
    public int deleteHrOutingById(Long outingId)
    {
        hrOutingAuditLogMapper.deleteHrOutingAuditLogByOutingId(outingId);
        return hrOutingMapper.deleteHrOutingById(outingId);
    }

    @Override
    @Transactional
    public int auditHrOuting(Long outingId, String auditAction, String auditRemark)
    {
        HrOuting exist = hrOutingMapper.selectHrOutingById(outingId);
        if (exist == null) { throw new ServiceException("外出单不存在"); }
        if (!"1".equals(auditAction) && !"2".equals(auditAction)) { throw new ServiceException("审核动作不合法"); }
        if (!"0".equals(exist.getAuditStatus())) { throw new ServiceException("当前状态不允许审批" + ("2".equals(exist.getAuditStatus()) ? "，请修改后重新提交" : "")); }

        String username = SecurityUtils.getUsername();
        Date auditTime = new Date();

        HrOuting update = new HrOuting();
        update.setOutingId(outingId);
        update.setAuditStatus(auditAction);
        update.setAuditBy(username);
        update.setAuditTime(auditTime);
        update.setAuditRemark(auditRemark);
        int rows = hrOutingMapper.updateHrOuting(update);

        if (rows > 0)
        {
            HrOutingAuditLog log = new HrOutingAuditLog();
            log.setOutingId(outingId);
            log.setAuditAction(auditAction);
            log.setAuditBy(username);
            log.setAuditTime(auditTime);
            log.setAuditRemark(auditRemark);
            hrOutingAuditLogMapper.insertHrOutingAuditLog(log);

            // 审批通过：将外出时间写入考勤记录
            if ("1".equals(auditAction))
            {
                applyOutingToAttendance(exist);
            }
        }
        return rows;
    }

    /**
     * 审批通过后，将外出时间写入考勤记录（如果已有正常考勤记录则不补充）
     * 外出的 startTime/endTime 格式为 HH:mm，需要组合外出日期生成完整 Date
     */
    private void applyOutingToAttendance(HrOuting outing)
    {
        if (outing.getEmployeeNo() == null || outing.getOutingDate() == null
            || StringUtils.isEmpty(outing.getStartTime()) || StringUtils.isEmpty(outing.getEndTime()))
        {
            return;
        }

        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(outing.getOutingDate());
        HrAttendanceRecord record = hrAttendanceRecordMapper.selectByEmployeeNoAndDate(outing.getEmployeeNo(), dateStr);

        // 将 HH:mm 时间组合外出日期生成完整 Date
        Date clockInTime = combineDateTime(outing.getOutingDate(), outing.getStartTime());
        Date clockOutTime = combineDateTime(outing.getOutingDate(), outing.getEndTime());

        if (record == null)
        {
            // 无考勤记录，新建一条
            record = new HrAttendanceRecord();
            record.setRecordDate(outing.getOutingDate());
            record.setEmployeeId(outing.getEmployeeId());
            record.setEmployeeName(outing.getEmployeeName());
            record.setEmployeeNo(outing.getEmployeeNo());
            record.setClockIn(clockInTime);
            record.setClockOut(clockOutTime);
            record.setClockInSource("3"); // 外出审批
            record.setClockOutSource("3"); // 外出审批
            record.setDelFlag("0");
            record.setCreateBy(SecurityUtils.getUsername());
            record.setRemark("外出审批生成");

            // 尝试匹配班次
            HrShift shift = null;
            try
            {
                HrSchedule schedule = hrScheduleMapper.selectByEmployeeNoAndDate(outing.getEmployeeNo(), dateStr);
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
            if (shift != null)
            {
                record.setShiftId(shift.getShiftId());
                record.setShiftName(shift.getShiftName());
            }

            hrAttendanceRecordService.computeAttendanceResultPublic(record);
            hrAttendanceRecordMapper.insertHrAttendanceRecord(record);
        }
        else
        {
            // 已有考勤记录，只补充缺失的打卡时间
            boolean needUpdate = false;
            if (record.getClockIn() == null)
            {
                record.setClockIn(clockInTime);
                record.setClockInSource("3"); // 外出审批
                needUpdate = true;
            }
            if (record.getClockOut() == null)
            {
                record.setClockOut(clockOutTime);
                record.setClockOutSource("3"); // 外出审批
                needUpdate = true;
            }
            if (needUpdate)
            {
                String existRemark = record.getRemark() != null ? record.getRemark() : "";
                String outingRemark = "外出审批补充";
                record.setRemark(existRemark.isEmpty() ? outingRemark : existRemark + "；" + outingRemark);
                record.setUpdateBy(SecurityUtils.getUsername());
                hrAttendanceRecordService.computeAttendanceResultPublic(record);
                hrAttendanceRecordMapper.updateHrAttendanceRecord(record);
            }
        }
    }

    /**
     * 将日期(HH:mm格式)组合外出日期生成完整 Date
     */
    private Date combineDateTime(Date date, String timeStr)
    {
        if (date == null || StringUtils.isEmpty(timeStr))
        {
            return null;
        }
        try
        {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
