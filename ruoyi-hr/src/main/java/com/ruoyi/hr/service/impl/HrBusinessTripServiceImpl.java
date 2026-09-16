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
import com.ruoyi.hr.domain.HrBusinessTrip;
import com.ruoyi.hr.domain.HrBusinessTripAuditLog;
import com.ruoyi.hr.domain.HrAttendanceRecord;
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.domain.HrShift;
import com.ruoyi.hr.mapper.HrBusinessTripMapper;
import com.ruoyi.hr.mapper.HrBusinessTripAuditLogMapper;
import com.ruoyi.hr.mapper.HrAttendanceRecordMapper;
import com.ruoyi.hr.mapper.HrScheduleMapper;
import com.ruoyi.hr.mapper.HrShiftMapper;
import com.ruoyi.hr.service.IHrBusinessTripService;

@Service
public class HrBusinessTripServiceImpl implements IHrBusinessTripService
{
    @Autowired
    private HrBusinessTripMapper hrBusinessTripMapper;
    @Autowired
    private HrBusinessTripAuditLogMapper hrBusinessTripAuditLogMapper;
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
    public HrBusinessTrip selectHrBusinessTripById(Long tripId)
    {
        HrBusinessTrip trip = hrBusinessTripMapper.selectHrBusinessTripById(tripId);
        if (trip != null)
        {
            trip.setAuditLogList(hrBusinessTripAuditLogMapper.selectHrBusinessTripAuditLogByTripId(tripId));
        }
        return trip;
    }

    @Override
    @DataScope(deptAlias = "biz_hr_business_trip")
    public List<HrBusinessTrip> selectHrBusinessTripList(HrBusinessTrip hrBusinessTrip) { return hrBusinessTripMapper.selectHrBusinessTripList(hrBusinessTrip); }

    @Override
    public int insertHrBusinessTrip(HrBusinessTrip hrBusinessTrip)
    {
        if (StringUtils.isEmpty(hrBusinessTrip.getTripNo()))
        {
            hrBusinessTrip.setTripNo(mkNumberRuleService.generateNumber("hr_business_trip"));
        }
        computeTripDays(hrBusinessTrip);
        if (StringUtils.isEmpty(hrBusinessTrip.getAuditStatus()))
        {
            hrBusinessTrip.setAuditStatus("0");
        }
        return hrBusinessTripMapper.insertHrBusinessTrip(hrBusinessTrip);
    }

    @Override
    public int updateHrBusinessTrip(HrBusinessTrip hrBusinessTrip)
    {
        if ("2".equals(hrBusinessTrip.getAuditStatus()))
        {
            hrBusinessTrip.setAuditStatus("0");
            hrBusinessTrip.setAuditBy(null);
            hrBusinessTrip.setAuditTime(null);
            hrBusinessTrip.setAuditRemark("");
        }
        computeTripDays(hrBusinessTrip);
        return hrBusinessTripMapper.updateHrBusinessTrip(hrBusinessTrip);
    }

    /** 根据出发/预计返回日期自动计算出差天数（含首尾） */
    private void computeTripDays(HrBusinessTrip trip)
    {
        if (trip.getDepartureDate() == null || trip.getExpectedReturnDate() == null
                || trip.getExpectedReturnDate().before(trip.getDepartureDate()))
        {
            return;
        }
        long millis = trip.getExpectedReturnDate().getTime() - trip.getDepartureDate().getTime();
        long days = millis / 86400000L;
        if (millis % 86400000L != 0)
        {
            days++;
        }
        trip.setTripDays(java.math.BigDecimal.valueOf(days));
    }

    @Override
    public int deleteHrBusinessTripByIds(Long[] tripIds)
    {
        int rows = hrBusinessTripMapper.deleteHrBusinessTripByIds(tripIds);
        for (Long tripId : tripIds)
        {
            hrBusinessTripAuditLogMapper.deleteHrBusinessTripAuditLogByTripId(tripId);
        }
        return rows;
    }

    @Override
    public int deleteHrBusinessTripById(Long tripId)
    {
        hrBusinessTripAuditLogMapper.deleteHrBusinessTripAuditLogByTripId(tripId);
        return hrBusinessTripMapper.deleteHrBusinessTripById(tripId);
    }

