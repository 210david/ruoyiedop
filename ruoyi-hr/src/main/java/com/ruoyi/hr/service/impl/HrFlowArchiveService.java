package com.ruoyi.hr.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ruoyi.common.event.FlowFormSubmitEvent;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.service.FlowArchiveHandler;
import com.ruoyi.hr.domain.HrAttendanceRecord;
import com.ruoyi.hr.domain.HrBusinessTrip;
import com.ruoyi.hr.domain.HrClockRepair;
import com.ruoyi.hr.domain.HrLeaveRequest;
import com.ruoyi.hr.domain.HrOuting;
import com.ruoyi.hr.domain.HrOvertime;
import com.ruoyi.hr.mapper.HrAttendanceRecordMapper;
import com.ruoyi.hr.service.IHrBusinessTripService;
import com.ruoyi.hr.service.IHrClockRepairService;
import com.ruoyi.hr.service.IHrLeaveRequestService;
import com.ruoyi.hr.service.IHrOutingService;
import com.ruoyi.hr.service.IHrOvertimeService;

/**
 * HR 考勤类审批流归档处理器
 *
 * 流程办结（PROCESS_COMPLETED）后，把流程表单数据回写到人力资源考勤业务表，
 * 实现「审批流 → 考勤数据」联动（月度考勤汇总按这些表统计）：
 *
 * <ul>
 * <li>travelApply（出差申请）→ biz_hr_business_trip 出差单（audit_status=1 已审批）</li>
 * <li>outingApply（外出申请）→ biz_hr_outing 外出单（audit_status=1 已审批）</li>
 * <li>makeupApply（打卡补卡）→ 修正 biz_hr_attendance_record：补录上/下班卡（来源=补卡），
 *     按班次重算迟到/早退分钟与考勤状态，消除漏卡异常</li>
 * <li>overtimeApply（加班申请）→ biz_hr_overtime 加班单（audit_status=1 已审批）</li>
 * <li>wf_94663220701（请假流程，表单设计器版）→ biz_hr_leave_request 请假单（audit_status=1 已审批）</li>
 * <li>leave_process（请假，旧版流程）→ biz_hr_leave_request 请假单（audit_status=1 已审批）</li>
 * </ul>
 *
 * 回写幂等：按员工+日期+事项查重，重复归档自动跳过；申请人无员工档案时跳过并记录日志。
 *
 * @author ruoyi
 */
@Service
public class HrFlowArchiveService implements FlowArchiveHandler, ApplicationListener<FlowFormSubmitEvent>
{
    private static final Logger log = LoggerFactory.getLogger(HrFlowArchiveService.class);

    /** 打卡来源：补卡（字典 hr_clock_source） */
    private static final String CLOCK_SOURCE_MAKEUP = "1";
    /** 打卡来源：外出（字典 hr_clock_source） */
    private static final String CLOCK_SOURCE_OUTING = "3";
    /** 打卡来源：出差（字典 hr_clock_source） */
    private static final String CLOCK_SOURCE_TRIP = "4";
    /** 考勤状态：0正常 1迟到 2早退 3旷工 4漏卡（字典 hr_attendance_status） */
    private static final String ST_NORMAL = "0";
    private static final String ST_LATE = "1";
    private static final String ST_EARLY = "2";
    private static final String ST_MISSING = "4";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IHrBusinessTripService businessTripService;

    @Autowired
    private IHrOutingService outingService;

    @Autowired
    private IHrLeaveRequestService leaveRequestService;

    @Autowired
    private IHrOvertimeService overtimeService;

    @Autowired
    private IHrClockRepairService clockRepairService;

    @Autowired
    private HrAttendanceRecordMapper attendanceRecordMapper;

    @Override
    public Set<String> processKeys()
    {
        return new HashSet<>(Arrays.asList("travelApply", "outingApply", "makeupApply", "overtimeApply",
                "wf_94663220701", "leave_process"));
    }

    @Override
    public void archive(String processKey, String instanceId, String businessKey)
    {
        switch (processKey)
        {
            case "travelApply":
                archiveTrip(instanceId);
                break;
            case "outingApply":
                archiveOuting(instanceId);
                break;
            case "makeupApply":
                archiveMakeup(instanceId);
                break;
            case "overtimeApply":
                archiveOvertime(instanceId);
                break;
            case "wf_94663220701":
                archiveLeaveForm(instanceId);
                break;
            case "leave_process":
                archiveLeave(instanceId, businessKey);
                break;
            default:
                break;
        }
    }

    /* ==================== 流程发起 → 生成考勤台账 ==================== */

    /**
     * 监听流程表单提交事件：流程发起（PHASE_START）后即生成台账记录，
     * 审批状态为「0 待审批」；流程办结后由归档回调更新为「1 已审核」。
     */
    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onApplicationEvent(FlowFormSubmitEvent event)
    {
        if (event == null || !FlowFormSubmitEvent.PHASE_START.equals(event.getPhase())
                || !processKeys().contains(event.getProcessKey()))
        {
            return;
        }
        try
        {
            createLedger(event);
        }
        catch (Exception e)
        {
            // 台账生成失败不影响流程本身
            log.error("流程发起生成考勤台账失败：processKey={}, instanceId={}, error={}", event.getProcessKey(),
                    event.getProcessInstanceId(), e.getMessage(), e);
        }
    }

    private void createLedger(FlowFormSubmitEvent event)
    {
        Map<String, Object> vars = event.getVariables();
        String instanceId = event.getProcessInstanceId();
        if (vars == null || vars.isEmpty() || StringUtils.isEmpty(instanceId))
        {
            return;
        }
        switch (event.getProcessKey())
        {
            case "travelApply":
                createTrip(vars, instanceId);
                break;
            case "outingApply":
                createOuting(vars, instanceId);
                break;
            case "makeupApply":
                createMakeup(vars, instanceId);
                break;
            case "overtimeApply":
                createOvertime(vars, instanceId);
                break;
            case "wf_94663220701":
                createLeave(vars, instanceId);
                break;
            default:
                break;
        }
    }

    private void createTrip(Map<String, Object> vars, String instanceId)
    {
        if (ledgerExists("biz_hr_business_trip", instanceId))
        {
            return;
        }
        Map<String, Object> emp = findEmployee(vars.get("applicant"));
        if (emp == null)
        {
            log.warn("出差流程发起：申请人 {} 无员工档案 instanceId={}", vars.get("applicant"), instanceId);
            return;
        }
        Date start = toDate(vars.get("start_time"));
        Date end = toDate(vars.get("end_time"));
        HrBusinessTrip trip = new HrBusinessTrip();
        trip.setTripNo(businessNo(vars.get("processNo"), "CC"));
        setEmployee(trip, emp);
        trip.setDestination(str(vars.get("travel_place")));
        trip.setReason(str(vars.get("travel_reason")));
        trip.setDepartureDate(start == null ? null : day(start));
        trip.setExpectedReturnDate(end == null ? null : day(end));
        trip.setAttachmentUrl(str(vars.get("attachment")));
        trip.setAuditStatus("0");
        trip.setStatus("0");
        trip.setDelFlag("0");
        trip.setFlowInstanceId(instanceId);
        trip.setCreateBy("flowStart");
        trip.setRemark("出差申请流程发起生成");
        businessTripService.insertHrBusinessTrip(trip);
        log.info("出差流程发起 → 生成出差台账 instanceId={} employeeId={}", instanceId, trip.getEmployeeId());
    }

