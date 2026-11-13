package com.ruoyi.integration.dingtalk;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.service.IHrEmployeeService;

/**
 * 钉钉模拟数据源（仅用于无凭据环境下的端到端联调）
 *
 * 设计要点：
 * 1. 完全不发起任何 HTTP 请求，不依赖 AppKey / AppSecret；
 * 2. 通讯录数据基于平台真实员工档案构造，保证手机号/工号匹配能够真实命中，
 *    覆盖「手机号匹配 / 工号匹配 / auto 工号优先 / 完全匹配不上」四类结果；
 * 3. 打卡数据生成在最近若干个工作日内，并埋入迟到、缺下班卡等异常，便于验证班次规则；
 * 4. 所有模拟用户的 userid 统一以 {@link #USERID_PREFIX} 开头，便于一键清理绑定表。
 *
 * 关闭参数 integration.dingtalk.mockEnabled 后，调用链自动走真实钉钉 HTTP 分支。
 *
 * @author ruoyi
 */
@Component
public class DingtalkMockDataProvider
{
    private static final Logger log = LoggerFactory.getLogger(DingtalkMockDataProvider.class);

    /** 模拟用户 userid 前缀（用于识别与清理模拟数据） */
    public static final String USERID_PREFIX = "mock_";

    /** 模拟打卡生成的工作日数量 */
    private static final int MOCK_WORK_DAYS = 3;

    /** 上班基准时间（早于多数企业 08:30 上班时间，保证默认不迟到） */
    private static final LocalTime CHECK_IN_TIME = LocalTime.of(8, 22);

    /** 下班基准时间 */
    private static final LocalTime CHECK_OUT_TIME = LocalTime.of(18, 6);

    @Autowired
    private IHrEmployeeService hrEmployeeService;

    /**
     * 模拟钉钉通讯录用户列表
     *
     * @return 用户列表（userid / name / mobile / job_number）
     */
    public List<JSONObject> listUsers()
    {
        List<HrEmployee> employees = loadEmployees();
        List<JSONObject> users = new ArrayList<>();

        // 1. 两名员工：仅提供手机号 → 手机号匹配
        HrEmployee first = employeeAt(employees, 0);
        HrEmployee second = employeeAt(employees, 1);
        users.add(buildUser(USERID_PREFIX + "u001", nameOf(first, "模拟员工甲"), phoneOf(first, "13800000001"), null));
        users.add(buildUser(USERID_PREFIX + "u002", nameOf(second, "模拟员工乙"), phoneOf(second, "13800000002"), null));

        // 2. 一名员工：仅提供钉钉工号（手机号故意给错）→ 工号匹配
        HrEmployee third = employeeAt(employees, 2);
        users.add(buildUser(USERID_PREFIX + "u003", nameOf(third, "模拟员工丙"), "13900000003", noOf(third, "MOCK0003")));

        // 3. 一名员工：手机号与工号同时正确 → auto 策略命中工号
        HrEmployee fourth = employeeAt(employees, 3);
        users.add(buildUser(USERID_PREFIX + "u004", nameOf(fourth, "模拟员工丁"), phoneOf(fourth, "13800000004"), noOf(fourth, "MOCK0004")));

        // 4. 两名虚构人员：信息完全对不上 → 保持未匹配，用于验证人工绑定
        users.add(buildUser(USERID_PREFIX + "u005", "模拟未匹配甲", "13700000005", "MOCK-NO-05"));
        users.add(buildUser(USERID_PREFIX + "u006", "模拟未匹配乙", "13700000006", ""));

        log.info("钉钉模拟通讯录生成完成：{} 个用户（基于 {} 条真实员工档案构造）", users.size(), employees.size());
        return users;
    }

