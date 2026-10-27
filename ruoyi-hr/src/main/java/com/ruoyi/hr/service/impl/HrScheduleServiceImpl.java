package com.ruoyi.hr.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.domain.HrScheduleTemplate;
import com.ruoyi.hr.domain.dto.HrScheduleBatchDTO;
import com.ruoyi.hr.mapper.HrScheduleMapper;
import com.ruoyi.hr.mapper.HrScheduleTemplateMapper;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.service.IHrScheduleService;

@Service
public class HrScheduleServiceImpl implements IHrScheduleService
{
    @Autowired
    private HrScheduleMapper hrScheduleMapper;
    @Autowired
    private HrScheduleTemplateMapper hrScheduleTemplateMapper;
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public HrSchedule selectHrScheduleById(Long scheduleId) { return hrScheduleMapper.selectHrScheduleById(scheduleId); }

    @Override
    public List<HrSchedule> selectHrScheduleList(HrSchedule hrSchedule) { return hrScheduleMapper.selectHrScheduleList(hrSchedule); }

    @Override
    public int insertHrSchedule(HrSchedule hrSchedule) { return hrScheduleMapper.insertHrSchedule(hrSchedule); }

    @Override
    public int updateHrSchedule(HrSchedule hrSchedule) { return hrScheduleMapper.updateHrSchedule(hrSchedule); }

    @Override
    public int deleteHrScheduleByIds(Long[] scheduleIds) { return hrScheduleMapper.deleteHrScheduleByIds(scheduleIds); }

    @Override
    public int deleteHrScheduleById(Long scheduleId) { return hrScheduleMapper.deleteHrScheduleById(scheduleId); }

