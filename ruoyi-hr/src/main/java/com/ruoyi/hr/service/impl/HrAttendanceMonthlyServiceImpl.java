package com.ruoyi.hr.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrAttendanceMonthly;
import com.ruoyi.hr.domain.HrAttendanceRecord;
import com.ruoyi.hr.domain.HrBusinessTrip;
import com.ruoyi.hr.domain.HrClockRepair;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.domain.HrLeaveRequest;
import com.ruoyi.hr.domain.HrOuting;
import com.ruoyi.hr.domain.HrOvertime;
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.domain.HrWorkdayCalendar;
import com.ruoyi.hr.mapper.HrAttendanceMonthlyMapper;
import com.ruoyi.hr.mapper.HrAttendanceRecordMapper;
import com.ruoyi.hr.mapper.HrBusinessTripMapper;
import com.ruoyi.hr.mapper.HrClockRepairMapper;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.mapper.HrLeaveRequestMapper;
import com.ruoyi.hr.mapper.HrOutingMapper;
import com.ruoyi.hr.mapper.HrOvertimeMapper;
import com.ruoyi.hr.mapper.HrScheduleMapper;
import com.ruoyi.hr.mapper.HrWorkdayCalendarMapper;
import com.ruoyi.hr.service.IHrAttendanceMonthlyService;
import java.util.HashSet;
import java.util.Set;

/**
 * 考勤月报服务
 *
 * 月报由系统按"日考勤打卡记录 + 当月已审批请假单"自动汇总生成，
 * HR 手工新增仅作为无打卡数据场景的兜底；已封账月报禁止再生成/修改。
 */
@Service
public class HrAttendanceMonthlyServiceImpl implements IHrAttendanceMonthlyService
{
    /** 考勤状态：0=正常 1=迟到 2=早退 4=漏卡（旷工不在考勤记录中产生，由月报统计计算） */
    private static final String ST_NORMAL = "0";
    private static final String ST_LATE = "1";
    private static final String ST_EARLY = "2";
    private static final String ST_MISS = "4";
    private static final String ST_ABSENT = "3";
    private static final String ST_LEAVE = "5";
    private static final String ST_TRIP = "6";
    // 旷工不再作为考勤记录状态，月报层面计算

    @Autowired
    private HrAttendanceMonthlyMapper hrAttendanceMonthlyMapper;

    @Autowired
    private HrAttendanceRecordMapper hrAttendanceRecordMapper;
    @Autowired
    private HrClockRepairMapper hrClockRepairMapper;

    @Autowired
    private HrLeaveRequestMapper hrLeaveRequestMapper;

    @Autowired
    private HrOutingMapper hrOutingMapper;

    @Autowired
    private HrOvertimeMapper hrOvertimeMapper;

    @Autowired
    private HrBusinessTripMapper hrBusinessTripMapper;

    @Autowired
    private HrScheduleMapper hrScheduleMapper;

    @Autowired
    private HrWorkdayCalendarMapper hrWorkdayCalendarMapper;

    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;

    @Override
    public HrAttendanceMonthly selectHrAttendanceMonthlyById(Long monthlyId) { return hrAttendanceMonthlyMapper.selectHrAttendanceMonthlyById(monthlyId); }

    @Override
    public List<HrAttendanceMonthly> selectHrAttendanceMonthlyList(HrAttendanceMonthly hrAttendanceMonthly) { return hrAttendanceMonthlyMapper.selectHrAttendanceMonthlyList(hrAttendanceMonthly); }

    @Override
    public int insertHrAttendanceMonthly(HrAttendanceMonthly hrAttendanceMonthly)
    {
        return hrAttendanceMonthlyMapper.insertHrAttendanceMonthly(hrAttendanceMonthly);
    }

    @Override
    public int updateHrAttendanceMonthly(HrAttendanceMonthly hrAttendanceMonthly)
    {
        HrAttendanceMonthly db = hrAttendanceMonthlyMapper.selectHrAttendanceMonthlyById(hrAttendanceMonthly.getMonthlyId());
        if (db == null)
        {
            throw new ServiceException("考勤月报不存在");
        }
        if ("1".equals(db.getSealedStatus()))
        {
            throw new ServiceException("考勤月报[" + db.getYearMonth() + " " + db.getEmployeeName() + "]已封账，禁止修改");
        }
        return hrAttendanceMonthlyMapper.updateHrAttendanceMonthly(hrAttendanceMonthly);
    }

