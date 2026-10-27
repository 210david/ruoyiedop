package com.ruoyi.hr.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrAttendanceImportDTO;
import com.ruoyi.hr.domain.HrAttendanceRecord;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.domain.HrShift;
import com.ruoyi.hr.mapper.HrAttendanceRecordMapper;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.mapper.HrScheduleMapper;
import com.ruoyi.hr.mapper.HrShiftMapper;
import com.ruoyi.hr.service.IHrAttendanceRecordService;

@Service
public class HrAttendanceRecordServiceImpl implements IHrAttendanceRecordService
{
    @Autowired
    private HrAttendanceRecordMapper hrAttendanceRecordMapper;
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;
    @Autowired
    private HrScheduleMapper hrScheduleMapper;
    @Autowired
    private HrShiftMapper hrShiftMapper;

    @Override
    public HrAttendanceRecord selectHrAttendanceRecordById(Long recordId) { return hrAttendanceRecordMapper.selectHrAttendanceRecordById(recordId); }

    @Override
    public List<HrAttendanceRecord> selectHrAttendanceRecordList(HrAttendanceRecord hrAttendanceRecord) { return hrAttendanceRecordMapper.selectHrAttendanceRecordList(hrAttendanceRecord); }

    @Override
    public int insertHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord)
    {
        // 根据班次自动计算考勤状态、迟到/早退
        computeAttendanceResult(hrAttendanceRecord);
        return hrAttendanceRecordMapper.insertHrAttendanceRecord(hrAttendanceRecord);
    }

    @Override
    public int updateHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord)
    {
        // 根据班次自动计算考勤状态、迟到/早退
        computeAttendanceResult(hrAttendanceRecord);
        return hrAttendanceRecordMapper.updateHrAttendanceRecord(hrAttendanceRecord);
    }

    @Override
    public int deleteHrAttendanceRecordByIds(Long[] recordIds) { return hrAttendanceRecordMapper.deleteHrAttendanceRecordByIds(recordIds); }

    @Override
    public int deleteHrAttendanceRecordById(Long recordId) { return hrAttendanceRecordMapper.deleteHrAttendanceRecordById(recordId); }

    /**
     * 考勤机原始数据导入
     *
     * 核心逻辑：
     * 1. 按工号+日期分组，取最早打卡为上班打卡、最晚打卡为下班打卡
     * 2. 按工号自动匹配员工档案
     * 3. 自动从排班表匹配班次信息
     * 4. 判重：同一天同一工号已存在记录时，按 updateSupport 决定跳过或更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importAttendance(List<HrAttendanceImportDTO> importList, Boolean updateSupport, String operName)
    {
        if (StringUtils.isNull(importList) || importList.isEmpty())
        {
            return AjaxResult.error("导入考勤数据不能为空！");
        }

        int successNum = 0;
        int failureNum = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        // 结构化明细列表
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();

        // 1. 按工号+日期分组
        Map<String, List<HrAttendanceImportDTO>> groupedMap = new LinkedHashMap<>();
        for (HrAttendanceImportDTO row : importList)
        {
            String key = groupKey(row, dateTimeFormat);
            if (key == null)
            {
                // 工号或打卡时间为空的行也记录为失败
                Map<String, Object> failItem = new LinkedHashMap<>();
                failItem.put("employeeNo", row.getEmployeeNo() != null ? row.getEmployeeNo() : "");
                failItem.put("employeeName", row.getEmployeeName() != null ? row.getEmployeeName() : "");
                failItem.put("clockTime", row.getClockTime() != null ? dateTimeFormat.format(row.getClockTime()) : "");
                failItem.put("machineNo", row.getMachineNo() != null ? row.getMachineNo() : "");
                failItem.put("errorReason", "工号或打卡时间为空");
                failureList.add(failItem);
                failureNum++;
                continue;
            }
            groupedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        for (Map.Entry<String, List<HrAttendanceImportDTO>> entry : groupedMap.entrySet())
        {
            String key = entry.getKey();
            List<HrAttendanceImportDTO> rows = entry.getValue();
            HrAttendanceImportDTO first = rows.get(0);
            String employeeNo = first.getEmployeeNo();
            String employeeName = first.getEmployeeName();
            String datePart = key.contains("|") ? key.substring(key.indexOf('|') + 1) : key;
            try
            {
                if (StringUtils.isEmpty(employeeNo))
                {
                    throw new RuntimeException("工号不能为空");
                }

                // 2. 按工号匹配员工档案
                HrEmployee employee = hrEmployeeMapper.selectHrEmployeeByEmployeeNo(employeeNo);
                if (employee == null)
                {
                    throw new RuntimeException("工号 " + employeeNo + " 未找到对应员工档案");
                }

                // 3. 按打卡时间排序，取最早和最晚
                rows.sort(Comparator.comparing(HrAttendanceImportDTO::getClockTime));
                Date earliestClock = rows.get(0).getClockTime();
                Date latestClock = rows.get(rows.size() - 1).getClockTime();
                String recordDateStr = dateFormat.format(earliestClock);

                // 4. 自动匹配排班班次（提前到判重之前，确保更新和新增都能用）
                HrSchedule schedule = hrScheduleMapper.selectByEmployeeNoAndDate(employeeNo, recordDateStr);

                // 5. 判重：同一天同一工号
                HrAttendanceRecord existing = hrAttendanceRecordMapper.selectByEmployeeNoAndDate(employeeNo, recordDateStr);
                if (existing != null)
                {
                    if (updateSupport)
                    {
                        existing.setClockIn(earliestClock);
                        existing.setClockOut(latestClock);
                        existing.setClockInSource("0");
                        existing.setClockOutSource("0");
                        // 重新匹配班次
                        if (schedule != null)
                        {
                            existing.setShiftId(schedule.getShiftId());
                            existing.setShiftName(schedule.getShiftName());
                        }
                        else
                        {
                            HrShift defaultShift = hrShiftMapper.selectDefaultShift();
                            if (defaultShift != null)
                            {
                                existing.setShiftId(defaultShift.getShiftId());
                                existing.setShiftName(defaultShift.getShiftName());
                            }
                        }
                        // 重新计算考勤状态
                        computeAttendanceResult(existing);
                        existing.setUpdateBy(operName);
                        hrAttendanceRecordMapper.updateHrAttendanceRecord(existing);
                        successNum++;
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("employeeNo", employeeNo);
                        item.put("employeeName", employee.getEmployeeName());
                        item.put("recordDate", recordDateStr);
                        item.put("clockIn", timeFormat.format(earliestClock));
                        item.put("clockOut", timeFormat.format(latestClock));
                        item.put("result", "更新成功");
                        successList.add(item);
                    }
                    else
                    {
                        failureNum++;
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("employeeNo", employeeNo);
                        item.put("employeeName", employee.getEmployeeName());
                        item.put("clockTime", dateTimeFormat.format(earliestClock));
                        item.put("machineNo", first.getMachineNo() != null ? first.getMachineNo() : "");
                        item.put("errorReason", "已存在考勤记录，请勾选「更新已存在数据」覆盖");
                        failureList.add(item);
                    }
                    continue;
                }

                // 6. 构建考勤记录
                HrAttendanceRecord record = new HrAttendanceRecord();
                record.setRecordDate(earliestClock);
                record.setEmployeeId(employee.getEmployeeId());
                record.setEmployeeName(employee.getEmployeeName());
                record.setEmployeeNo(employeeNo);
                record.setOrgId(employee.getOrgId());
                record.setClockIn(earliestClock);
                record.setClockOut(latestClock);
                record.setClockInSource("0");
                record.setClockOutSource("0");
                record.setAttendanceStatus("0");
                record.setLateMinutes(0);
                record.setEarlyMinutes(0);
                record.setOvertimeHours(java.math.BigDecimal.ZERO);
                record.setNightShift(0);
                record.setDelFlag("0");
                record.setCreateBy(operName);
                record.setRemark("考勤机导入");

                if (schedule != null)
                {
                    record.setShiftId(schedule.getShiftId());
                    record.setShiftName(schedule.getShiftName());
                }
                else
                {
                    HrShift defaultShift = hrShiftMapper.selectDefaultShift();
                    if (defaultShift != null)
                    {
                        record.setShiftId(defaultShift.getShiftId());
                        record.setShiftName(defaultShift.getShiftName());
                    }
                }

                // 7. 根据班次标准时间计算考勤状态、迟到/早退分钟
                computeAttendanceResult(record);

                hrAttendanceRecordMapper.insertHrAttendanceRecord(record);
                successNum++;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("employeeNo", employeeNo);
                item.put("employeeName", employee.getEmployeeName());
                item.put("recordDate", recordDateStr);
                item.put("clockIn", timeFormat.format(earliestClock));
                item.put("clockOut", timeFormat.format(latestClock));
                item.put("result", "导入成功");
                successList.add(item);
            }
            catch (Exception e)
            {
                failureNum++;
                // 提取简短错误信息
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 150)
                {
                    errMsg = errMsg.substring(0, 150) + "...";
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("employeeNo", employeeNo != null ? employeeNo : "");
                item.put("employeeName", employeeName != null ? employeeName : "");
                item.put("clockTime", !rows.isEmpty() && rows.get(0).getClockTime() != null ? dateTimeFormat.format(rows.get(0).getClockTime()) : datePart);
                item.put("machineNo", first.getMachineNo() != null ? first.getMachineNo() : "");
                item.put("errorReason", errMsg != null ? errMsg : "未知错误");
                failureList.add(item);
            }
        }

        // 构建返回结果
        AjaxResult ajax = AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0 && successNum == 0)
        {
            ajax.put("code", 500);
            ajax.put("msg", "导入失败，共 " + failureNum + " 条数据异常");
        }
        else if (failureNum > 0)
        {
            ajax.put("msg", "部分导入成功：成功 " + successNum + " 条，失败 " + failureNum + " 条");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条");
        }
        return ajax;
    }

    /**
     * 根据班次标准时间计算考勤状态、迟到分钟、早退分钟
     *
     * 考勤记录只反映打卡事实，不计算旷工（旷工是报表层面的事）
     * 状态值：0=正常 1=迟到 2=早退 4=漏卡
     *
     * 规则：
     * - 两次打卡都有：比较时间判断迟到/早退/正常
     * - 只有一次打卡：标记为漏卡，并根据班次时间智能区分上班/下班
     * - 无打卡：不处理（不会产生考勤记录，旷工在月报统计时计算）
     * - 迟到宽限5分钟，早退宽限5分钟
     */
    /** 公开方法：供补卡审批等外部调用计算考勤状态 */
    public void computeAttendanceResultPublic(HrAttendanceRecord record)
    {
        computeAttendanceResult(record);
    }

    private void computeAttendanceResult(HrAttendanceRecord record)
    {
        if (record == null || record.getShiftId() == null)
        {
            return;
        }

        // 获取班次信息
        HrShift shift = hrShiftMapper.selectHrShiftById(record.getShiftId());
        if (shift == null || StringUtils.isEmpty(shift.getStartTime()) || StringUtils.isEmpty(shift.getEndTime()))
        {
            return;
        }

        // 班次标准上下班时间（HH:mm 格式）
        int standardIn = toMinutes(shift.getStartTime());
        int standardOut = toMinutes(shift.getEndTime());

        // 打卡时间（Date 类型）
        Date clockIn = record.getClockIn();
        Date clockOut = record.getClockOut();

        // 宽限分钟数（5分钟内不计迟到/早退）
        int graceMinutes = 5;

        // 无打卡记录时不计算状态（考勤记录只记录打卡事实，旷工在月报统计）
        boolean noPunch = (clockIn == null && clockOut == null);
        if (noPunch)
        {
            record.setAttendanceStatus("0");
            record.setLateMinutes(0);
            record.setEarlyMinutes(0);
            return;
        }

        // 判断是否只有一次打卡（clock_in == clock_out 或其中一个为null）
        boolean isSinglePunch = (clockIn != null && clockOut != null && clockIn.equals(clockOut))
            || (clockIn != null && clockOut == null)
            || (clockIn == null && clockOut != null);

        if (isSinglePunch)
        {
            // 只有一次打卡：取非空的那个时间来判断是上班还是下班
            Date singlePunch = clockIn != null ? clockIn : clockOut;
            int punchMin = dateToMinutes(singlePunch);
            // 班次中间时间点（上班+下班)/2，早于中间点→上班打卡，晚于→下班打卡
            int midPoint = (standardIn + standardOut) / 2;

            if (punchMin <= midPoint)
            {
                // 靠近上班时间 → 这是上班打卡，下班未打卡
                record.setClockIn(singlePunch);
                record.setClockOut(null);
                // 如果上班迟到也算迟到
                int diff = punchMin - standardIn;
                record.setLateMinutes(diff > graceMinutes ? diff : 0);
                record.setEarlyMinutes(0);
            }
            else
            {
                // 靠近下班时间 → 这是下班打卡，上班未打卡
                record.setClockIn(null);
                record.setClockOut(singlePunch);
                record.setLateMinutes(0);
                // 如果下班早退也算早退
                int diff = standardOut - punchMin;
                record.setEarlyMinutes(diff > graceMinutes ? diff : 0);
            }
            record.setAttendanceStatus("4"); // 漏卡
            return;
        }

        // 正常两次打卡的情况
        int late = 0;
        int early = 0;
        if (clockIn != null)
        {
            int clockInMin = dateToMinutes(clockIn);
            int diff = clockInMin - standardIn;
            if (diff > graceMinutes)
            {
                late = diff;
            }
        }

        if (clockOut != null)
        {
            int clockOutMin = dateToMinutes(clockOut);
            int diff = standardOut - clockOutMin;
            if (diff > graceMinutes)
            {
                early = diff;
            }
        }

        record.setLateMinutes(late);
        record.setEarlyMinutes(early);

        // 判定考勤状态
        if (late > 0 && early > 0)
        {
            record.setAttendanceStatus("1"); // 迟到（含迟到+早退）
        }
        else if (late > 0)
        {
            record.setAttendanceStatus("1"); // 迟到
        }
        else if (early > 0)
        {
            record.setAttendanceStatus("2"); // 早退
        }
        else if (clockIn != null && clockOut != null)
        {
            record.setAttendanceStatus("0"); // 正常
        }
        else
        {
            record.setAttendanceStatus("4"); // 漏卡
        }
    }

    /** 将 HH:mm 格式字符串转为当天的分钟数 */
    private int toMinutes(String timeStr)
    {
        if (StringUtils.isEmpty(timeStr) || !timeStr.contains(":"))
        {
            return -1;
        }
        String[] parts = timeStr.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return h * 60 + m;
    }

    /** 将 Date 转为当天的分钟数（小时*60+分钟） */
    private int dateToMinutes(Date date)
    {
        if (date == null) return -1;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    /**
     * 批量重算考勤状态：根据班次重新计算所有记录的考勤状态、迟到/早退分钟
     */
    @Override
    public int recalculateAttendanceStatus(HrAttendanceRecord query)
    {
        List<HrAttendanceRecord> list = hrAttendanceRecordMapper.selectHrAttendanceRecordList(query);
        int count = 0;
        for (HrAttendanceRecord record : list)
        {
            computeAttendanceResult(record);
            record.setUpdateBy("system-recalculate");
            hrAttendanceRecordMapper.updateHrAttendanceRecord(record);
            count++;
        }
        return count;
    }

    /**
     * 生成分组键：工号 + 日期
     * 如果工号或打卡时间为空则返回 null（跳过该行）
     */
    private String groupKey(HrAttendanceImportDTO row, SimpleDateFormat dateTimeFormat)
    {
        if (StringUtils.isEmpty(row.getEmployeeNo()) || row.getClockTime() == null)
        {
            return null;
        }
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(row.getClockTime());
        return row.getEmployeeNo() + "|" + dateStr;
    }
}
