package com.ruoyi.hr.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrLeaveBalance;
import com.ruoyi.hr.domain.HrLeaveBalanceLog;
import com.ruoyi.hr.domain.HrLeaveRequest;
import com.ruoyi.hr.domain.HrLeaveRequestAuditLog;
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.domain.HrShift;
import com.ruoyi.hr.mapper.HrLeaveBalanceLogMapper;
import com.ruoyi.hr.mapper.HrLeaveBalanceMapper;
import com.ruoyi.hr.mapper.HrLeaveRequestAuditLogMapper;
import com.ruoyi.hr.mapper.HrLeaveRequestMapper;
import com.ruoyi.hr.mapper.HrScheduleMapper;
import com.ruoyi.hr.mapper.HrShiftMapper;
import com.ruoyi.hr.service.IHrLeaveRequestService;

@Service
public class HrLeaveRequestServiceImpl implements IHrLeaveRequestService
{
    @Autowired
    private HrLeaveRequestMapper hrLeaveRequestMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;
    @Autowired
    private HrLeaveBalanceMapper hrLeaveBalanceMapper;
    @Autowired
    private HrLeaveBalanceLogMapper hrLeaveBalanceLogMapper;
    @Autowired
    private HrScheduleMapper hrScheduleMapper;
    @Autowired
    private HrShiftMapper hrShiftMapper;
    @Autowired
    private HrLeaveRequestAuditLogMapper hrLeaveRequestAuditLogMapper;

    @Override
    public HrLeaveRequest selectHrLeaveRequestById(Long leaveReqId)
    {
        HrLeaveRequest leave = hrLeaveRequestMapper.selectHrLeaveRequestById(leaveReqId);
        if (leave != null)
        {
            leave.setAuditLogList(hrLeaveRequestAuditLogMapper.selectHrLeaveRequestAuditLogByLeaveReqId(leaveReqId));
        }
        return leave;
    }