    @Override
    public int deleteHrAttendanceMonthlyByIds(Long[] monthlyIds)
    {
        if (monthlyIds != null)
        {
            for (Long id : monthlyIds)
            {
                HrAttendanceMonthly db = hrAttendanceMonthlyMapper.selectHrAttendanceMonthlyById(id);
                if (db != null && "1".equals(db.getSealedStatus()))
                {
                    throw new ServiceException("考勤月报[" + db.getYearMonth() + " " + db.getEmployeeName() + "]已封账，禁止删除");
                }
            }
        }
        return hrAttendanceMonthlyMapper.deleteHrAttendanceMonthlyByIds(monthlyIds);
    }

    @Override
    public int deleteHrAttendanceMonthlyById(Long monthlyId)
    {
        HrAttendanceMonthly db = hrAttendanceMonthlyMapper.selectHrAttendanceMonthlyById(monthlyId);
        if (db != null && "1".equals(db.getSealedStatus()))
        {
            throw new ServiceException("考勤月报已封账，禁止删除");
        }
        return hrAttendanceMonthlyMapper.deleteHrAttendanceMonthlyById(monthlyId);
    }

    /**
     * 按年月自动汇总生成月报（覆盖未封账的旧月报数据）
     *
     * 数据来源：
     * 1. biz_hr_attendance_record 日打卡记录：出勤/迟到/早退/旷工/加班/夜班/高温天数（高温按班次含"夜/高温"关键字简化判断，仅统计有出勤记录的天数）
     * 2. biz_hr_leave_request 当月已审批请假单：请假天数合计
     * 3. biz_hr_overtime 当月已审批加班单：加班小时合计（取打卡加班与审批加班的较大值，避免重复）
     * 4. biz_hr_outing 当月已审批外出单：外出天数合计
     * 5. biz_hr_business_trip 当月已审批出差单：出差天数合计
     * 6. 旷工 = 应出勤天数 - 有考勤记录的天数 - 请假天数 - 出差天数 - 外出天数（在所有记录汇总后计算）
     */
    @Override
    @Transactional
    public Map<String, Object> generate(String yearMonth, String operator)
    {
        if (StringUtils.isEmpty(yearMonth) || !yearMonth.matches("\\d{4}-\\d{2}"))
        {
            throw new ServiceException("年月格式不正确，应为 yyyy-MM");
        }

        // ---- 1. 当月日考勤记录 ----
        HrAttendanceRecord recQuery = new HrAttendanceRecord();
        recQuery.getParams().put("beginRecordDate", yearMonth + "-01");
        recQuery.getParams().put("endRecordDate", monthLastDay(yearMonth));
        List<HrAttendanceRecord> records = hrAttendanceRecordMapper.selectHrAttendanceRecordList(recQuery);

        // ---- 2. 当月已审批请假单 ----
        HrLeaveRequest leaveQuery = new HrLeaveRequest();
        leaveQuery.setAuditStatus("1");
        leaveQuery.getParams().put("beginStartTime", yearMonth + "-01");
        leaveQuery.getParams().put("endStartTime", monthLastDay(yearMonth));
        List<HrLeaveRequest> leaves = hrLeaveRequestMapper.selectHrLeaveRequestList(leaveQuery);
        Map<Long, BigDecimal> leaveDaysByEmp = new HashMap<>();
        for (HrLeaveRequest lr : leaves)
        {
            if (lr.getEmployeeId() != null)
            {
                leaveDaysByEmp.merge(lr.getEmployeeId(), lr.getLeaveDays() == null ? BigDecimal.ZERO : lr.getLeaveDays(), BigDecimal::add);
            }
        }

        // ---- 3. 当月已审批外出单/出差单 ----
        HrOuting outingQuery = new HrOuting();
        outingQuery.setAuditStatus("1");
        outingQuery.getParams().put("beginOutingDate", yearMonth + "-01");
        outingQuery.getParams().put("endOutingDate", monthLastDay(yearMonth));
        List<HrOuting> outings = hrOutingMapper.selectHrOutingList(outingQuery);
        Map<Long, BigDecimal> outingDaysByEmp = new HashMap<>();
        for (HrOuting o : outings)
        {
            if (o.getEmployeeId() != null)
            {
                BigDecimal days = calcSpanDays(o.getOutingDate(), o.getOutingDate(), yearMonth);
                outingDaysByEmp.merge(o.getEmployeeId(), days, BigDecimal::add);
            }
        }
        HrBusinessTrip tripQuery = new HrBusinessTrip();
        tripQuery.setAuditStatus("1");
        tripQuery.getParams().put("beginDepartureDate", yearMonth + "-01");
        tripQuery.getParams().put("endDepartureDate", monthLastDay(yearMonth));
        List<HrBusinessTrip> trips = hrBusinessTripMapper.selectHrBusinessTripList(tripQuery);
        Map<Long, BigDecimal> tripDaysByEmp = new HashMap<>();
        for (HrBusinessTrip t : trips)
        {
            if (t.getEmployeeId() != null)
            {
                BigDecimal days = calcSpanDays(t.getDepartureDate(), t.getActualReturnDate() != null ? t.getActualReturnDate() : t.getExpectedReturnDate(), yearMonth);
                tripDaysByEmp.merge(t.getEmployeeId(), days, BigDecimal::add);
            }
        }

        // ---- 3.5 当月已审批加班单 ----
        HrOvertime overtimeQuery = new HrOvertime();
        overtimeQuery.setAuditStatus("1");
        overtimeQuery.getParams().put("beginOvertimeDate", yearMonth + "-01");
        overtimeQuery.getParams().put("endOvertimeDate", monthLastDay(yearMonth));
        List<HrOvertime> overtimes = hrOvertimeMapper.selectHrOvertimeList(overtimeQuery);
        Map<Long, BigDecimal> overtimeHoursByEmp = new HashMap<>();
        for (HrOvertime ot : overtimes)
        {
            if (ot.getEmployeeId() != null && ot.getOvertimeHours() != null)
            {
                overtimeHoursByEmp.merge(ot.getEmployeeId(), ot.getOvertimeHours(), BigDecimal::add);
            }
        }

        // ---- 4. 当月已审批补卡记录 ----
        HrClockRepair repairQuery = new HrClockRepair();
        repairQuery.setAuditStatus("1");
        repairQuery.getParams().put("beginRepairDate", yearMonth + "-01");
        repairQuery.getParams().put("endRepairDate", nextMonthFirstDay(yearMonth));
        List<HrClockRepair> repairs = hrClockRepairMapper.selectHrClockRepairList(repairQuery);
        Map<Long, Set<String>> repairDatesByEmp = new HashMap<>();
        for (HrClockRepair r : repairs)
        {
            if (r.getEmployeeId() != null && r.getRepairDate() != null)
            {
                String dStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(r.getRepairDate());
                repairDatesByEmp.computeIfAbsent(r.getEmployeeId(), k -> new HashSet<>()).add(dStr);
            }
        }

        // ---- 5. 应出勤【日期集合】 ----
        Map<Long, Set<java.time.LocalDate>> requiredDaySets = calcRequiredDaySets(yearMonth);
        // ---- 6. 按人汇总（仅统计应出勤日内的打卡） ----
        Map<Long, HrAttendanceMonthly> monthlyMap = new LinkedHashMap<>();
        for (HrAttendanceRecord rec : records)
        {
            if (rec.getEmployeeId() == null)
            {
                continue;
            }
            Set<java.time.LocalDate> reqDaySet = requiredDaySets.get(rec.getEmployeeId());
            java.time.LocalDate recDay = rec.getRecordDate() == null ? null
                    : rec.getRecordDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            // 请假日已有打卡时以请假为准，避免出勤与请假重复计算
            if (leaveDaysByEmp.containsKey(rec.getEmployeeId()) && recDay != null)
            {
                // 该员工有请假单：检查打卡日期是否落在某张请假单区间内
                boolean onLeave = false;
                for (HrLeaveRequest lr : leaves)
                {
                    if (lr.getEmployeeId().equals(rec.getEmployeeId()) && lr.getStartTime() != null)
                    {
                        java.time.LocalDate b = lr.getStartTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        java.util.Date ed = lr.getEndTime() != null ? lr.getEndTime() : lr.getStartTime();
                        java.time.LocalDate e = ed.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        if (!recDay.isBefore(b) && !recDay.isAfter(e)) { onLeave = true; break; }
                    }
                }
                if (onLeave) { continue; }
            }
            // 非应出勤日（周末/节假日）打卡不计出勤/旷工/迟到/早退，仅累计加班
            if (reqDaySet != null && (recDay == null || !reqDaySet.contains(recDay)))
            {
                if (rec.getOvertimeHours() != null)
                {
                    HrAttendanceMonthly om = monthlyMap.computeIfAbsent(rec.getEmployeeId(), k2 -> newMonthlyRow(yearMonth, rec.getEmployeeId(), operator));
                    om.setOvertimeHours(nvl(om.getOvertimeHours()).add(rec.getOvertimeHours()));
                }
                continue;
            }
            HrAttendanceMonthly m = monthlyMap.computeIfAbsent(rec.getEmployeeId(), k -> newMonthlyRow(yearMonth, rec.getEmployeeId(), operator));
            if (m.getDeptId() == null && rec.getOrgId() != null)
            {
                m.setDeptId(rec.getOrgId());
                m.setDeptName(rec.getOrgName());
            }
            String st = rec.getAttendanceStatus();
            if (st == null)
            {
                st = (rec.getClockIn() != null && rec.getClockOut() != null) ? ST_NORMAL : ST_MISS;
            }
            switch (st)
            {
                case ST_NORMAL:
                    m.setAttendDays(nvl(m.getAttendDays()).add(BigDecimal.ONE));
                    break;
                case ST_LATE:
                    m.setAttendDays(nvl(m.getAttendDays()).add(BigDecimal.ONE));
                    m.setLateCount(nvlInt(m.getLateCount()) + 1);
                    break;
                case ST_EARLY:
                    m.setAttendDays(nvl(m.getAttendDays()).add(BigDecimal.ONE));
                    m.setEarlyCount(nvlInt(m.getEarlyCount()) + 1);
                    break;
                case ST_ABSENT:
                    m.setAbsentDays(nvl(m.getAbsentDays()).add(BigDecimal.ONE));
                    break;
                case ST_LEAVE:
                    m.setLeaveDays(nvl(m.getLeaveDays()).add(BigDecimal.ONE));
                    break;
                case ST_TRIP:
                    m.setAttendDays(nvl(m.getAttendDays()).add(BigDecimal.ONE));
                    break;
                case ST_MISS:
                    String recDateStr = rec.getRecordDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(rec.getRecordDate()) : "";
                    Set<String> repairDates = repairDatesByEmp.get(rec.getEmployeeId());
                    if (repairDates != null && repairDates.contains(recDateStr))
                    {
                        m.setAttendDays(nvl(m.getAttendDays()).add(BigDecimal.ONE));
                    }
                    else
                    {
                        m.setAttendDays(nvl(m.getAttendDays()).add(new BigDecimal("0.5")));
                        m.setAbsentDays(nvl(m.getAbsentDays()).add(new BigDecimal("0.5")));
                    }
                    break;
                default:
                    break;
            }
            if (rec.getOvertimeHours() != null)
            {
                m.setOvertimeHours(nvl(m.getOvertimeHours()).add(rec.getOvertimeHours()));
            }
            if (rec.getNightShift() != null && rec.getNightShift() == 1)
            {
                m.setNightShiftCount(nvlInt(m.getNightShiftCount()) + 1);
            }
        }
        // ---- 7. 请假/外出/出差合并（无打卡员工也建行） ----
        for (Map.Entry<Long, BigDecimal> e : leaveDaysByEmp.entrySet())
        {
            HrAttendanceMonthly m = monthlyMap.computeIfAbsent(e.getKey(), k -> newMonthlyRow(yearMonth, k, operator));
            m.setLeaveDays(e.getValue().max(nvl(m.getLeaveDays())));
        }
        for (Map.Entry<Long, BigDecimal> e : outingDaysByEmp.entrySet())
        {
            HrAttendanceMonthly m = monthlyMap.computeIfAbsent(e.getKey(), k -> newMonthlyRow(yearMonth, k, operator));
            m.setOutingDays(e.getValue());
        }
        for (Map.Entry<Long, BigDecimal> e : tripDaysByEmp.entrySet())
        {
            HrAttendanceMonthly m = monthlyMap.computeIfAbsent(e.getKey(), k -> newMonthlyRow(yearMonth, k, operator));
            m.setTripDays(e.getValue());
        }
        // 加班审批单合并：取打卡加班与审批加班的较大值，避免重复累加
        for (Map.Entry<Long, BigDecimal> e : overtimeHoursByEmp.entrySet())
        {
            HrAttendanceMonthly m = monthlyMap.computeIfAbsent(e.getKey(), k -> newMonthlyRow(yearMonth, k, operator));
            m.setOvertimeHours(nvl(m.getOvertimeHours()).max(e.getValue()));
        }

        // ---- 8. 应出勤天数 + 无打卡员工建行 ----
        for (Map.Entry<Long, Set<java.time.LocalDate>> e : requiredDaySets.entrySet())
        {
            HrAttendanceMonthly m = monthlyMap.computeIfAbsent(e.getKey(), k -> newMonthlyRow(yearMonth, k, operator));
            m.setRequiredDays(new BigDecimal(e.getValue().size()));
        }

        // ---- 9. 旷工倒算 + 实际工作天数 ----
        for (HrAttendanceMonthly m : monthlyMap.values())
        {
            BigDecimal required = nvl(m.getRequiredDays());
            BigDecimal accounted = nvl(m.getAttendDays()).add(nvl(m.getLeaveDays())).add(nvl(m.getTripDays())).add(nvl(m.getOutingDays()));
            BigDecimal computedAbsent = required.subtract(accounted).max(BigDecimal.ZERO);
            m.setAbsentDays(nvl(m.getAbsentDays()).max(computedAbsent).setScale(1, RoundingMode.HALF_UP));
            // 实际出勤天数 = 打卡出勤 + 出差 + 外出（出差/外出视同出勤）
            m.setAttendDays(nvl(m.getAttendDays()).add(nvl(m.getTripDays())).add(nvl(m.getOutingDays())).setScale(1, RoundingMode.HALF_UP));
            m.setActualDays(nvl(m.getAttendDays()).setScale(1, RoundingMode.HALF_UP));
        }

        // ---- 10. 落库 ----
        HrAttendanceMonthly delQuery = new HrAttendanceMonthly();
        delQuery.setYearMonth(yearMonth);
        delQuery.setSealedStatus("0");
        for (HrAttendanceMonthly old : hrAttendanceMonthlyMapper.selectHrAttendanceMonthlyList(delQuery))
        {
            hrAttendanceMonthlyMapper.deleteHrAttendanceMonthlyById(old.getMonthlyId());
        }
        int count = 0;
        for (HrAttendanceMonthly m : monthlyMap.values())
        {
            hrAttendanceMonthlyMapper.insertHrAttendanceMonthly(m);
            count++;
        }

        Map<String, Object> ret = new HashMap<>();
        ret.put("total", count);
        ret.put("leaveCount", leaves.size());
        ret.put("overtimeCount", overtimes.size());
        ret.put("outingCount", outings.size());
        ret.put("tripCount", trips.size());
        ret.put("recordCount", records.size());
        return ret;
    }
    /** 下钻明细 */
    @Override
    public Map<String, Object> detail(Long employeeId, String yearMonth)
    {
        Map<String, Object> ret = new HashMap<>();
        HrAttendanceRecord recQuery = new HrAttendanceRecord();
        recQuery.setEmployeeId(employeeId);
        recQuery.getParams().put("beginRecordDate", yearMonth + "-01");
        recQuery.getParams().put("endRecordDate", monthLastDay(yearMonth));
        List<HrAttendanceRecord> records = hrAttendanceRecordMapper.selectHrAttendanceRecordList(recQuery);
        ret.put("records", records);

        HrLeaveRequest leaveQuery = new HrLeaveRequest();
        leaveQuery.setEmployeeId(employeeId);
        leaveQuery.setAuditStatus("1");
        leaveQuery.getParams().put("beginStartTime", yearMonth + "-01");
        leaveQuery.getParams().put("endStartTime", monthLastDay(yearMonth));
        ret.put("leaves", hrLeaveRequestMapper.selectHrLeaveRequestList(leaveQuery));

        HrOuting outQuery = new HrOuting();
        outQuery.setEmployeeId(employeeId);
        outQuery.setAuditStatus("1");
        outQuery.getParams().put("beginOutingDate", yearMonth + "-01");
        outQuery.getParams().put("endOutingDate", monthLastDay(yearMonth));
        ret.put("outings", hrOutingMapper.selectHrOutingList(outQuery));

        HrBusinessTrip tripQuery = new HrBusinessTrip();
        tripQuery.setEmployeeId(employeeId);
        tripQuery.setAuditStatus("1");
        tripQuery.getParams().put("beginDepartureDate", yearMonth + "-01");
        tripQuery.getParams().put("endDepartureDate", monthLastDay(yearMonth));
        ret.put("trips", hrBusinessTripMapper.selectHrBusinessTripList(tripQuery));

        HrOvertime overtimeQuery = new HrOvertime();
        overtimeQuery.setEmployeeId(employeeId);
        overtimeQuery.setAuditStatus("1");
        overtimeQuery.getParams().put("beginOvertimeDate", yearMonth + "-01");
        overtimeQuery.getParams().put("endOvertimeDate", monthLastDay(yearMonth));
        ret.put("overtimes", hrOvertimeMapper.selectHrOvertimeList(overtimeQuery));

        // ---- 旷工明细：应出勤日内无打卡=全天旷工；漏卡=半天旷工（用虚拟考勤记录承载，状态=3旷工，复用前端明细表） ----
        Map<java.time.LocalDate, String> scheduleShiftNames = getScheduleShiftNames(employeeId, yearMonth);
        Set<java.time.LocalDate> reqDays = calcRequiredDaySets(yearMonth).get(employeeId);
        if (reqDays != null)
        {
            Set<String> punchedFull = new HashSet<>();   // 有完整打卡的日期
            Set<String> punchedHalf = new HashSet<>();   // 漏卡（半天出勤+半天旷工）的日期
            Set<String> repaired = new HashSet<>();      // 补卡审批通过日期
            // 请假/外出/出差覆盖的日期不算旷工（与月报统计的扣减口径一致）
            Set<String> approvedDays = new HashSet<>();
            java.time.LocalDate mBegin0 = java.time.LocalDate.parse(yearMonth + "-01");
            java.time.LocalDate mEnd0 = mBegin0.plusMonths(1);
            java.time.LocalDate firstOfMonth = mBegin0;
            java.time.LocalDate lastOfMonth = mEnd0.minusDays(1);
            for (HrLeaveRequest lr : hrLeaveRequestMapper.selectHrLeaveRequestList(leaveQuery))
            {
                if (lr.getStartTime() == null) { continue; }
                java.time.LocalDate b = lr.getStartTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                java.time.LocalDate e = (lr.getEndTime() == null ? lr.getStartTime() : lr.getEndTime()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                for (java.time.LocalDate d = b.isBefore(firstOfMonth) ? firstOfMonth : b; !d.isAfter(lastOfMonth) && !d.isAfter(e); d = d.plusDays(1))
                {
                    approvedDays.add(d.toString());
                }
            }
            for (HrOuting o : hrOutingMapper.selectHrOutingList(outQuery))
            {
                if (o.getOutingDate() != null) { approvedDays.add(o.getOutingDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()); }
            }
            for (HrBusinessTrip t : hrBusinessTripMapper.selectHrBusinessTripList(tripQuery))
            {
                java.time.LocalDate b = t.getDepartureDate() == null ? null : t.getDepartureDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                java.util.Date endDate = t.getActualReturnDate() != null ? t.getActualReturnDate() : t.getExpectedReturnDate();
                java.util.Date effEnd = endDate != null ? endDate : t.getDepartureDate();
                java.time.LocalDate e = effEnd.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (b == null) { continue; }
                for (java.time.LocalDate d = b.isBefore(firstOfMonth) ? firstOfMonth : b; !d.isAfter(lastOfMonth) && !d.isAfter(e); d = d.plusDays(1))
                {
                    approvedDays.add(d.toString());
                }
            }
            Map<String, HrAttendanceRecord> recByDate = new HashMap<>();
            for (HrAttendanceRecord r : records)
            {
                if (r.getRecordDate() == null) { continue; }
                String dStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(r.getRecordDate());
                recByDate.put(dStr, r);
                if ("4".equals(r.getAttendanceStatus())) { punchedHalf.add(dStr); }
                else { punchedFull.add(dStr); }
            }
            HrClockRepair repairQuery = new HrClockRepair();
            repairQuery.setEmployeeId(employeeId);
            repairQuery.setAuditStatus("1");
            List<HrClockRepair> repairs = hrClockRepairMapper.selectHrClockRepairList(repairQuery);
            for (HrClockRepair r : repairs)
            {
                if (r.getRepairDate() != null)
                {
                    repaired.add(new java.text.SimpleDateFormat("yyyy-MM-dd").format(r.getRepairDate()));
                }
            }
            List<HrAttendanceRecord> absentDetails = new ArrayList<>();
            java.time.LocalDate mBegin = java.time.LocalDate.parse(yearMonth + "-01");
            java.time.LocalDate mEnd = mBegin.plusMonths(1);
            for (java.time.LocalDate d = mBegin; d.isBefore(mEnd); d = d.plusDays(1))
            {
                if (!reqDays.contains(d)) { continue; }
                String dStr = d.toString();
                if (punchedFull.contains(dStr) || repaired.contains(dStr) || approvedDays.contains(dStr)) { continue; }
                HrAttendanceRecord v = new HrAttendanceRecord();
                v.setEmployeeId(employeeId);
                v.setRecordDate(java.sql.Date.valueOf(d));
                v.setAttendanceStatus("3");
                v.setShiftName(scheduleShiftNames.get(d));
                if (punchedHalf.contains(dStr))
                {
                    // 漏卡缺半天：带出当天真实打卡时间，前端明细表可直接显示打过的半天
                    HrAttendanceRecord real = recByDate.get(dStr);
                    if (real != null)
                    {
                        v.setClockIn(real.getClockIn());
                        v.setClockOut(real.getClockOut());
                        v.setClockInSource(real.getClockInSource());
                        v.setClockOutSource(real.getClockOutSource());
                        v.setLateMinutes(real.getLateMinutes());
                        v.setEarlyMinutes(real.getEarlyMinutes());
                    }
                    v.setRemark("漏卡缺半天（已计半天出勤）");
                }
                else
                {
                    v.setRemark("全天无打卡且无请假/出差/外出审批");
                }
                absentDetails.add(v);
            }
            ret.put("absentRecords", absentDetails);



            }

        return ret;
    }

    /** 为仅有审批单/排班（无打卡记录）的员工创建月报行：回填员工信息，数值字段默认 0 */
    private HrAttendanceMonthly newMonthlyRow(String yearMonth, Long employeeId, String operator)
    {
        HrAttendanceMonthly nm = new HrAttendanceMonthly();
        nm.setYearMonth(yearMonth);
        nm.setEmployeeId(employeeId);
        // 回填员工姓名/工号/部门，避免月报出现空姓名行
        HrEmployee emp = hrEmployeeMapper.selectHrEmployeeById(employeeId);
        if (emp != null)
        {
            nm.setEmployeeName(emp.getEmployeeName());
            nm.setEmployeeNo(emp.getEmployeeNo());
            nm.setOrgId(emp.getOrgId());
            nm.setDeptId(emp.getOrgId());
            nm.setDeptName(emp.getOrgName());
        }
        // 数值字段默认 0，而不是 NULL
        BigDecimal zero = BigDecimal.ZERO;
        nm.setAttendDays(zero);
        nm.setAbsentDays(zero);
        nm.setLeaveDays(zero);
        nm.setOutingDays(zero);
        nm.setTripDays(zero);
        nm.setOvertimeHours(zero);
        nm.setRequiredDays(zero);
        nm.setLateCount(0);
        nm.setEarlyCount(0);
        nm.setNightShiftCount(0);
        nm.setHighTempDays(zero);
        nm.setSealedStatus("0");
        nm.setCreateBy(operator);
        return nm;
    }

    /** 获取某员工当月排班的【日期→班次名】映射（用于旷工虚拟记录回填班次）
     *  无排班时按日历工作日兜底，班次名显示"默认班次" */
    private Map<java.time.LocalDate, String> getScheduleShiftNames(Long employeeId, String yearMonth)
    {
        Map<java.time.LocalDate, String> ret = new HashMap<>();
        HrSchedule schedQuery = new HrSchedule();
        schedQuery.setEmployeeId(employeeId);
        schedQuery.getParams().put("beginScheduleDate", yearMonth + "-01");
        schedQuery.getParams().put("endScheduleDate", monthLastDay(yearMonth));
        List<HrSchedule> schedules = hrScheduleMapper.selectHrScheduleList(schedQuery);
        for (HrSchedule s : schedules)
        {
            if (s.getScheduleDate() != null)
            {
                java.time.LocalDate d = s.getScheduleDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                ret.put(d, s.getShiftName() != null ? s.getShiftName() : "默认班次");
            }
        }
        // 兜底：无排班员工取日历工作日，班次名标为"默认班次"
        if (ret.isEmpty())
        {
            for (java.time.LocalDate d : calendarWorkdaySet(yearMonth))
            {
                ret.put(d, "默认班次");
            }
        }
        return ret;
    }

    /** 单据在指定月份内的天数（跨月按当月内天数截取；无日期按 0.5 天计） */
    private BigDecimal calcSpanDays(Date begin, Date end, String yearMonth)
    {
        if (begin == null)
        {
            return new BigDecimal("0.5");
        }
        java.time.LocalDate monthBegin = java.time.LocalDate.parse(yearMonth + "-01");
        java.time.LocalDate monthEnd = monthBegin.plusMonths(1);
        java.time.LocalDate b = begin.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.time.LocalDate e = end == null ? b : end.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        if (e.isBefore(b)) { e = b; }
        java.time.LocalDate lo = b.isBefore(monthBegin) ? monthBegin : b;
        java.time.LocalDate hi = (e.isBefore(monthEnd) ? e : monthEnd.minusDays(1));
        if (hi.isBefore(lo)) { return BigDecimal.ZERO; }
        return new BigDecimal((int) java.time.temporal.ChronoUnit.DAYS.between(lo, hi) + 1);
    }

    /** 当月日历工作日天数（date_type=0；用于无排班员工的默认班次应出勤兜底） */
    private int countCalendarWorkdays(String yearMonth)
    {
        HrWorkdayCalendar calQuery = new HrWorkdayCalendar();
        calQuery.setYear(Integer.parseInt(yearMonth.substring(0, 4)));
        calQuery.setMonth(Integer.parseInt(yearMonth.substring(5, 7)));
        int count = 0;
        for (HrWorkdayCalendar c : hrWorkdayCalendarMapper.selectHrWorkdayCalendarList(calQuery))
        {
            if ("0".equals(c.getDateType()))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * 计算当月应出勤【日期集合】：有排班的员工取排班日期去重集合（不剔除周末/节假日）；
     * 无排班的员工取当月日历工作日集合（date_type=0，默认班次兜底）。
     * 打卡统计与旷工倒算均以此为"应出勤日"边界。
     */
    private Map<Long, Set<java.time.LocalDate>> calcRequiredDaySets(String yearMonth)
    {
        Map<Long, Set<java.time.LocalDate>> ret = new HashMap<>();
        HrSchedule schedQuery = new HrSchedule();
        schedQuery.getParams().put("beginScheduleDate", yearMonth + "-01");
        schedQuery.getParams().put("endScheduleDate", monthLastDay(yearMonth));
        List<HrSchedule> schedules = hrScheduleMapper.selectHrScheduleList(schedQuery);
        for (HrSchedule s : schedules)
        {
            if (s.getEmployeeId() == null || s.getScheduleDate() == null)
            {
                continue;
            }
            // shift_id 为 0/NULL 表示休息班，不计应出勤
            if (s.getShiftId() == null || s.getShiftId() == 0L)
            {
                continue;
            }
            java.time.LocalDate d = s.getScheduleDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            // 有排班即应出勤：完全按排班来，不剔除周末与法定节假日
            ret.computeIfAbsent(s.getEmployeeId(), k -> new HashSet<>()).add(d);
        }
        // 兜底：无排班员工取当月日历工作日集合（默认班次）
        Set<java.time.LocalDate> calendarWorkdays = calendarWorkdaySet(yearMonth);
        for (HrEmployee emp : hrEmployeeMapper.selectHrEmployeeList(new HrEmployee()))
        {
            if (emp.getEmployeeId() != null && !ret.containsKey(emp.getEmployeeId()))
            {
                ret.put(emp.getEmployeeId(), new HashSet<>(calendarWorkdays));
            }
        }
        return ret;
    }

    /** 当月日历工作日日期集合（date_type=0） */
    private Set<java.time.LocalDate> calendarWorkdaySet(String yearMonth)
    {
        Set<java.time.LocalDate> ret = new HashSet<>();
        HrWorkdayCalendar calQuery = new HrWorkdayCalendar();
        calQuery.setYear(Integer.parseInt(yearMonth.substring(0, 4)));
        calQuery.setMonth(Integer.parseInt(yearMonth.substring(5, 7)));
        for (HrWorkdayCalendar c : hrWorkdayCalendarMapper.selectHrWorkdayCalendarList(calQuery))
        {
            if ("0".equals(c.getDateType()) && c.getCalendarDate() != null)
            {
                ret.add(c.getCalendarDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            }
        }
        return ret;
    }

    /** 当月最后一天（mapper 的 end 条件为 <=，用闭合右端避免把下月 1 号数据算进当月） */
    private String monthLastDay(String yearMonth)
    {
        int y = Integer.parseInt(yearMonth.substring(0, 4));
        int m = Integer.parseInt(yearMonth.substring(5, 7));
        java.time.LocalDate first = java.time.LocalDate.of(y, m, 1).plusMonths(1).minusDays(1);
        return first.toString();
    }

    /** 下月1号（用于左闭右开月度区间，避免 09-31 之类无效日期） */
    private String nextMonthFirstDay(String yearMonth)
    {
        int y = Integer.parseInt(yearMonth.substring(0, 4));
        int m = Integer.parseInt(yearMonth.substring(5, 7));
        return m == 12 ? (y + 1) + "-01-01" : y + "-" + String.format("%02d", m + 1) + "-01";
    }


    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private int nvlInt(Integer v) { return v == null ? 0 : v; }
}
