package com.ruoyi.hr.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.stats.IModuleStatsProvider;
import com.ruoyi.common.stats.MetricValue;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.mapper.HrStatsMapper;

/**
 * 人力资源模块统计指标提供者
 *
 * 直连 Mapper 而非 Service：快照任务无登录上下文，
 * 避免经过带 @DataScope 注解的 Service 列表方法（规范 3.5.3）。
 *
 * 覆盖指标：HR_HEADCOUNT / HR_LEAVE_RATE / HR_COST_RATIO
 *
 * @author ruoyi
 */
@Component
public class HrStatsProvider implements IModuleStatsProvider
{
    @Autowired
    private HrStatsMapper hrStatsMapper;

    @Autowired
    private HrEmployeeMapper employeeMapper;

    @Override
    public String getModuleCode()
    {
        return "hr";
    }

    @Override
    public List<String> supportedMetricCodes()
    {
        return Arrays.asList("HR_HEADCOUNT", "HR_LEAVE_RATE", "HR_COST_RATIO");
    }

    @Override
    public MetricValue getMetric(String metricCode, Date metricDate, Long deptId)
    {
        // 部门维度快照待后续迭代（当前仅全部门汇总）
        if (deptId != null)
        {
            return new MetricValue(metricCode, metricDate, deptId, null, null, "real");
        }

        BigDecimal value = null;
        String unit = null;
        switch (metricCode)
        {
            case "HR_HEADCOUNT":
                // 口径：在册人数 = 在职(0) + 试用期(1) 员工数
                int headcount = 0;
                for (Map<String, Object> row : hrStatsMapper.selectEmployeeStatusCount())
                {
                    String status = String.valueOf(row.get("status"));
                    if ("0".equals(status) || "1".equals(status))
                    {
                        headcount += ((Number) row.get("cnt")).intValue();
                    }
                }
                value = BigDecimal.valueOf(headcount);
                unit = "人";
                break;

            case "HR_LEAVE_RATE":
                // 口径：月离职率 = 当月离职人数 / (当前在职 + 试用期 + 当月离职)
                // 离职判定：employee_status=2 且离职日期落在指标日期当月
                int active = 0;
                for (Map<String, Object> row : hrStatsMapper.selectEmployeeStatusCount())
                {
                    String status = String.valueOf(row.get("status"));
                    if ("0".equals(status) || "1".equals(status))
                    {
                        active += ((Number) row.get("cnt")).intValue();
                    }
                }
                int monthLeft = 0;
                for (HrEmployee emp : employeeMapper.selectHrEmployeeList(new HrEmployee()))
                {
                    if ("2".equals(emp.getEmployeeStatus()) && emp.getLeaveDate() != null
                            && inSameMonth(emp.getLeaveDate(), metricDate))
                    {
                        monthLeft++;
                    }
                }
                value = rate(monthLeft, active + monthLeft);
                unit = "%";
                break;

            case "HR_COST_RATIO":
                // 口径【已确认 v1】：人工成本占比 = 当月实发工资合计 / 当月完工入库产值 × 100%。
                // 分子：biz_hr_payslip 实发工资合计；分母：MMS 完工入库产值（HrStatsMapper 联查说明见 XML）。
                BigDecimal salary = hrStatsMapper.selectNetSalaryTotal(toYearMonth(metricDate));
                BigDecimal output = hrStatsMapper.selectFinishOutputValue(
                        monthStart(metricDate), monthEnd(metricDate));
                if (salary == null || output == null
                        || output.compareTo(BigDecimal.ZERO) <= 0)
                {
                    // 工资未核算或产值无数据：返回 null（前端展示"—"），禁止用 0 冒充
                    value = null;
                }
                else
                {
                    value = salary.multiply(BigDecimal.valueOf(100))
                            .divide(output, 2, RoundingMode.HALF_UP);
                }
                unit = "%";
                break;

            default:
                break;
        }
        return new MetricValue(metricCode, metricDate, deptId, value, unit, "real");
    }

    private boolean inSameMonth(Date a, Date b)
    {
        if (a == null || b == null)
        {
            return false;
        }
        Calendar ca = Calendar.getInstance();
        ca.setTime(a);
        Calendar cb = Calendar.getInstance();
        cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
                && ca.get(Calendar.MONTH) == cb.get(Calendar.MONTH);
    }

    private String toYearMonth(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return String.format("%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
    }

    private Date monthStart(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private Date monthEnd(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(monthStart(date));
        c.add(Calendar.MONTH, 1);
        return c.getTime();
    }

    private BigDecimal rate(int part, int total)
    {
        if (total <= 0)
        {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(part * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
    }
}