    private void createOuting(Map<String, Object> vars, String instanceId)
    {
        if (ledgerExists("biz_hr_outing", instanceId))
        {
            return;
        }
        Map<String, Object> emp = findEmployee(vars.get("applicant"));
        if (emp == null)
        {
            log.warn("外出流程发起：申请人 {} 无员工档案 instanceId={}", vars.get("applicant"), instanceId);
            return;
        }
        Date start = toDate(vars.get("start_time"));
        Date end = toDate(vars.get("end_time"));
        HrOuting outing = new HrOuting();
        outing.setOutingNo(businessNo(vars.get("processNo"), "WC"));
        setEmployee(outing, emp);
        outing.setOutingDate(day(start));
        outing.setStartTime(start == null ? null : hm(start));
        outing.setEndTime(end == null ? null : hm(end));
        outing.setDestination(str(vars.get("outing_place")));
        outing.setReason(str(vars.get("outing_reason")));
        outing.setAttachmentUrl(str(vars.get("attachment")));
        outing.setAuditStatus("0");
        outing.setStatus("0");
        outing.setDelFlag("0");
        outing.setFlowInstanceId(instanceId);
        outing.setCreateBy("flowStart");
        outing.setRemark("外出申请流程发起生成");
        outingService.insertHrOuting(outing);
        log.info("外出流程发起 → 生成外出台账 instanceId={} employeeId={}", instanceId, outing.getEmployeeId());
    }

    private void createMakeup(Map<String, Object> vars, String instanceId)
    {
        if (ledgerExists("biz_hr_clock_repair", instanceId))
        {
            return;
        }
        Map<String, Object> emp = findEmployee(vars.get("applicant"));
        if (emp == null)
        {
            log.warn("补卡流程发起：申请人 {} 无员工档案 instanceId={}", vars.get("applicant"), instanceId);
            return;
        }
        Date makeupDate = toDate(vars.get("makeup_date"));
        Date makeupTime = toDate(vars.get("makeup_time"));
        if (makeupDate == null || makeupTime == null)
        {
            log.warn("补卡流程发起：补卡日期/时间为空 instanceId={}", instanceId);
            return;
        }
        LocalDateTime clock = LocalDateTime.of(
                makeupDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                makeupTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime());
        ShiftInfo shift = findShiftByName(str(vars.get("shift_type")));
        boolean fillIn = isClockInSide(clock, shift);
        HrClockRepair repair = new HrClockRepair();
        repair.setClockRepairNo(businessNo(vars.get("processNo"), "BK"));
        setEmployee(repair, emp);
        repair.setRepairDate(day(makeupDate));
        repair.setRepairType(fillIn ? "0" : "1");
        repair.setExpectedClockTime(Date.from(clock.atZone(ZoneId.systemDefault()).toInstant()));
        repair.setActualClockTime(Date.from(clock.atZone(ZoneId.systemDefault()).toInstant()));
        repair.setReason(str(vars.get("makeup_reason")));
        repair.setAuditStatus("0");
        repair.setStatus("0");
        repair.setDelFlag("0");
        repair.setFlowInstanceId(instanceId);
        repair.setCreateBy("flowStart");
        repair.setRemark("打卡补卡流程发起生成");
        clockRepairService.insertHrClockRepair(repair);
        log.info("补卡流程发起 → 生成补卡台账 instanceId={} employeeId={}", instanceId, repair.getEmployeeId());
    }

    private void createOvertime(Map<String, Object> vars, String instanceId)
    {
        if (ledgerExists("biz_hr_overtime", instanceId))
        {
            return;
        }
        Map<String, Object> emp = findEmployee(vars.get("applicant"));
        if (emp == null)
        {
            log.warn("加班流程发起：申请人 {} 无员工档案 instanceId={}", vars.get("applicant"), instanceId);
            return;
        }
        Date overtimeDate = toDate(vars.get("overtime_date"));
        HrOvertime overtime = new HrOvertime();
        overtime.setOvertimeNo(businessNo(vars.get("processNo"), "JB"));
        setEmployee(overtime, emp);
        overtime.setOvertimeDate(overtimeDate == null ? null : day(overtimeDate));
        overtime.setOvertimeType(mapOvertimeType(vars.get("overtime_type")));
        overtime.setStartTime(toDate(vars.get("start_time")));
        overtime.setEndTime(toDate(vars.get("end_time")));
        overtime.setOvertimeHours(toDecimal(vars.get("overtime_hours")));
        overtime.setReason(str(vars.get("reason")));
        overtime.setAuditStatus("0");
        overtime.setStatus("0");
        overtime.setDelFlag("0");
        overtime.setFlowInstanceId(instanceId);
        overtime.setCreateBy("flowStart");
        overtime.setRemark("加班申请流程发起生成");
        overtimeService.insertHrOvertime(overtime);
        log.info("加班流程发起 → 生成加班台账 instanceId={} employeeId={}", instanceId, overtime.getEmployeeId());
    }

    private void createLeave(Map<String, Object> vars, String instanceId)
    {
        if (ledgerExists("biz_hr_leave_request", instanceId))
        {
            return;
        }
        Map<String, Object> emp = findEmployee(vars.get("sqr"));
        if (emp == null)
        {
            log.warn("请假流程发起：申请人 {} 无员工档案 instanceId={}", vars.get("sqr"), instanceId);
            return;
        }
        Date start = toDate(vars.get("start_date"));
        Date end = toDate(vars.get("end_date"));
        BigDecimal days = toDecimal(vars.get("days"));
        if (days == null || days.compareTo(BigDecimal.ZERO) <= 0)
        {
            days = calcDays(start, end);
        }
        HrLeaveRequest leave = new HrLeaveRequest();
        leave.setLeaveReqNo(businessNo(vars.get("processNo"), "QJ"));
        setEmployee(leave, emp);
        leave.setLeaveType(mapLeaveType(vars.get("leave_type")));
        leave.setStartTime(start);
        leave.setEndTime(end);
        leave.setLeaveDays(days);
        leave.setReason(str(vars.get("reason")));
        leave.setAttachmentUrl(str(vars.get("xgfj")));
        leave.setAuditStatus("0");
        leave.setStatus("0");
        leave.setDelFlag("0");
        leave.setFlowInstanceId(instanceId);
        leave.setCreateBy("flowStart");
        leave.setRemark("请假流程发起生成");
        leaveRequestService.insertHrLeaveRequest(leave);
        log.info("请假流程发起 → 生成请假台账 instanceId={} employeeId={} days={}", instanceId, leave.getEmployeeId(), days);
    }

    /* ==================== 出差申请 → 出差单 ==================== */