    @Override
    public List<HrLeaveRequest> selectHrLeaveRequestList(HrLeaveRequest hrLeaveRequest) { return hrLeaveRequestMapper.selectHrLeaveRequestList(hrLeaveRequest); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertHrLeaveRequest(HrLeaveRequest hrLeaveRequest)
    {
        if (StringUtils.isEmpty(hrLeaveRequest.getLeaveReqNo()))
        {
            hrLeaveRequest.setLeaveReqNo(mkNumberRuleService.generateNumber("hr_leave_request"));
        }
        computeLeaveDays(hrLeaveRequest);
        // 新建请假单默认待审批
        if (StringUtils.isEmpty(hrLeaveRequest.getAuditStatus()))
        {
            hrLeaveRequest.setAuditStatus("0");
        }
        int rows = hrLeaveRequestMapper.insertHrLeaveRequest(hrLeaveRequest);
        // 发起请假时冻结假期余额
        if (rows > 0 && hrLeaveRequest.getLeaveDays() != null && hrLeaveRequest.getLeaveDays().compareTo(BigDecimal.ZERO) > 0)
        {
            freezeLeaveBalance(hrLeaveRequest);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateHrLeaveRequest(HrLeaveRequest hrLeaveRequest)
    {
        // 驳回后修改重新提交：状态重置为待审批，清空审批人/审批时间/审批意见
        if ("2".equals(hrLeaveRequest.getAuditStatus()))
        {
            hrLeaveRequest.setAuditStatus("0");
            hrLeaveRequest.setAuditBy(null);
            hrLeaveRequest.setAuditTime(null);
            hrLeaveRequest.setAuditRemark(null);
            // 清空旧字符串需要用空串覆盖（MyBatis动态SQL的空判断）
            hrLeaveRequest.setAuditRemark("");
            // 重新提交时重新冻结（旧的冻结在驳回时已解冻）
            computeLeaveDays(hrLeaveRequest);
            int rows = hrLeaveRequestMapper.updateHrLeaveRequest(hrLeaveRequest);
            if (rows > 0 && hrLeaveRequest.getLeaveDays() != null && hrLeaveRequest.getLeaveDays().compareTo(BigDecimal.ZERO) > 0)
            {
                freezeLeaveBalance(hrLeaveRequest);
            }
            return rows;
        }
        computeLeaveDays(hrLeaveRequest);
        return hrLeaveRequestMapper.updateHrLeaveRequest(hrLeaveRequest);
    }

    /**
     * 根据开始/结束时间计算请假天数（考虑排班休息时间）
     * 规则：
     * - 查询员工在请假时间范围内每天的排班
     * - 排班为休息日（无排班或shiftId=0）的，不计入请假天数
     * - 排班为工作日的，按实际请假小时占当天工作小时的比例折算
     * - ≥4小时算1天，不足4小时算0.5天
     * - 跨天累加，最低0.5天
     */
    public void computeLeaveDays(HrLeaveRequest leave)
    {
        if (leave.getStartTime() == null || leave.getEndTime() == null
                || leave.getEndTime().before(leave.getStartTime()))
        {
            return;
        }
        // 如果员工ID为空，退回简单计算
        if (leave.getEmployeeId() == null)
        {
            simpleComputeLeaveDays(leave);
            return;
        }
        try
        {
            BigDecimal days = computeLeaveDaysWithSchedule(leave.getEmployeeId(), leave.getStartTime(), leave.getEndTime());
            leave.setLeaveDays(days);
        }
        catch (Exception e)
        {
            // 排班查询出错时退回简单计算
            simpleComputeLeaveDays(leave);
        }
    }

    /**
     * 简单计算请假天数（不考虑排班，仅按自然时间）
     */
    private void simpleComputeLeaveDays(HrLeaveRequest leave)
    {
        long millis = leave.getEndTime().getTime() - leave.getStartTime().getTime();
        long fullDays = millis / 86400000L;
        long remainHours = (millis % 86400000L) / 3600000L;
        BigDecimal extra;
        if (remainHours >= 4) { extra = BigDecimal.ONE; }
        else if (remainHours > 0) { extra = new BigDecimal("0.5"); }
        else { extra = BigDecimal.ZERO; }
        BigDecimal days = new BigDecimal(fullDays).add(extra);
        if (days.compareTo(new BigDecimal("0.5")) < 0) { days = new BigDecimal("0.5"); }
        leave.setLeaveDays(days);
    }

    /**
     * 结合排班计算请假天数
     * 算法：遍历请假范围内的每一天，查询当天排班
     * - 无排班或休息日 → 跳过（不计入请假天数）
     * - 有排班 → 计算当天请假时间段与班次时间段的重叠小时
     *   - 重叠 ≥4小时 → 1天
     *   - 重叠 >0且<4小时 → 0.5天
     *   - 重叠 =0 → 0天
     */
    private BigDecimal computeLeaveDaysWithSchedule(Long employeeId, Date startTime, Date endTime)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        BigDecimal totalDays = BigDecimal.ZERO;

        while (!cal.getTime().after(endTime))
        {
            String dateStr = sdf.format(cal.getTime());
            // 查询当天排班
            HrSchedule schedule = null;
            try
            {
                List<HrSchedule> schedules = hrScheduleMapper.selectExistingSchedule(
                        employeeId, dateStr, dateStr);
                if (schedules != null && !schedules.isEmpty())
                {
                    schedule = schedules.get(0);
                }
            }
            catch (Exception e)
            {
                // 排班表不存在或查询异常，按工作日处理
            }

            // 无排班或休息日（shiftId为空或0），跳过
            if (schedule == null || schedule.getShiftId() == null || schedule.getShiftId() == 0)
            {
                // 无排班 = 休息日，不计入请假
                cal.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            // 查询班次信息获取工作时间
            HrShift shift = hrShiftMapper.selectHrShiftById(schedule.getShiftId());
            if (shift == null || StringUtils.isEmpty(shift.getStartTime()) || StringUtils.isEmpty(shift.getEndTime()))
            {
                // 无班次信息，按1天算
                totalDays = totalDays.add(BigDecimal.ONE);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            // 计算当天请假时间段与班次时间段的重叠小时
            double overlapHours = calculateOverlapHours(cal.getTime(), startTime, endTime,
                    shift.getStartTime(), shift.getEndTime(), shift.getCrossMidnight());

            if (overlapHours >= 4)
            {
                totalDays = totalDays.add(BigDecimal.ONE);
            }
            else if (overlapHours > 0)
            {
                totalDays = totalDays.add(new BigDecimal("0.5"));
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 最低0.5天
        if (totalDays.compareTo(BigDecimal.ZERO) == 0)
        {
            // 如果整个请假范围内都是休息日，至少算0.5天
            totalDays = new BigDecimal("0.5");
        }
        return totalDays;
    }

    /**
     * 计算请假时间段与当天班次时间段的重叠小时数
     */
    private double calculateOverlapHours(Date day, Date leaveStart, Date leaveEnd,
                                         String shiftStartStr, String shiftEndStr, Integer crossMidnight)
    {
        try
        {
            SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
            String dayStr = dayFmt.format(day);

            // 构建当天班次的开始和结束时间
            SimpleDateFormat fullFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date shiftStart = fullFmt.parse(dayStr + " " + shiftStartStr);

            Date shiftEnd;
            if (crossMidnight != null && crossMidnight == 1)
            {
                // 跨零点：结束时间在第二天
                Calendar nextDay = Calendar.getInstance();
                nextDay.setTime(day);
                nextDay.add(Calendar.DAY_OF_MONTH, 1);
                shiftEnd = fullFmt.parse(dayFmt.format(nextDay.getTime()) + " " + shiftEndStr);
            }
            else
            {
                shiftEnd = fullFmt.parse(dayStr + " " + shiftEndStr);
            }

            // 计算重叠
            long overlapStart = Math.max(leaveStart.getTime(), shiftStart.getTime());
            long overlapEnd = Math.min(leaveEnd.getTime(), shiftEnd.getTime());

            if (overlapEnd <= overlapStart)
            {
                return 0;
            }
            return (overlapEnd - overlapStart) / 3600000.0;
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteHrLeaveRequestByIds(Long[] leaveReqIds)
    {
        // 删除前先解冻待审批的请假冻结额
        for (Long leaveReqId : leaveReqIds)
        {
            HrLeaveRequest leave = hrLeaveRequestMapper.selectHrLeaveRequestById(leaveReqId);
            if (leave != null && "0".equals(leave.getAuditStatus()))
            {
                unfreezeLeaveBalance(leave);
            }
            hrLeaveRequestAuditLogMapper.deleteHrLeaveRequestAuditLogByLeaveReqId(leaveReqId);
        }
        return hrLeaveRequestMapper.deleteHrLeaveRequestByIds(leaveReqIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteHrLeaveRequestById(Long leaveReqId)
    {
        HrLeaveRequest leave = hrLeaveRequestMapper.selectHrLeaveRequestById(leaveReqId);
        if (leave != null && "0".equals(leave.getAuditStatus()))
        {
            unfreezeLeaveBalance(leave);
        }
        hrLeaveRequestAuditLogMapper.deleteHrLeaveRequestAuditLogByLeaveReqId(leaveReqId);
        return hrLeaveRequestMapper.deleteHrLeaveRequestById(leaveReqId);
    }

    /**
     * 请假审批
     * auditAction: 1=通过, 2=驳回
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditLeaveRequest(Long leaveReqId, String auditAction, String auditRemark)
    {
        HrLeaveRequest leave = hrLeaveRequestMapper.selectHrLeaveRequestById(leaveReqId);
        if (leave == null)
        {
            throw new ServiceException("请假单不存在");
        }
        if (!"1".equals(auditAction) && !"2".equals(auditAction))
        {
            throw new ServiceException("审核动作不合法");
        }
        if (!"0".equals(leave.getAuditStatus()))
        {
            throw new ServiceException("该请假单已审批，不可重复审批" + ("2".equals(leave.getAuditStatus()) ? "，请修改后重新提交" : ""));
        }

        String username = SecurityUtils.getUsername();
        Date auditTime = new Date();
        leave.setAuditStatus("1".equals(auditAction) ? "1" : "2");
        leave.setAuditBy(username);
        leave.setAuditTime(auditTime);
        leave.setAuditRemark(auditRemark);

        int rows = hrLeaveRequestMapper.updateHrLeaveRequest(leave);

        // 写入审核日志
        if (rows > 0)
        {
            HrLeaveRequestAuditLog log = new HrLeaveRequestAuditLog();
            log.setLeaveReqId(leaveReqId);
            log.setAuditAction(auditAction);
            log.setAuditBy(username);
            log.setAuditTime(auditTime);
            log.setAuditRemark(auditRemark);
            hrLeaveRequestAuditLogMapper.insertHrLeaveRequestAuditLog(log);
        }

        // 审批通过：解冻并扣减假期余额
        if (rows > 0 && "1".equals(auditAction))
        {
            // 先解冻待审批期间的冻结额
            unfreezeLeaveBalance(leave);
            // 再扣减实际已休天数
            deductLeaveBalance(leave);
        }

        // 审批驳回：解冻假期余额
        if (rows > 0 && "2".equals(auditAction))
        {
            unfreezeLeaveBalance(leave);
        }

        return rows;
    }

    /**
     * 冻结假期余额（请假发起时调用）
     * 将请假天数加到 frozenDays 上
     */
    private void freezeLeaveBalance(HrLeaveRequest leave)
    {
        HrLeaveBalance balance = getLeaveBalance(leave);
        if (balance == null) return;

        BigDecimal beforeFrozen = balance.getFrozenDays() != null ? balance.getFrozenDays() : BigDecimal.ZERO;
        BigDecimal afterFrozen = beforeFrozen.add(leave.getLeaveDays());

        balance.setFrozenDays(afterFrozen);
        hrLeaveBalanceMapper.updateHrLeaveBalance(balance);

        // 记录冻结明细
        HrLeaveBalanceLog log = new HrLeaveBalanceLog();
        log.setBalanceId(balance.getBalanceId());
        log.setLeaveReqId(leave.getLeaveReqId());
        log.setLeaveReqNo(leave.getLeaveReqNo());
        log.setEmployeeId(leave.getEmployeeId());
        log.setEmployeeName(leave.getEmployeeName());
        log.setLeaveType(leave.getLeaveType());
        log.setYear(Calendar.getInstance().get(Calendar.YEAR));
        log.setChangeDays(leave.getLeaveDays());
        log.setBeforeFrozen(beforeFrozen);
        log.setAfterFrozen(afterFrozen);
        log.setChangeType("freeze");
        log.setChangeReason("请假发起冻结");
        log.setOperator(SecurityUtils.getUsername());
        log.setCreateBy(SecurityUtils.getUsername());
        hrLeaveBalanceLogMapper.insertHrLeaveBalanceLog(log);
    }

    /**
     * 解冻假期余额（审批驳回/删除时调用）
     * 将请假天数从 frozenDays 中扣除
     */
    private void unfreezeLeaveBalance(HrLeaveRequest leave)
    {
        HrLeaveBalance balance = getLeaveBalance(leave);
        if (balance == null) return;

        BigDecimal beforeFrozen = balance.getFrozenDays() != null ? balance.getFrozenDays() : BigDecimal.ZERO;
        if (beforeFrozen.compareTo(BigDecimal.ZERO) <= 0) return; // 无冻结额可解冻

        BigDecimal afterFrozen = beforeFrozen.subtract(leave.getLeaveDays());
        if (afterFrozen.compareTo(BigDecimal.ZERO) < 0) afterFrozen = BigDecimal.ZERO;

        balance.setFrozenDays(afterFrozen);
        hrLeaveBalanceMapper.updateHrLeaveBalance(balance);

        // 记录解冻明细
        HrLeaveBalanceLog log = new HrLeaveBalanceLog();
        log.setBalanceId(balance.getBalanceId());
        log.setLeaveReqId(leave.getLeaveReqId());
        log.setLeaveReqNo(leave.getLeaveReqNo());
        log.setEmployeeId(leave.getEmployeeId());
        log.setEmployeeName(leave.getEmployeeName());
        log.setLeaveType(leave.getLeaveType());
        log.setYear(Calendar.getInstance().get(Calendar.YEAR));
        log.setChangeDays(leave.getLeaveDays());
        log.setBeforeFrozen(beforeFrozen);
        log.setAfterFrozen(afterFrozen);
        log.setChangeType("unfreeze");
        log.setChangeReason("请假驳回/删除解冻");
        log.setOperator(SecurityUtils.getUsername());
        log.setCreateBy(SecurityUtils.getUsername());
        hrLeaveBalanceLogMapper.insertHrLeaveBalanceLog(log);
    }

    /**
     * 查询请假单对应的假期余额
     */
    private HrLeaveBalance getLeaveBalance(HrLeaveRequest leave)
    {
        HrLeaveBalance query = new HrLeaveBalance();
        query.setEmployeeId(leave.getEmployeeId());
        query.setLeaveType(leave.getLeaveType());
        query.setYear(Calendar.getInstance().get(Calendar.YEAR));
        List<HrLeaveBalance> balances = hrLeaveBalanceMapper.selectHrLeaveBalanceList(query);
        if (balances == null || balances.isEmpty()) return null;
        return balances.get(0);
    }

    /**
     * 扣减假期余额（审批通过时调用）
     */
    private void deductLeaveBalance(HrLeaveRequest leave)
    {
        // 查询对应假别的假期余额
        HrLeaveBalance query = new HrLeaveBalance();
        query.setEmployeeId(leave.getEmployeeId());
        query.setLeaveType(leave.getLeaveType());
        query.setYear(Calendar.getInstance().get(Calendar.YEAR));
        List<HrLeaveBalance> balances = hrLeaveBalanceMapper.selectHrLeaveBalanceList(query);

        if (balances == null || balances.isEmpty())
        {
            // 没有余额记录，不扣减（但审批仍然通过）
            return;
        }

        HrLeaveBalance balance = balances.get(0);
        BigDecimal beforeUsed = balance.getUsedDays() != null ? balance.getUsedDays() : BigDecimal.ZERO;
        BigDecimal beforeRemaining = balance.getRemainingDays() != null ? balance.getRemainingDays() : balance.getTotalDays();

        // 扣减
        BigDecimal afterUsed = beforeUsed.add(leave.getLeaveDays());
        BigDecimal afterRemaining = balance.getTotalDays() != null ? balance.getTotalDays().subtract(afterUsed) : BigDecimal.ZERO;

        balance.setUsedDays(afterUsed);
        balance.setRemainingDays(afterRemaining);
        hrLeaveBalanceMapper.updateHrLeaveBalance(balance);

        // 记录扣减明细
        HrLeaveBalanceLog log = new HrLeaveBalanceLog();
        log.setBalanceId(balance.getBalanceId());
        log.setLeaveReqId(leave.getLeaveReqId());
        log.setLeaveReqNo(leave.getLeaveReqNo());
        log.setEmployeeId(leave.getEmployeeId());
        log.setEmployeeName(leave.getEmployeeName());
        log.setLeaveType(leave.getLeaveType());
        log.setYear(Calendar.getInstance().get(Calendar.YEAR));
        log.setChangeDays(leave.getLeaveDays());
        log.setBeforeUsed(beforeUsed);
        log.setAfterUsed(afterUsed);
        log.setBeforeRemaining(beforeRemaining);
        log.setAfterRemaining(afterRemaining);
        log.setChangeType("deduct");
        log.setChangeReason("请假审批通过自动扣减");
        log.setOperator(SecurityUtils.getUsername());
        log.setCreateBy(SecurityUtils.getUsername());
        hrLeaveBalanceLogMapper.insertHrLeaveBalanceLog(log);
    }
}