    /**
     * 模拟打卡记录
     *
     * 仅生成 mock_ 前缀用户在工作日内的上下班打卡；
     * 埋点：u002 每天迟到（09:25 上班）、u003 每天缺下班卡、u006 只有上班卡。
     *
     * @param workDateFrom yyyy-MM-dd HH:mm:ss
     * @param workDateTo   yyyy-MM-dd HH:mm:ss
     * @param useridList   需要生成的用户（单次 ≤50，与真实接口一致）
     * @return 打卡记录列表（userid / userCheckTime / checkType / timeResult）
     */
    public List<JSONObject> listAttendance(String workDateFrom, String workDateTo, List<String> useridList)
    {
        List<JSONObject> records = new ArrayList<>();
        if (useridList == null || useridList.isEmpty())
        {
            return records;
        }
        LocalDate begin = parseDate(workDateFrom);
        LocalDate end = parseDate(workDateTo);
        if (begin == null || end == null || begin.isAfter(end))
        {
            return records;
        }

        List<LocalDate> workDays = collectWorkDays(begin, end);
        for (String userid : useridList)
        {
            if (StringUtils.isEmpty(userid) || !userid.startsWith(USERID_PREFIX))
            {
                continue;
            }
            for (LocalDate day : workDays)
            {
                // 上班卡：u002 每天迟到（09:05 打卡，相对 08:30 班次迟到 35 分钟）
                LocalTime inTime = userid.endsWith("u002") ? LocalTime.of(9, 5) : CHECK_IN_TIME;
                records.add(buildPunch(userid, day, inTime, "OnDuty", inTime.isAfter(LocalTime.of(9, 0)) ? "Late" : "Normal"));

                // 下班卡：u003 缺下班卡，u006 缺下班卡
                if (userid.endsWith("u003") || userid.endsWith("u006"))
                {
                    continue;
                }
                records.add(buildPunch(userid, day, CHECK_OUT_TIME, "OffDuty", "Normal"));
            }
        }
        log.info("钉钉模拟打卡生成完成：{} 条（{}~{}，{} 个工作日）", records.size(), begin, end, workDays.size());
        return records;
    }

    /**
     * 读取平台真实员工档案（最多 4 条，作为模拟用户原型）
     */
    private List<HrEmployee> loadEmployees()
    {
        try
        {
            HrEmployee query = new HrEmployee();
            List<HrEmployee> list = hrEmployeeService.selectHrEmployeeList(query);
            List<HrEmployee> result = new ArrayList<>();
            if (list != null)
            {
                for (HrEmployee employee : list)
                {
                    if (StringUtils.isNotEmpty(employee.getPhone()) && StringUtils.isNotEmpty(employee.getEmployeeNo()))
                    {
                        result.add(employee);
                    }
                    if (result.size() >= 4)
                    {
                        break;
                    }
                }
            }
            return result;
        }
        catch (Exception e)
        {
            log.warn("读取员工档案失败，模拟通讯录将使用纯虚构数据: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 收集日期范围内的工作日（跳过周六周日，最多 MOCK_WORK_DAYS 天）
     */
    private List<LocalDate> collectWorkDays(LocalDate begin, LocalDate end)
    {
        List<LocalDate> days = new ArrayList<>();
        LocalDate cursor = end;
        while (!cursor.isBefore(begin) && days.size() < MOCK_WORK_DAYS)
        {
            DayOfWeek week = cursor.getDayOfWeek();
            if (week != DayOfWeek.SATURDAY && week != DayOfWeek.SUNDAY)
            {
                days.add(0, cursor);
            }
            cursor = cursor.minusDays(1);
        }
        return days;
    }

    /**
     * 构造钉钉通讯录用户结构
     */
    private JSONObject buildUser(String userid, String name, String mobile, String jobNumber)
    {
        JSONObject user = new JSONObject();
        user.put("userid", userid);
        user.put("name", name);
        user.put("mobile", mobile);
        user.put("job_number", jobNumber);
        user.put("active", true);
        user.put("dept_id_list", new long[] { 1L });
        return user;
    }

    /**
     * 构造钉钉打卡记录结构（userCheckTime 为毫秒时间戳，与 oapi 返回一致）
     */
    private JSONObject buildPunch(String userid, LocalDate day, LocalTime time, String checkType, String timeResult)
    {
        LocalDateTime dateTime = LocalDateTime.of(day, time);
        JSONObject record = new JSONObject();
        record.put("userid", userid);
        record.put("userCheckTime", dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        record.put("checkType", checkType);
        record.put("timeResult", timeResult);
        record.put("locationResult", "Normal");
        record.put("deviceId", "mock-device");
        record.put("userAddress", "模拟打卡地点");
        return record;
    }

    /**
     * 解析 yyyy-MM-dd HH:mm:ss（容错为 yyyy-MM-dd）
     */
    private LocalDate parseDate(String text)
    {
        if (StringUtils.isEmpty(text) || text.length() < 10)
        {
            return null;
        }
        try
        {
            return LocalDate.parse(text.substring(0, 10));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private HrEmployee employeeAt(List<HrEmployee> employees, int index)
    {
        return index < employees.size() ? employees.get(index) : null;
    }

    private String nameOf(HrEmployee employee, String fallback)
    {
        return employee != null ? employee.getEmployeeName() : fallback;
    }

    private String phoneOf(HrEmployee employee, String fallback)
    {
        return employee != null && StringUtils.isNotEmpty(employee.getPhone()) ? employee.getPhone() : fallback;
    }

    private String noOf(HrEmployee employee, String fallback)
    {
        return employee != null && StringUtils.isNotEmpty(employee.getEmployeeNo()) ? employee.getEmployeeNo() : fallback;
    }
}