    @Override
    public List<HrSchedule> selectScheduleCalendar(List<Long> employeeIds, Long orgId, String startDate, String endDate) {
        return hrScheduleMapper.selectScheduleCalendar(employeeIds, orgId, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int clearSchedule(List<Long> employeeIds, Long orgId, String startDate, String endDate) {
        return hrScheduleMapper.deleteScheduleByRange(employeeIds, orgId, startDate, endDate);
    }

    /**
     * 批量排班核心逻辑
     *
     * 支持两种模式：
     * 1. 模板排班：按轮转模板的班次序列循环生成排班
     * 2. 固定班次排班：每天使用相同的班次
     *
     * 支持选择人员方式：
     * 1. 直接指定员工ID列表
     * 2. 指定部门ID，自动获取该部门所有在职员工
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSchedule(HrScheduleBatchDTO dto, String operName)
    {
        // 1. 确定排班人员列表
        List<Long> employeeIds = dto.getEmployeeIds();
        if (employeeIds == null || employeeIds.isEmpty())
        {
            if (dto.getOrgId() != null)
            {
                // 按部门获取在职员工
                HrEmployee queryEmp = new HrEmployee();
                queryEmp.setOrgId(dto.getOrgId());
                List<HrEmployee> employees = hrEmployeeMapper.selectHrEmployeeList(queryEmp);
                employeeIds = new ArrayList<>();
                for (HrEmployee emp : employees)
                {
                    if (emp.getEmployeeStatus() == null || "0".equals(emp.getEmployeeStatus().toString()) || "1".equals(emp.getEmployeeStatus().toString()))
                    {
                        employeeIds.add(emp.getEmployeeId());
                    }
                }
            }
            if (employeeIds == null || employeeIds.isEmpty())
            {
                throw new RuntimeException("未找到可排班的员工");
            }
        }

        // 2. 解析日期范围
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate, endDate;
        try {
            startDate = sdf.parse(dto.getStartDate());
            endDate = sdf.parse(dto.getEndDate());
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误，要求 yyyy-MM-dd");
        }

        if (startDate.after(endDate)) {
            throw new RuntimeException("开始日期不能晚于结束日期");
        }

        // 3. 解析班次序列
        // 每个元素: {shiftId, shiftName, day}
        // shiftId=0 或 null 表示休息（不生成排班记录）
        List<Map<String, Object>> shiftSequence = new ArrayList<>();

        if (dto.getTemplateId() != null)
        {
            // 模板排班
            HrScheduleTemplate template = hrScheduleTemplateMapper.selectHrScheduleTemplateById(dto.getTemplateId());
            if (template == null)
            {
                throw new RuntimeException("排班模板不存在");
            }
            try {
                shiftSequence = objectMapper.readValue(template.getShiftSequence(),
                        new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                throw new RuntimeException("模板班次序列解析失败");
            }
        }
        else if (dto.getShiftId() != null)
        {
            // 固定班次排班，构造1天循环
            Map<String, Object> item = new HashMap<>();
            item.put("shiftId", dto.getShiftId());
            item.put("shiftName", dto.getShiftName() != null ? dto.getShiftName() : "");
            item.put("day", 1);
            shiftSequence.add(item);
        }
        else
        {
            throw new RuntimeException("请选择排班模板或班次");
        }

        int cycleDays = shiftSequence.size();
        if (cycleDays == 0)
        {
            throw new RuntimeException("班次序列不能为空");
        }

        // 4. 获取员工信息
        List<HrEmployee> employees = new ArrayList<>();
        for (Long empId : employeeIds)
        {
            HrEmployee emp = hrEmployeeMapper.selectHrEmployeeById(empId);
            if (emp != null)
            {
                employees.add(emp);
            }
        }
        if (employees.isEmpty())
        {
            throw new RuntimeException("未找到员工信息");
        }

        // 5. 预处理班次序列
        boolean overwrite = Boolean.TRUE.equals(dto.getOverwrite());
        boolean excludeWeekend = Boolean.TRUE.equals(dto.getExcludeWeekend());

        // 当排除周末时，模板中的"休息"天（shiftId=0）不再有意义（周末已经排除），
        // 应过滤掉，只保留有效班次天，避免"休息"天轮转到工作日上导致缺排班
        List<Map<String, Object>> effectiveSequence = new ArrayList<>();
        for (Map<String, Object> item : shiftSequence)
        {
            Object sidObj = item.get("shiftId");
            Long sid = null;
            if (sidObj instanceof Number) {
                sid = ((Number) sidObj).longValue();
            } else if (sidObj instanceof String) {
                try { sid = Long.parseLong((String) sidObj); } catch (Exception ignored) {}
            }
            if (sid != null && sid > 0)
            {
                effectiveSequence.add(item);
            }
            else
            {
                // 休息天：不排除周末时保留（作为模板周期的一部分），排除周末时丢弃
                if (!excludeWeekend)
                {
                    effectiveSequence.add(item);
                }
            }
        }
        int cycleLen = effectiveSequence.size();
        if (cycleLen == 0)
        {
            throw new RuntimeException("班次序列中没有有效班次");
        }

        // 6. 生成排班记录
        List<HrSchedule> toInsert = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        int dayIndex = 0;

        while (!cal.getTime().after(endDate))
        {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

            if (!(excludeWeekend && isWeekend))
            {
                // 当前在循环中的位置
                int seqIndex = dayIndex % cycleLen;
                Map<String, Object> shiftInfo = effectiveSequence.get(seqIndex);

                Object shiftIdObj = shiftInfo.get("shiftId");
                Long shiftId = null;
                if (shiftIdObj instanceof Number) {
                    shiftId = ((Number) shiftIdObj).longValue();
                } else if (shiftIdObj instanceof String) {
                    try { shiftId = Long.parseLong((String) shiftIdObj); } catch (Exception ignored) {}
                }

                String shiftName = shiftInfo.get("shiftName") != null ? shiftInfo.get("shiftName").toString() : "";

                // 休息日（shiftId=0）也生成排班记录，shiftName设为"休息"
                // 这样导出时能完整展示每天的排班情况
                boolean isRestDay = (shiftId == null || shiftId <= 0);
                String effectiveShiftName = isRestDay ? "休息" : shiftName;
                Long effectiveShiftId = isRestDay ? 0L : shiftId;

                String dateStr = sdf.format(cal.getTime());

                for (HrEmployee emp : employees)
                {
                    // 检查是否已存在排班
                    if (overwrite)
                    {
                        // 先删除已有的
                        List<HrSchedule> existing = hrScheduleMapper.selectExistingSchedule(emp.getEmployeeId(), dateStr, dateStr);
                        if (existing != null && !existing.isEmpty())
                        {
                            for (HrSchedule ex : existing)
                            {
                                hrScheduleMapper.deleteHrScheduleById(ex.getScheduleId());
                            }
                        }
                    }
                    else
                    {
                        // 不覆盖，跳过已存在的
                        List<HrSchedule> existing = hrScheduleMapper.selectExistingSchedule(emp.getEmployeeId(), dateStr, dateStr);
                        if (existing != null && !existing.isEmpty())
                        {
                            continue;
                        }
                    }

                    HrSchedule schedule = new HrSchedule();
                    schedule.setScheduleDate(cal.getTime());
                    schedule.setEmployeeId(emp.getEmployeeId());
                    schedule.setEmployeeName(emp.getEmployeeName());
                    schedule.setEmployeeNo(emp.getEmployeeNo());
                    schedule.setOrgId(emp.getOrgId());
                    schedule.setShiftId(effectiveShiftId);
                    schedule.setShiftName(effectiveShiftName);
                    schedule.setStatus("0");
                    schedule.setCreateBy(operName);
                    toInsert.add(schedule);
                }
                dayIndex++;
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 6. 批量插入
        int count = 0;
        if (!toInsert.isEmpty())
        {
            // 分批插入，每批200条
            int batchSize = 200;
            for (int i = 0; i < toInsert.size(); i += batchSize)
            {
                int end = Math.min(i + batchSize, toInsert.size());
                List<HrSchedule> batch = toInsert.subList(i, end);
                count += hrScheduleMapper.batchInsertHrSchedule(batch);
            }
        }

        return count;
    }
}