    private void archiveTrip(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT processNo, applicant, travel_place, travel_reason, start_time, end_time, attachment "
                        + "FROM flow_data_travel_apply WHERE process_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            log.error("出差归档跳过：未找到流程表单数据 instanceId={}", instanceId);
            return;
        }
        Map<String, Object> row = rows.get(0);
        String processNo = str(row.get("processNo"));
        if (updateArchived("biz_hr_business_trip", instanceId, "出差流程办结自动归档（流程编号 " + processNo + "）"))
        {
            // 台账已随流程发起生成：状态更新后仍需把出差期间打卡回写到考勤记录
            writeTripAttendance(instanceId);
            return;
        }
        Map<String, Object> emp = findEmployee(row.get("applicant"));
        if (emp == null)
        {
            log.error("出差归档跳过：申请人 {} 无员工档案 instanceId={}", row.get("applicant"), instanceId);
            return;
        }
        Long employeeId = toLong(emp.get("employee_id"));
        Date start = toDate(row.get("start_time"));
        Date end = toDate(row.get("end_time"));
        if (dedup("biz_hr_business_trip", employeeId, "departure_date", start, "destination", row.get("travel_place")))
        {
            return;
        }
        HrBusinessTrip trip = new HrBusinessTrip();
        trip.setTripNo(processNo);
        trip.setFlowInstanceId(instanceId);
        trip.setEmployeeId(employeeId);
        trip.setEmployeeName(str(emp.get("employee_name")));
        trip.setEmployeeNo(str(emp.get("employee_no")));
        trip.setDeptId(toLong(emp.get("org_id")));
        trip.setDeptName(deptName(emp.get("org_id")));
        trip.setDestination(str(row.get("travel_place")));
        trip.setReason(str(row.get("travel_reason")));
        trip.setDepartureDate(start == null ? null : Date.from(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        trip.setExpectedReturnDate(end == null ? null : Date.from(end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        trip.setAttachmentUrl(str(row.get("attachment")));
        trip.setAuditStatus("1");
        trip.setStatus("0");
        trip.setDelFlag("0");
        trip.setAuditBy(lastApprover(instanceId));
        trip.setAuditTime(new Date());
        trip.setAuditRemark("出差流程办结自动归档（流程编号 " + str(row.get("processNo")) + "）");
        trip.setCreateBy("flowArchive");
        businessTripService.insertHrBusinessTrip(trip);
        writeTripAttendance(instanceId);
        log.info("出差流程归档完成 instanceId={} → 出差单 employeeId={}", instanceId, employeeId);
    }

    /* ==================== 外出申请 → 外出单 ==================== */

    private void archiveOuting(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT processNo, applicant, outing_place, outing_reason, start_time, end_time, attachment "
                        + "FROM flow_data_outing_apply WHERE process_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            log.error("外出归档跳过：未找到流程表单数据 instanceId={}", instanceId);
            return;
        }
        Map<String, Object> row = rows.get(0);
        String processNo = str(row.get("processNo"));
        if (updateArchived("biz_hr_outing", instanceId, "外出流程办结自动归档（流程编号 " + processNo + "）"))
        {
            // 台账已随流程发起生成：状态更新后仍需把外出打卡回写到考勤记录
            writeOutingAttendance(instanceId);
            return;
        }
        Map<String, Object> emp = findEmployee(row.get("applicant"));
        if (emp == null)
        {
            log.error("外出归档跳过：申请人 {} 无员工档案 instanceId={}", row.get("applicant"), instanceId);
            return;
        }
        Long employeeId = toLong(emp.get("employee_id"));
        Date start = toDate(row.get("start_time"));
        Date end = toDate(row.get("end_time"));
        if (dedup("biz_hr_outing", employeeId, "outing_date", start == null ? null : day(start), "destination", row.get("outing_place")))
        {
            return;
        }
        HrOuting outing = new HrOuting();
        outing.setOutingNo(processNo);
        outing.setFlowInstanceId(instanceId);
        outing.setEmployeeId(employeeId);
        outing.setEmployeeName(str(emp.get("employee_name")));
        outing.setEmployeeNo(str(emp.get("employee_no")));
        outing.setDeptId(toLong(emp.get("org_id")));
        outing.setDeptName(deptName(emp.get("org_id")));
        outing.setOutingDate(day(start));
        outing.setStartTime(start == null ? null : hm(start));
        outing.setEndTime(end == null ? null : hm(end));
        outing.setDestination(str(row.get("outing_place")));
        outing.setReason(str(row.get("outing_reason")));
        outing.setAttachmentUrl(str(row.get("attachment")));
        outing.setAuditStatus("1");
        outing.setStatus("0");
        outing.setDelFlag("0");
        outing.setAuditBy(lastApprover(instanceId));
        outing.setAuditTime(new Date());
        outing.setAuditRemark("外出流程办结自动归档（流程编号 " + str(row.get("processNo")) + "）");
        outing.setCreateBy("flowArchive");
        outingService.insertHrOuting(outing);
        writeOutingAttendance(instanceId);
        log.info("外出流程归档完成 instanceId={} → 外出单 employeeId={}", instanceId, employeeId);
    }

    /* ==================== 打卡补卡 → 修正打卡记录 ==================== */

    private void archiveMakeup(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT processNo, applicant, makeup_date, shift_type, makeup_time, makeup_reason "
                        + "FROM flow_data_makeup_apply WHERE process_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            log.error("补卡归档跳过：未找到流程表单数据 instanceId={}", instanceId);
            return;
        }
        Map<String, Object> row = rows.get(0);
        String processNo = str(row.get("processNo"));
        // 补卡台账状态随流程办结更新，考勤卡修正逻辑继续执行
        updateArchived("biz_hr_clock_repair", instanceId, "补卡流程办结自动归档（流程编号 " + processNo + "）");
        Map<String, Object> emp = findEmployee(row.get("applicant"));
        if (emp == null)
        {
            log.error("补卡归档跳过：申请人 {} 无员工档案 instanceId={}", row.get("applicant"), instanceId);
            return;
        }
        Long employeeId = toLong(emp.get("employee_id"));
        Date makeupDate = toDate(row.get("makeup_date"));
        Date makeupTime = toDate(row.get("makeup_time"));
        if (makeupDate == null || makeupTime == null)
        {
            log.error("补卡归档跳过：补卡日期/时间为空 instanceId={}", instanceId);
            return;
        }
        // 补卡时间点 = 补卡日期 + 补卡时间（time部分），保证落卡在补卡日期当天
        LocalDateTime clock = LocalDateTime.of(
                makeupDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                makeupTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime());
        String shiftType = str(row.get("shift_type"));

        // 班次解析：优先按表单班次名称匹配考勤模块班次配置，其次取员工当日已有记录的班次
        ShiftInfo shift = findShiftByName(shiftType);
        HrAttendanceRecord record = findRecord(employeeId, makeupDate);
        if (record == null)
        {
            if (shift == null)
            {
                log.error("补卡归档跳过：无当日打卡记录且班次无法解析（班次={}）instanceId={}", shiftType, instanceId);
                return;
            }
            record = new HrAttendanceRecord();
            record.setRecordDate(day(makeupDate));
            record.setEmployeeId(employeeId);
            record.setEmployeeName(str(emp.get("employee_name")));
            record.setEmployeeNo(str(emp.get("employee_no")));
            record.setOrgId(toLong(emp.get("org_id")));
            record.setShiftId(shift.id);
            record.setShiftName(shift.name);
            record.setClockInSource(CLOCK_SOURCE_MAKEUP);
            record.setClockOutSource(CLOCK_SOURCE_MAKEUP);
            record.setRemark("补卡流程归档：" + processNo + "；事由：" + str(row.get("makeup_reason")));
            record.setCreateBy("flowArchive");
            record.setDelFlag("0");
            applyMakeup(record, clock, shift, true);
            attendanceRecordMapper.insertHrAttendanceRecord(record);
            log.info("补卡流程归档完成（新建打卡记录）instanceId={} recordDate={} shift={}", instanceId, clock.toLocalDate(), shift.name);
            return;
        }

        // 已有当日记录：补空缺卡位；两端均有卡时按时间点与班次判定补上/下班卡
        if (shift == null)
        {
            shift = findShiftById(record.getShiftId());
        }
        boolean fillIn = record.getClockIn() == null;
        boolean fillOut = record.getClockOut() == null;
        if (!fillIn && !fillOut)
        {
            fillIn = isClockInSide(clock, shift);
        }
        record.setUpdateBy("flowArchive");
        String suffix = "；补卡归档：" + processNo + "（补" + (fillIn ? "上班" : "下班") + "卡 " + clock.toLocalTime() + "）";
        record.setRemark(StringUtils.isEmpty(record.getRemark()) ? suffix.replaceFirst("；", "") : record.getRemark() + suffix);
        applyMakeup(record, clock, shift, fillIn);
        attendanceRecordMapper.updateHrAttendanceRecord(record);
        log.info("补卡流程归档完成（修正打卡记录）instanceId={} recordId={} 补{}班卡", instanceId, record.getRecordId(), fillIn ? "上" : "下");
    }

    /**
     * 应用补卡：写入上/下班卡（来源=补卡）并按班次重算迟到/早退与考勤状态
     */
    private void applyMakeup(HrAttendanceRecord record, LocalDateTime clock, ShiftInfo shift, boolean fillIn)
    {
        Date clockDate = Date.from(clock.atZone(ZoneId.systemDefault()).toInstant());
        if (fillIn)
        {
            record.setClockIn(clockDate);
            record.setClockInSource(CLOCK_SOURCE_MAKEUP);
        }
        else
        {
            record.setClockOut(clockDate);
            record.setClockOutSource(CLOCK_SOURCE_MAKEUP);
        }
        recalcAttendance(record, shift);
    }

    /**
     * 按班次重算迟到/早退分钟与考勤状态
     */
    private void recalcAttendance(HrAttendanceRecord record, ShiftInfo shift)
    {
        int late = 0;
        int early = 0;
        int inMin = record.getClockIn() == null ? -1 : toMinutes(record.getClockIn());
        int outMin = record.getClockOut() == null ? -1 : toMinutes(record.getClockOut());
        if (inMin >= 0 && shift != null && shift.startMin >= 0)
        {
            late = Math.max(0, inMin - shift.startMin);
        }
        if (outMin >= 0 && shift != null && shift.endMin >= 0)
        {
            early = Math.max(0, shift.endMin - outMin);
        }
        record.setLateMinutes(late);
        record.setEarlyMinutes(early);
        if (inMin < 0 || outMin < 0)
        {
            record.setAttendanceStatus(ST_MISSING);
        }
        else if (late > 0)
        {
            record.setAttendanceStatus(ST_LATE);
        }
        else if (early > 0)
        {
            record.setAttendanceStatus(ST_EARLY);
        }
        else
        {
            record.setAttendanceStatus(ST_NORMAL);
        }
    }

    /** 判定补卡时间点属上班卡还是下班卡（班次开始时间+60分钟内为上班侧；无班次以12点分界） */
    private boolean isClockInSide(LocalDateTime clock, ShiftInfo shift)
    {
        int tod = clock.toLocalTime().getHour() * 60 + clock.toLocalTime().getMinute();
        if (shift != null && shift.startMin >= 0)
        {
            return tod <= shift.startMin + 60;
        }
        return tod < 12 * 60;
    }

    /* ==================== 加班申请 → 加班单 ==================== */

    private void archiveOvertime(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT processNo, applicant, overtime_date, overtime_type, start_time, end_time, overtime_hours, reason "
                        + "FROM flow_data_overtime_apply WHERE process_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            log.error("加班归档跳过：未找到流程表单数据 instanceId={}", instanceId);
            return;
        }
        Map<String, Object> row = rows.get(0);
        String processNo = str(row.get("processNo"));
        if (updateArchived("biz_hr_overtime", instanceId, "加班流程办结自动归档（流程编号 " + processNo + "）"))
        {
            return;
        }
        Map<String, Object> emp = findEmployee(row.get("applicant"));
        if (emp == null)
        {
            log.error("加班归档跳过：申请人 {} 无员工档案 instanceId={}", row.get("applicant"), instanceId);
            return;
        }
        Long employeeId = toLong(emp.get("employee_id"));
        Date overtimeDate = toDate(row.get("overtime_date"));
        String reason = str(row.get("reason"));
        if (dedup("biz_hr_overtime", employeeId, "overtime_date", overtimeDate == null ? null : day(overtimeDate),
                "reason", reason))
        {
            return;
        }
        HrOvertime overtime = new HrOvertime();
        overtime.setOvertimeNo(processNo);
        overtime.setFlowInstanceId(instanceId);
        overtime.setEmployeeId(employeeId);
        overtime.setEmployeeName(str(emp.get("employee_name")));
        overtime.setEmployeeNo(str(emp.get("employee_no")));
        overtime.setDeptId(toLong(emp.get("org_id")));
        overtime.setDeptName(deptName(emp.get("org_id")));
        overtime.setOvertimeDate(overtimeDate == null ? null : day(overtimeDate));
        overtime.setOvertimeType(str(row.get("overtime_type")));
        overtime.setStartTime(toDate(row.get("start_time")));
        overtime.setEndTime(toDate(row.get("end_time")));
        overtime.setOvertimeHours(toDecimal(row.get("overtime_hours")));
        overtime.setReason(reason);
        overtime.setAuditStatus("1");
        overtime.setStatus("0");
        overtime.setDelFlag("0");
        overtime.setAuditBy(lastApprover(instanceId));
        overtime.setAuditTime(new Date());
        overtime.setAuditRemark("加班流程办结自动归档（流程编号 " + str(row.get("processNo")) + "）");
        overtime.setCreateBy("flowArchive");
        overtimeService.insertHrOvertime(overtime);
        log.info("加班流程归档完成 instanceId={} → 加班单 employeeId={} date={} hours={}", instanceId, employeeId,
                overtimeDate, row.get("overtime_hours"));
    }

