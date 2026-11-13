package com.ruoyi.integration.wecom;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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
 * 企业微信模拟数据源（仅用于无凭据环境下的端到端联调）
 *
 * 设计要点：
 * 1. 完全不发起任何 HTTP 请求，不依赖 corpId / secret；
 * 2. 通讯录数据基于平台真实员工档案构造，专门覆盖「手机号优先、工号兜底」策略下的各类分支：
 *    - 手机号可匹配
 *    - 手机号为空（模拟企业微信 2022-06-20 后新建自建应用不返回 mobile 的限制）→ 靠 alias 别名对工号命中
 *    - 手机号为空、alias 为空 → 靠 userid 直接对工号命中
 *    - 手机号错误但 alias 正确 → 兜底命中
 *    - 全部对不上 → 保持未匹配，验证人工绑定
 * 3. 打卡数据使用企业微信真实返回结构 checkin_type（上班打卡/下班打卡/外出打卡），
 *    用于验证 punchType 精确落位：迟到、缺下班卡、只有下班卡、外出打卡不参与上下班落位；
 * 4. 所有模拟用户的 userid 统一以 {@link #USERID_PREFIX} 开头，便于一键清理绑定表。
 *
 * 关闭参数 integration.wecom.mockEnabled 后，调用链自动走真实企业微信 HTTP 分支。
 *
 * @author ruoyi
 */
@Component
public class WecomMockDataProvider
{
    private static final Logger log = LoggerFactory.getLogger(WecomMockDataProvider.class);

    /** 模拟用户 userid 前缀（用于识别与清理模拟数据） */
    public static final String USERID_PREFIX = "mockwecom_";

    /** 打卡类型（与企业微信 checkin_type 一致） */
    public static final String TYPE_ON_DUTY = "上班打卡";

    public static final String TYPE_OFF_DUTY = "下班打卡";

    public static final String TYPE_OUT = "外出打卡";

    /** 模拟打卡生成的工作日数量 */
    private static final int MOCK_WORK_DAYS = 3;

    /** 上班基准时间 */
    private static final LocalTime CHECK_IN_TIME = LocalTime.of(8, 22);

    /** 下班基准时间 */
    private static final LocalTime CHECK_OUT_TIME = LocalTime.of(18, 6);

    @Autowired
    private IHrEmployeeService hrEmployeeService;

    /**
     * 模拟企业微信通讯录用户列表
     *
     * @return 用户列表（userid / name / mobile / alias / position）
     */
    public List<JSONObject> listUsers()
    {
        List<HrEmployee> employees = loadEmployees();
        List<JSONObject> users = new ArrayList<>();

        HrEmployee first = employeeAt(employees, 0);
        HrEmployee second = employeeAt(employees, 1);
        HrEmployee third = employeeAt(employees, 2);
        HrEmployee fourth = employeeAt(employees, 3);

        // 1. 手机号正确 → 手机号策略直接命中
        users.add(buildUser(USERID_PREFIX + "u001", nameOf(first, "企微模拟甲"), phoneOf(first, "13800000001"), "", ""));

        // 2. 手机号为空（模拟企微新应用不返回 mobile）→ 靠 alias 别名对工号兜底
        users.add(buildUser(USERID_PREFIX + "u002", nameOf(second, "企微模拟乙"), "", noOf(second, "MOCKW0002"), ""));

        // 3. 手机号与 alias 都为空 → 靠 userid 直接对工号兜底
        String thirdNo = noOf(third, "MOCKW0003");
        users.add(buildUser(USERID_PREFIX + thirdNo, nameOf(third, "企微模拟丙"), "", "", ""));

        // 4. 手机号错误但 alias 正确 → 手机号未命中后兜底命中
        users.add(buildUser(USERID_PREFIX + "u004", nameOf(fourth, "企微模拟丁"), "13900000004", noOf(fourth, "MOCKW0004"), ""));

        // 5. 两名虚构人员：信息完全对不上 → 保持未匹配，用于验证人工绑定
        users.add(buildUser(USERID_PREFIX + "u005", "企微未匹配甲", "13700000005", "", ""));
        users.add(buildUser(USERID_PREFIX + "u006", "企微未匹配乙", "", "", ""));

        log.info("企业微信模拟通讯录生成完成：{} 个用户（基于 {} 条真实员工档案构造）", users.size(), employees.size());
        return users;
    }

    /**
     * 模拟打卡记录
     *
     * 埋点场景：
     * - u002：每天迟到（09:05 上班卡）
     * - u003：只有上班卡，缺下班卡 → 精确落位后仍判漏卡
     * - u004：只有下班卡（19:46），验证"只打下班卡"不再被推断成上班卡
     * - u005：当天只有外出打卡 → 不参与上下班落位，回退到原有推断逻辑
     * - u006：上班卡 + 晚间外出卡 → 外出卡不会被当成下班卡
     *
     * @param beginDate  开始时间
     * @param endDate    结束时间
     * @param useridList 需要生成的用户
     * @return 打卡记录列表（userid / checkin_type / checkin_time）
     */
    public List<JSONObject> listAttendance(Date beginDate, Date endDate, List<String> useridList)
    {
        List<JSONObject> records = new ArrayList<>();
        if (useridList == null || useridList.isEmpty())
        {
            return records;
        }
        LocalDate begin = toLocalDate(beginDate);
        LocalDate end = toLocalDate(endDate);
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
                // 只有下班卡：模拟 19:46 打下班卡、上班未打卡
                if (userid.endsWith("u004"))
                {
                    records.add(buildPunch(userid, day, LocalTime.of(19, 46), TYPE_OFF_DUTY));
                    continue;
                }
                // 只有外出打卡：验证外出卡不被当上下班卡
                if (userid.endsWith("u005"))
                {
                    records.add(buildPunch(userid, day, LocalTime.of(10, 30), TYPE_OUT));
                    continue;
                }

                LocalTime inTime = userid.endsWith("u002") ? LocalTime.of(9, 5) : CHECK_IN_TIME;
                records.add(buildPunch(userid, day, inTime, TYPE_ON_DUTY));

                // 缺下班卡：u003、u006
                if (userid.endsWith("u003") || userid.endsWith("u006"))
                {
                    // u006 额外补一条晚间外出卡，验证不覆盖正常下班判定
                    if (userid.endsWith("u006"))
                    {
                        records.add(buildPunch(userid, day, LocalTime.of(20, 15), TYPE_OUT));
                    }
                    continue;
                }
                records.add(buildPunch(userid, day, CHECK_OUT_TIME, TYPE_OFF_DUTY));
            }
        }
        log.info("企业微信模拟打卡生成完成：{} 条（{}~{}，{} 个工作日）", records.size(), begin, end, workDays.size());
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
                    if (StringUtils.isNotEmpty(employee.getEmployeeNo()))
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
     * 构造企业微信通讯录用户结构
     */
    private JSONObject buildUser(String userid, String name, String mobile, String alias, String position)
    {
        JSONObject user = new JSONObject();
        user.put("userid", userid);
        user.put("name", name);
        user.put("mobile", mobile);
        user.put("alias", alias);
        user.put("position", position);
        user.put("status", 1);
        user.put("department", new long[] { 1L });
        return user;
    }

    /**
     * 构造企业微信打卡记录结构（checkin_time 为秒级时间戳，与 checkin/getcheckindata 一致）
     */
    private JSONObject buildPunch(String userid, LocalDate day, LocalTime time, String checkinType)
    {
        LocalDateTime dateTime = LocalDateTime.of(day, time);
        JSONObject record = new JSONObject();
        record.put("userid", userid);
        record.put("checkin_type", checkinType);
        record.put("checkin_time", dateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        record.put("exception_type", "");
        record.put("groupname", "模拟打卡规则");
        record.put("location_title", "模拟打卡地点");
        return record;
    }

    private LocalDate toLocalDate(Date date)
    {
        if (date == null)
        {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private HrEmployee employeeAt(List<HrEmployee> employees, int index)
    {
        return index < employees.size() ? employees.get(index) : null;
    }

    private String nameOf(HrEmployee employee, String fallback)
    {
        return employee != null && StringUtils.isNotEmpty(employee.getEmployeeName())
                ? employee.getEmployeeName() : fallback;
    }

    private String phoneOf(HrEmployee employee, String fallback)
    {
        return employee != null && StringUtils.isNotEmpty(employee.getPhone()) ? employee.getPhone() : fallback;
    }

    private String noOf(HrEmployee employee, String fallback)
    {
        return employee != null && StringUtils.isNotEmpty(employee.getEmployeeNo())
                ? employee.getEmployeeNo() : fallback;
    }
}