    @Override
    @Transactional
    public int auditHrBusinessTrip(Long tripId, String auditAction, String auditRemark)
    {
        HrBusinessTrip exist = hrBusinessTripMapper.selectHrBusinessTripById(tripId);
        if (exist == null) { throw new ServiceException("出差单不存在"); }
        if (!"1".equals(auditAction) && !"2".equals(auditAction)) { throw new ServiceException("审核动作不合法"); }
        if (!"0".equals(exist.getAuditStatus())) { throw new ServiceException("当前状态不允许审批" + ("2".equals(exist.getAuditStatus()) ? "，请修改后重新提交" : "")); }

        String username = SecurityUtils.getUsername();
        Date auditTime = new Date();

        HrBusinessTrip update = new HrBusinessTrip();
        update.setTripId(tripId);
        update.setAuditStatus(auditAction);
        update.setAuditBy(username);
        update.setAuditTime(auditTime);
        update.setAuditRemark(auditRemark);
        int rows = hrBusinessTripMapper.updateHrBusinessTrip(update);

        if (rows > 0)
        {
            HrBusinessTripAuditLog log = new HrBusinessTripAuditLog();
            log.setTripId(tripId);
            log.setAuditAction(auditAction);
            log.setAuditBy(username);
            log.setAuditTime(auditTime);
            log.setAuditRemark(auditRemark);
            hrBusinessTripAuditLogMapper.insertHrBusinessTripAuditLog(log);

            // 审批通过：对出差日期范围内的每天写入考勤记录
            if ("1".equals(auditAction))
            {
                applyTripToAttendance(exist);
            }
        }
        return rows;
    }

    /**
     * 审批通过后，对出差日期范围内的每个工作日写入考勤记录
     * 如果已有完整考勤记录则不补充，只补充缺失的打卡时间
     */
    private void applyTripToAttendance(HrBusinessTrip trip)
    {
        if (trip.getEmployeeNo() == null || trip.getDepartureDate() == null)
        {
            return;
        }
        Date endDate = trip.getActualReturnDate() != null ? trip.getActualReturnDate() : trip.getExpectedReturnDate();
        if (endDate == null || endDate.before(trip.getDepartureDate()))
        {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String username = SecurityUtils.getUsername();

        Calendar cal = Calendar.getInstance();
        cal.setTime(trip.getDepartureDate());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        while (!cal.after(endCal))
        {
            Date currentDate = cal.getTime();
            String dateStr = sdf.format(currentDate);

            // 查找班次
            HrShift shift = null;
            try
            {
                HrSchedule schedule = hrScheduleMapper.selectByEmployeeNoAndDate(trip.getEmployeeNo(), dateStr);
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

            // 用班次上下班时间作为打卡时间
            Date clockInTime = shift != null ? combineDateTime(currentDate, shift.getStartTime()) : null;
            Date clockOutTime = shift != null ? combineDateTime(currentDate, shift.getEndTime()) : null;

            HrAttendanceRecord record = hrAttendanceRecordMapper.selectByEmployeeNoAndDate(trip.getEmployeeNo(), dateStr);

            if (record == null)
            {
                // 无考勤记录，新建一条
                record = new HrAttendanceRecord();
                record.setRecordDate(currentDate);
                record.setEmployeeId(trip.getEmployeeId());
                record.setEmployeeName(trip.getEmployeeName());
                record.setEmployeeNo(trip.getEmployeeNo());
                record.setClockIn(clockInTime);
                record.setClockOut(clockOutTime);
                record.setClockInSource("4"); // 出差审批
                record.setClockOutSource("4"); // 出差审批
                record.setDelFlag("0");
                record.setCreateBy(username);
                record.setRemark("出差审批生成");
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
                if (record.getClockIn() == null && clockInTime != null)
                {
                    record.setClockIn(clockInTime);
                    record.setClockInSource("4"); // 出差审批
                    needUpdate = true;
                }
                if (record.getClockOut() == null && clockOutTime != null)
                {
                    record.setClockOut(clockOutTime);
                    record.setClockOutSource("4"); // 出差审批
                    needUpdate = true;
                }
                if (needUpdate)
                {
                    String existRemark = record.getRemark() != null ? record.getRemark() : "";
                    String tripRemark = "出差审批补充";
                    record.setRemark(existRemark.isEmpty() ? tripRemark : existRemark + "；" + tripRemark);
                    record.setUpdateBy(username);
                    hrAttendanceRecordService.computeAttendanceResultPublic(record);
                    hrAttendanceRecordMapper.updateHrAttendanceRecord(record);
                }
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    /**
     * 将日期 + HH:mm 时间字符串组合成完整 Date
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