    /* ==================== 请假（表单设计器流程）→ 请假单 ==================== */

    private void archiveLeaveForm(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT processNo, sqr, leave_type, start_date, end_date, days, reason, xgfj "
                        + "FROM flow_data_leave_form WHERE process_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            log.error("请假归档跳过：未找到流程表单数据 instanceId={}", instanceId);
            return;
        }
        Map<String, Object> row = rows.get(0);
        String processNo = str(row.get("processNo"));
        if (archiveLeaveLedger(instanceId, "请假流程办结自动归档（流程编号 " + processNo + "）"))
        {
            return;
        }
        Map<String, Object> emp = findEmployee(row.get("sqr"));
        if (emp == null)
        {
            log.error("请假归档跳过：申请人 {} 无员工档案 instanceId={}", row.get("sqr"), instanceId);
            return;
        }
        Long employeeId = toLong(emp.get("employee_id"));
        Date start = toDate(row.get("start_date"));
        Date end = toDate(row.get("end_date"));
        if (dedup("biz_hr_leave_request", employeeId, "start_time", start, "leave_type", row.get("leave_type")))
        {
            return;
        }
        BigDecimal days = toDecimal(row.get("days"));
        if (days == null || days.compareTo(BigDecimal.ZERO) <= 0)
        {
            long d = 1;
            if (start != null && end != null)
            {
                d = (day(end).getTime() - day(start).getTime()) / 86400000L + 1;
            }
            days = BigDecimal.valueOf(d);
        }
        HrLeaveRequest leave = new HrLeaveRequest();
        leave.setLeaveReqNo(processNo == null ? businessNo(null, "QJ") : processNo);
        leave.setFlowInstanceId(instanceId);
        leave.setEmployeeId(employeeId);
        leave.setEmployeeName(str(emp.get("employee_name")));
        leave.setEmployeeNo(str(emp.get("employee_no")));
        leave.setDeptId(toLong(emp.get("org_id")));
        leave.setDeptName(deptName(emp.get("org_id")));
        leave.setLeaveType(str(row.get("leave_type")));
        leave.setStartTime(start);
        leave.setEndTime(end);
        leave.setLeaveDays(days);
        leave.setReason(str(row.get("reason")));
        leave.setAttachmentUrl(str(row.get("xgfj")));
        leave.setAuditStatus("1");
        leave.setStatus("0");
        leave.setDelFlag("0");
        leave.setAuditBy(lastApprover(instanceId));
        leave.setAuditTime(new Date());
        leave.setAuditRemark("请假流程办结自动归档（流程编号 " + str(row.get("processNo")) + "）");
        leave.setCreateBy("flowArchive");
        leaveRequestService.insertHrLeaveRequest(leave);
        // 旧数据补录：插入时已冻结额度，办结后需解冻并转为已休
        leaveRequestService.settleLeaveBalanceByFlow(instanceId);
        log.info("请假流程归档完成 instanceId={} → 请假单 employeeId={} days={}", instanceId, employeeId, days);
    }

    /* ==================== 请假台账状态与假期余额联动 ==================== */

    /**
     * 请假流程办结：更新台账状态；若为首次办结，同时把发起时冻结的额度转为已休
     *
     * 返回 true 表示台账已存在（本次只做状态更新/余额结算）。
     */
    private boolean archiveLeaveLedger(String instanceId, String remark)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT audit_status FROM biz_hr_leave_request WHERE flow_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            return false;
        }
        boolean firstTime = "0".equals(str(rows.get(0).get("audit_status")));
        updateArchived("biz_hr_leave_request", instanceId, remark);
        if (firstTime)
        {
            leaveRequestService.settleLeaveBalanceByFlow(instanceId);
        }
        return true;
    }

    /* ==================== 请假（旧版流程）→ 请假单 ==================== */

    private void archiveLeave(String instanceId, String businessKey)
    {
        if (StringUtils.isEmpty(businessKey))
        {
            log.error("请假归档跳过：businessKey 为空 instanceId={}", instanceId);
            return;
        }
        if (archiveLeaveLedger(instanceId, "请假流程办结自动归档"))
        {
            return;
        }
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT title, leave_type, start_date, end_date, days, reason, start_user_id, start_user_name "
                        + "FROM flow_leave WHERE leave_id = ? LIMIT 1", Long.valueOf(businessKey));
        if (rows.isEmpty())
        {
            log.error("请假归档跳过：未找到请假数据 leaveId={} instanceId={}", businessKey, instanceId);
            return;
        }
        Map<String, Object> row = rows.get(0);
        Long userId = toLong(row.get("start_user_id"));
        Map<String, Object> emp = findEmployee(userId);
        if (emp == null)
        {
            log.error("请假归档跳过：发起人 userId={} 无员工档案 instanceId={}", userId, instanceId);
            return;
        }
        Long employeeId = toLong(emp.get("employee_id"));
        Date start = toDate(row.get("start_date"));
        Date end = toDate(row.get("end_date"));
        if (dedup("biz_hr_leave_request", employeeId, "start_time", start, "leave_type", row.get("leave_type")))
        {
            return;
        }
        BigDecimal days = toDecimal(row.get("days"));
        if (days == null || days.compareTo(BigDecimal.ZERO) <= 0)
        {
            long d = 1;
            if (start != null && end != null)
            {
                long diff = (day(end).getTime() - day(start).getTime()) / 86400000L;
                d = diff + 1;
            }
            days = BigDecimal.valueOf(d);
        }
        HrLeaveRequest leave = new HrLeaveRequest();
        leave.setLeaveReqNo(String.format("LQ%s%04d", new java.text.SimpleDateFormat("yyyyMMdd").format(new Date()),
                (System.currentTimeMillis() % 10000)));
        leave.setFlowInstanceId(instanceId);
        leave.setEmployeeId(employeeId);
        leave.setEmployeeName(str(emp.get("employee_name")));
        leave.setEmployeeNo(str(emp.get("employee_no")));
        leave.setDeptId(toLong(emp.get("org_id")));
        leave.setDeptName(deptName(emp.get("org_id")));
        leave.setLeaveType(str(row.get("leave_type")));
        leave.setStartTime(start);
        leave.setEndTime(end);
        leave.setLeaveDays(days);
        leave.setReason(str(row.get("reason")));
        leave.setAuditStatus("1");
        leave.setStatus("0");
        leave.setDelFlag("0");
        leave.setAuditBy(lastApprover(instanceId));
        leave.setAuditTime(new Date());
        leave.setAuditRemark("请假流程办结自动归档（" + str(row.get("title")) + "）");
        leave.setCreateBy("flowArchive");
        leaveRequestService.insertHrLeaveRequest(leave);
        // 旧数据补录：插入时已冻结额度，办结后需解冻并转为已休
        leaveRequestService.settleLeaveBalanceByFlow(instanceId);
        log.info("请假流程归档完成 instanceId={} → 请假单 employeeId={} days={}", instanceId, employeeId, days);
    }

    /* ==================== 公共辅助 ==================== */

    /** 惰性获取 JdbcTemplate（归档回调在流程引擎事件线程内执行） */
    private JdbcTemplate jdbcTemplate()
    {
        return jdbcTemplate;
    }

    /** 台账是否已按流程实例生成（发起事件幂等） */
    private boolean ledgerExists(String table, String instanceId)
    {
        Integer cnt = jdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE flow_instance_id = ?", Integer.class, instanceId);
        return cnt != null && cnt > 0;
    }

    /** 流程办结后更新台账审批状态（发起时已生成的记录） */
    private boolean updateArchived(String table, String instanceId, String remark)
    {
        int rows = jdbcTemplate().update(
                "UPDATE " + table + " SET audit_status = '1', audit_by = ?, audit_time = NOW(), audit_remark = ? "
                        + "WHERE flow_instance_id = ?",
                lastApprover(instanceId), remark, instanceId);
        return rows > 0;
    }

    /** 流程终止/撤销后更新台账状态为「3 已撤销」 */
    @Override
    public void cancelled(String processKey, String instanceId, String businessKey)
    {
        if (!processKeys().contains(processKey))
        {
            return;
        }
        String[] tables = { "biz_hr_business_trip", "biz_hr_outing", "biz_hr_clock_repair", "biz_hr_overtime",
                "biz_hr_leave_request" };
        for (String table : tables)
        {
            try
            {
                if ("biz_hr_leave_request".equals(table))
                {
                    // 请假：撤销时释放发起阶段冻结的额度（仅审批中的单据）
                    leaveRequestService.cancelLeaveBalanceByFlow(instanceId);
                }
                // 只回写仍处于审批中的记录，已办结的台账不允许被撤销覆盖
                jdbcTemplate.update(
                        "UPDATE " + table + " SET audit_status = '3', audit_remark = '流程已终止/撤销，台账自动关闭' "
                                + "WHERE flow_instance_id = ? AND audit_status = '0'",
                        instanceId);
            }
            catch (Exception e)
            {
                log.warn("流程撤销更新台账失败：table={}, instanceId={}, error={}", table, instanceId, e.getMessage());
            }
        }
    }

    /**
     * 流程复活：台账从「已通过/已撤销」回退到「审批中」，并回滚办结时产生的副作用
     *
     * 回滚内容：出差/外出/补卡写入的考勤打卡、请假已扣减的假期额度、台账审批状态。
     */
    @Override
    public void rollback(String processKey, String instanceId, String businessKey)
    {
        if (!processKeys().contains(processKey))
        {
            return;
        }
        try
        {
            rollbackTripAttendance(instanceId);
        }
        catch (Exception e)
        {
            log.warn("回滚出差考勤失败 instanceId={} error={}", instanceId, e.getMessage());
        }
        try
        {
            rollbackOutingAttendance(instanceId);
        }
        catch (Exception e)
        {
            log.warn("回滚外出考勤失败 instanceId={} error={}", instanceId, e.getMessage());
        }
        try
        {
            rollbackMakeupAttendance(instanceId);
        }
        catch (Exception e)
        {
            log.warn("回滚补卡考勤失败 instanceId={} error={}", instanceId, e.getMessage());
        }
        try
        {
            leaveRequestService.rollbackLeaveBalanceByFlow(instanceId);
        }
        catch (Exception e)
        {
            log.warn("回滚请假余额失败 instanceId={} error={}", instanceId, e.getMessage());
        }

        String[] tables = { "biz_hr_business_trip", "biz_hr_outing", "biz_hr_clock_repair", "biz_hr_overtime",
                "biz_hr_leave_request" };
        for (String table : tables)
        {
            jdbcTemplate.update(
                    "UPDATE " + table + " SET audit_status = '0', audit_by = NULL, audit_time = NULL, "
                            + "audit_remark = '流程复活回退，台账重新进入审批中' "
                            + "WHERE flow_instance_id = ? AND audit_status IN ('1', '3')",
                    instanceId);
        }
        log.info("流程复活回滚台账完成 instanceId={}", instanceId);
    }

    /** 流程复活后实例ID变更：台账关联的 flow_instance_id 切到新实例 */
    @Override
    public void rebind(String processKey, String oldInstanceId, String newInstanceId)
    {
        if (!processKeys().contains(processKey) || StringUtils.isEmpty(newInstanceId))
        {
            return;
        }
        String[] tables = { "biz_hr_business_trip", "biz_hr_outing", "biz_hr_clock_repair", "biz_hr_overtime",
                "biz_hr_leave_request" };
        for (String table : tables)
        {
            jdbcTemplate.update("UPDATE " + table + " SET flow_instance_id = ? WHERE flow_instance_id = ?",
                    newInstanceId, oldInstanceId);
        }
        log.info("流程复活台账实例ID已切换 old={} new={}", oldInstanceId, newInstanceId);
    }

    /** 回滚出差办结时写入的考勤打卡 */
    private void rollbackTripAttendance(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT employee_id, departure_date, COALESCE(actual_return_date, expected_return_date) AS end_date "
                        + "FROM biz_hr_business_trip WHERE flow_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            return;
        }
        Map<String, Object> trip = rows.get(0);
        Long employeeId = toLong(trip.get("employee_id"));
        Date start = toDate(trip.get("departure_date"));
        Date end = toDate(trip.get("end_date"));
        if (employeeId == null || start == null || end == null || end.before(start))
        {
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(day(start));
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(day(end));
        while (!cal.after(endCal))
        {
            rollbackAttendanceCoverage(employeeId, cal.getTime(), CLOCK_SOURCE_TRIP, "出差流程办结补充");
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    /** 回滚外出办结时写入的考勤打卡 */
    private void rollbackOutingAttendance(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT employee_id, outing_date FROM biz_hr_outing WHERE flow_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            return;
        }
        Map<String, Object> outing = rows.get(0);
        rollbackAttendanceCoverage(toLong(outing.get("employee_id")), toDate(outing.get("outing_date")),
                CLOCK_SOURCE_OUTING, "外出流程办结补充");
    }

    /** 回滚补卡办结时写入的考勤打卡（按补卡类型清理对应卡位） */
    private void rollbackMakeupAttendance(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT employee_id, repair_date, repair_type FROM biz_hr_clock_repair WHERE flow_instance_id = ? LIMIT 1",
                instanceId);
        if (rows.isEmpty())
        {
            return;
        }
        Map<String, Object> repair = rows.get(0);
        Long employeeId = toLong(repair.get("employee_id"));
        Date repairDate = toDate(repair.get("repair_date"));
        String repairType = str(repair.get("repair_type"));
        if (employeeId == null || repairDate == null || StringUtils.isEmpty(repairType))
        {
            return;
        }
        HrAttendanceRecord record = findRecord(employeeId, repairDate);
        if (record == null)
        {
            return;
        }
        for (String type : repairType.split(","))
        {
            if ("0".equals(type.trim()) && CLOCK_SOURCE_MAKEUP.equals(record.getClockInSource()))
            {
                record.setClockIn(null);
                record.setClockInSource(null);
            }
            if ("1".equals(type.trim()) && CLOCK_SOURCE_MAKEUP.equals(record.getClockOutSource()))
            {
                record.setClockOut(null);
                record.setClockOutSource(null);
            }
        }
        persistRolledBackRecord(record, "补卡流程办结补充");
    }

    /**
     * 回滚某天的考勤打卡：只清理办结时写入的卡位
     *
     * 若整条记录是流程办结时新建的（两端都是该来源、且无真实打卡），直接删除；
     * 否则只清空对应卡位并重算考勤状态，保留员工真实打卡。
     */
    private void rollbackAttendanceCoverage(Long employeeId, Date recordDate, String source, String remarkText)
    {
        if (employeeId == null || recordDate == null)
        {
            return;
        }
        HrAttendanceRecord record = findRecord(employeeId, recordDate);
        if (record == null)
        {
            return;
        }
        boolean inFromFlow = source.equals(record.getClockInSource());
        boolean outFromFlow = source.equals(record.getClockOutSource());
        if (!inFromFlow && !outFromFlow)
        {
            return;
        }
        if (inFromFlow)
        {
            record.setClockIn(null);
            record.setClockInSource(null);
        }
        if (outFromFlow)
        {
            record.setClockOut(null);
            record.setClockOutSource(null);
        }
        persistRolledBackRecord(record, remarkText);
    }

    /** 回滚后的考勤记录落库：流程生成的空记录直接删除，其余重算后更新 */
    private void persistRolledBackRecord(HrAttendanceRecord record, String remarkText)
    {
        if (record.getClockIn() == null && record.getClockOut() == null && "flowArchive".equals(record.getCreateBy()))
        {
            attendanceRecordMapper.deleteHrAttendanceRecordById(record.getRecordId());
            return;
        }
        if (record.getRemark() != null && !remarkText.isEmpty())
        {
            String remark = record.getRemark()
                    .replace("；" + remarkText, "")
                    .replace(remarkText + "；", "")
                    .replace(remarkText, "");
            record.setRemark(remark);
        }
        ShiftInfo shift = findShiftById(record.getShiftId());
        if (shift == null)
        {
            shift = findDefaultShift();
        }
        recalcAttendance(record, shift);
        attendanceRecordMapper.updateHrAttendanceRecord(record);
    }

    /** 按申请人（用户ID或姓名）查员工档案（biz_hr_employee.org_id 即 sys_dept.dept_id） */
    private Map<String, Object> findEmployee(Object applicant)
    {
        if (applicant == null)
        {
            return null;
        }
        String value = applicant.toString().trim();
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        List<Map<String, Object>> list;
        if (value.matches("\\d+"))
        {
            Long userId = Long.valueOf(value);
            list = jdbcTemplate().queryForList(
                    "SELECT employee_id, employee_no, employee_name, org_id FROM biz_hr_employee "
                            + "WHERE user_id = ? AND del_flag = '0' LIMIT 1", userId);
            if (list.isEmpty())
            {
                list = jdbcTemplate().queryForList(
                        "SELECT employee_id, employee_no, employee_name, org_id FROM biz_hr_employee "
                                + "WHERE employee_id = ? AND del_flag = '0' LIMIT 1", userId);
            }
        }
        else
        {
            list = jdbcTemplate().queryForList(
                    "SELECT employee_id, employee_no, employee_name, org_id FROM biz_hr_employee "
                            + "WHERE employee_name = ? AND del_flag = '0' LIMIT 1", value);
        }
        return list.isEmpty() ? null : list.get(0);
    }

    /** 流程单号：优先取流程编号，缺失时按业务前缀生成 */
    private static String businessNo(Object processNo, String prefix)
    {
        String no = str(processNo);
        if (StringUtils.isNotEmpty(no))
        {
            return no;
        }
        return prefix + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    private static BigDecimal calcDays(Date start, Date end)
    {
        if (start == null || end == null)
        {
            return BigDecimal.ONE;
        }
        long diff = (day(end).getTime() - day(start).getTime()) / 86400000L;
        return BigDecimal.valueOf(Math.max(1, diff + 1));
    }

    /** 请假流程假别名称 → 字典值（hr_leave_type） */
    private static String mapLeaveType(Object type)
    {
        String s = str(type);
        if (StringUtils.isEmpty(s))
        {
            return s;
        }
        switch (s.trim())
        {
            case "事假": return "1";
            case "病假": return "2";
            case "年假": return "3";
            case "婚假": return "4";
            case "产假": return "5";
            case "陪产假": return "6";
            case "丧假": return "7";
            case "工伤假": return "8";
            case "调休": return "9";
            default: return s.trim();
        }
    }

    /** 加班类型名称 → 字典值（hr_overtime_type） */
    private static String mapOvertimeType(Object type)
    {
        String s = str(type);
        if (StringUtils.isEmpty(s))
        {
            return s;
        }
        switch (s.trim())
        {
            case "工作日": return "0";
            case "休息日": return "1";
            case "法定节假日": return "2";
            default: return s.trim();
        }
    }

    private void setEmployee(HrBusinessTrip target, Map<String, Object> emp)
    {
        target.setEmployeeId(toLong(emp.get("employee_id")));
        target.setEmployeeName(str(emp.get("employee_name")));
        target.setEmployeeNo(str(emp.get("employee_no")));
        target.setDeptId(toLong(emp.get("org_id")));
        target.setDeptName(deptName(emp.get("org_id")));
    }

    private void setEmployee(HrOuting target, Map<String, Object> emp)
    {
        target.setEmployeeId(toLong(emp.get("employee_id")));
        target.setEmployeeName(str(emp.get("employee_name")));
        target.setEmployeeNo(str(emp.get("employee_no")));
        target.setDeptId(toLong(emp.get("org_id")));
        target.setDeptName(deptName(emp.get("org_id")));
    }

    private void setEmployee(HrOvertime target, Map<String, Object> emp)
    {
        target.setEmployeeId(toLong(emp.get("employee_id")));
        target.setEmployeeName(str(emp.get("employee_name")));
        target.setEmployeeNo(str(emp.get("employee_no")));
        target.setDeptId(toLong(emp.get("org_id")));
        target.setDeptName(deptName(emp.get("org_id")));
    }

    private void setEmployee(HrLeaveRequest target, Map<String, Object> emp)
    {
        target.setEmployeeId(toLong(emp.get("employee_id")));
        target.setEmployeeName(str(emp.get("employee_name")));
        target.setEmployeeNo(str(emp.get("employee_no")));
        target.setDeptId(toLong(emp.get("org_id")));
        target.setDeptName(deptName(emp.get("org_id")));
    }

    private void setEmployee(HrClockRepair target, Map<String, Object> emp)
    {
        target.setEmployeeId(toLong(emp.get("employee_id")));
        target.setEmployeeName(str(emp.get("employee_name")));
        target.setEmployeeNo(str(emp.get("employee_no")));
        target.setDeptId(toLong(emp.get("org_id")));
        target.setDeptName(deptName(emp.get("org_id")));
    }

    /** 按班次名称查考勤模块班次配置 */
    private ShiftInfo findShiftByName(String name)
    {
        if (StringUtils.isEmpty(name))
        {
            return null;
        }
        List<Map<String, Object>> list = jdbcTemplate().queryForList(
                "SELECT shift_id, shift_name, start_time, end_time FROM biz_hr_shift "
                        + "WHERE shift_name = ? AND status = '0' AND del_flag = '0' LIMIT 1", name);
        return list.isEmpty() ? null : toShift(list.get(0));
    }

    private ShiftInfo findShiftById(Long shiftId)
    {
        if (shiftId == null)
        {
            return null;
        }
        List<Map<String, Object>> list = jdbcTemplate().queryForList(
                "SELECT shift_id, shift_name, start_time, end_time FROM biz_hr_shift "
                        + "WHERE shift_id = ? AND status = '0' AND del_flag = '0' LIMIT 1", shiftId);
        return list.isEmpty() ? null : toShift(list.get(0));
    }

    private ShiftInfo toShift(Map<String, Object> row)
    {
        ShiftInfo s = new ShiftInfo();
        s.id = toLong(row.get("shift_id"));
        s.name = str(row.get("shift_name"));
        s.startMin = parseHm(row.get("start_time"));
        s.endMin = parseHm(row.get("end_time"));
        return s;
    }

    private HrAttendanceRecord findRecord(Long employeeId, Date recordDate)
    {
        List<Long> ids = jdbcTemplate().queryForList(
                "SELECT record_id FROM biz_hr_attendance_record WHERE employee_id = ? AND record_date = ? "
                        + "AND (del_flag IS NULL OR del_flag = '0') LIMIT 1",
                Long.class, employeeId, day(recordDate));
        if (ids.isEmpty())
        {
            return null;
        }
        return attendanceRecordMapper.selectHrAttendanceRecordById(ids.get(0));
    }

    /* ==================== 出差/外出 → 考勤记录回写 ==================== */

    /** 出差办结：按出差区间把班次上下班时间写入考勤记录（来源=出差） */
    private void writeTripAttendance(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT employee_id, employee_no, employee_name, dept_id, departure_date, "
                        + "COALESCE(actual_return_date, expected_return_date) AS end_date "
                        + "FROM biz_hr_business_trip WHERE flow_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            return;
        }
        Map<String, Object> trip = rows.get(0);
        Date start = toDate(trip.get("departure_date"));
        Date end = toDate(trip.get("end_date"));
        if (StringUtils.isEmpty(str(trip.get("employee_no"))) || start == null || end == null || end.before(start))
        {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(day(start));
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(day(end));
        int covered = 0;
        while (!cal.after(endCal))
        {
            Date current = cal.getTime();
            ShiftInfo shift = findScheduleShift(str(trip.get("employee_no")), sdf.format(current));
            if (shift == null)
            {
                shift = findDefaultShift();
            }
            Date clockIn = shift == null || shift.startMin < 0 ? null : atMinutes(current, shift.startMin);
            Date clockOut = shift == null || shift.endMin < 0 ? null : atMinutes(current, shift.endMin);
            if (upsertAttendanceCoverage(toLong(trip.get("employee_id")), str(trip.get("employee_no")),
                    str(trip.get("employee_name")), toLong(trip.get("dept_id")), current, clockIn, clockOut,
                    CLOCK_SOURCE_TRIP, "出差流程办结补充"))
            {
                covered++;
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        log.info("出差考勤回写完成 instanceId={} 覆盖天数={}", instanceId, covered);
    }

    /** 外出办结：按外出起止时间写入当日考勤记录（来源=外出） */
    private void writeOutingAttendance(String instanceId)
    {
        List<Map<String, Object>> rows = jdbcTemplate().queryForList(
                "SELECT employee_id, employee_no, employee_name, dept_id, outing_date, start_time, end_time "
                        + "FROM biz_hr_outing WHERE flow_instance_id = ? LIMIT 1", instanceId);
        if (rows.isEmpty())
        {
            return;
        }
        Map<String, Object> outing = rows.get(0);
        Date outingDate = toDate(outing.get("outing_date"));
        if (StringUtils.isEmpty(str(outing.get("employee_no"))) || outingDate == null)
        {
            return;
        }
        int startMin = parseHm(outing.get("start_time"));
        int endMin = parseHm(outing.get("end_time"));
        Date clockIn = startMin < 0 ? null : atMinutes(outingDate, startMin);
        Date clockOut = endMin < 0 ? null : atMinutes(outingDate, endMin);
        upsertAttendanceCoverage(toLong(outing.get("employee_id")), str(outing.get("employee_no")),
                str(outing.get("employee_name")), toLong(outing.get("dept_id")), outingDate, clockIn, clockOut,
                CLOCK_SOURCE_OUTING, "外出流程办结补充");
        log.info("外出考勤回写完成 instanceId={} date={}", instanceId, new SimpleDateFormat("yyyy-MM-dd").format(outingDate));
    }

    /**
     * 写入/补充某天的考勤打卡：无记录则新建，有记录则只补缺失卡位（幂等）
     *
     * 返回 true 表示当天考勤记录有变更。
     */
    private boolean upsertAttendanceCoverage(Long employeeId, String employeeNo, String employeeName, Long orgId,
            Date recordDate, Date clockIn, Date clockOut, String source, String remark)
    {
        if (employeeId == null || recordDate == null)
        {
            return false;
        }
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(recordDate);
        HrAttendanceRecord record = findRecord(employeeId, recordDate);
        if (record == null)
        {
            ShiftInfo shift = findScheduleShift(employeeNo, dateStr);
            if (shift == null)
            {
                shift = findDefaultShift();
            }
            record = new HrAttendanceRecord();
            record.setRecordDate(day(recordDate));
            record.setEmployeeId(employeeId);
            record.setEmployeeName(employeeName);
            record.setEmployeeNo(employeeNo);
            record.setOrgId(orgId);
            record.setClockIn(clockIn);
            record.setClockOut(clockOut);
            record.setClockInSource(clockIn == null ? null : source);
            record.setClockOutSource(clockOut == null ? null : source);
            record.setDelFlag("0");
            record.setRemark(remark);
            record.setCreateBy("flowArchive");
            if (shift != null)
            {
                record.setShiftId(shift.id);
                record.setShiftName(shift.name);
            }
            recalcAttendance(record, shift);
            attendanceRecordMapper.insertHrAttendanceRecord(record);
            return true;
        }
        boolean changed = false;
        if (record.getClockIn() == null && clockIn != null)
        {
            record.setClockIn(clockIn);
            record.setClockInSource(source);
            changed = true;
        }
        if (record.getClockOut() == null && clockOut != null)
        {
            record.setClockOut(clockOut);
            record.setClockOutSource(source);
            changed = true;
        }
        if (!changed)
        {
            return false;
        }
        record.setUpdateBy("flowArchive");
        record.setRemark(StringUtils.isEmpty(record.getRemark()) ? remark : record.getRemark() + "；" + remark);
        ShiftInfo shift = findShiftById(record.getShiftId());
        if (shift == null)
        {
            shift = findDefaultShift();
        }
        recalcAttendance(record, shift);
        attendanceRecordMapper.updateHrAttendanceRecord(record);
        return true;
    }

    /** 查员工某天排班对应的班次 */
    private ShiftInfo findScheduleShift(String employeeNo, String dateStr)
    {
        if (StringUtils.isEmpty(employeeNo))
        {
            return null;
        }
        List<Long> ids = jdbcTemplate().queryForList(
                "SELECT shift_id FROM biz_hr_schedule WHERE employee_no = ? AND schedule_date = ? "
                        + "AND (del_flag IS NULL OR del_flag = '0') LIMIT 1",
                Long.class, employeeNo, dateStr);
        return ids.isEmpty() ? null : findShiftById(ids.get(0));
    }

    /** 取默认班次 */
    private ShiftInfo findDefaultShift()
    {
        List<Map<String, Object>> list = jdbcTemplate().queryForList(
                "SELECT shift_id, shift_name, start_time, end_time FROM biz_hr_shift "
                        + "WHERE is_default = 1 AND (del_flag IS NULL OR del_flag = '0') LIMIT 1");
        return list.isEmpty() ? null : toShift(list.get(0));
    }

    /** 指定日期 + 当天第几分钟 → 精确到分钟的 Date */
    private Date atMinutes(Date date, int minutes)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day(date));
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /** 回写查重：同员工+同日期+同事项已存在则跳过（幂等） */
    private boolean dedup(String table, Long employeeId, String dateCol, Object dateVal, String itemCol, Object itemVal)
    {
        if (dateVal == null)
        {
            return false;
        }
        Integer cnt = jdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE employee_id = ? AND " + dateCol + " = ? AND "
                        + itemCol + " = ? AND del_flag = '0'",
                Integer.class, employeeId, dateVal, itemVal == null ? "" : itemVal.toString());
        return cnt != null && cnt > 0;
    }

    /** 最后一名实际办理审批人姓名 */
    private String lastApprover(String instanceId)
    {
        try
        {
            List<String> assignees = jdbcTemplate().queryForList(
                    "SELECT ASSIGNEE_ FROM act_hi_taskinst WHERE PROC_INST_ID_ = ? AND ASSIGNEE_ IS NOT NULL "
                            + "ORDER BY END_TIME_ DESC LIMIT 1", String.class, instanceId);
            if (assignees.isEmpty())
            {
                return "流程归档";
            }
            List<String> names = jdbcTemplate().queryForList(
                    "SELECT nick_name FROM sys_user WHERE user_id = ?", String.class, Long.valueOf(assignees.get(0)));
            return names.isEmpty() ? "流程归档" : names.get(0);
        }
        catch (Exception e)
        {
            return "流程归档";
        }
    }

    private String deptName(Object deptId)
    {
        Long id = toLong(deptId);
        if (id == null)
        {
            return null;
        }
        try
        {
            List<String> names = jdbcTemplate().queryForList(
                    "SELECT dept_name FROM sys_dept WHERE dept_id = ?", String.class, id);
            return names.isEmpty() ? null : names.get(0);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static Long toLong(Object v)
    {
        if (v == null) { return null; }
        if (v instanceof Number) { return ((Number) v).longValue(); }
        try { return Long.valueOf(v.toString().trim()); } catch (Exception e) { return null; }
    }

    private static BigDecimal toDecimal(Object v)
    {
        if (v == null) { return null; }
        if (v instanceof BigDecimal) { return (BigDecimal) v; }
        if (v instanceof Number) { return BigDecimal.valueOf(((Number) v).doubleValue()); }
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return null; }
    }

    private static String str(Object v)
    {
        return v == null ? null : v.toString();
    }

    private static Date toDate(Object v)
    {
        if (v == null) { return null; }
        // 统一归一化为 java.util.Date：java.sql.Date.toInstant() 会抛 UnsupportedOperationException
        if (v instanceof java.sql.Timestamp) { return new Date(((java.sql.Timestamp) v).getTime()); }
        if (v instanceof java.sql.Date) { return new Date(((java.sql.Date) v).getTime()); }
        if (v instanceof Date) { return (Date) v; }
        // JDBC 驱动（MySQL Connector/J 8.0.23+）getObject 对 DATETIME 可能返回 java.time 类型
        if (v instanceof LocalDateTime) { return Date.from(((LocalDateTime) v).atZone(ZoneId.systemDefault()).toInstant()); }
        if (v instanceof LocalDate) { return Date.from(((LocalDate) v).atStartOfDay(ZoneId.systemDefault()).toInstant()); }
        if (v instanceof LocalTime) { return Date.from(LocalDateTime.of(java.time.LocalDate.now(), (LocalTime) v).atZone(ZoneId.systemDefault()).toInstant()); }
        try
        {
            String s = v.toString();
            return new java.text.SimpleDateFormat(s.length() > 10 ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd").parse(s);
        }
        catch (Exception e) { return null; }
    }

    private static Date day(Date d)
    {
        return Date.from(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** 取 HH:mm 文本 */
    private static String hm(Date d)
    {
        return new java.text.SimpleDateFormat("HH:mm").format(d);
    }

    private static int toMinutes(Date d)
    {
        LocalTime t = d.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        return t.getHour() * 60 + t.getMinute();
    }

    /** 解析 HH:mm / HH:mm:ss 为当天分钟数，失败返回 -1 */
    private static int parseHm(Object v)
    {
        if (v == null) { return -1; }
        try
        {
            String s = v.toString().trim();
            String[] parts = s.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        }
        catch (Exception e) { return -1; }
    }

    /** 班次解析结果 */
    private static class ShiftInfo
    {
        Long id;
        String name;
        int startMin = -1;
        int endMin = -1;
    }
}
