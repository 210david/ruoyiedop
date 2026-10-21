package com.ruoyi.hr.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrAttendanceMonthly;
import com.ruoyi.hr.service.IHrAttendanceMonthlyService;

/**
 * 人力资源 AI辅助 Controller（外挂式，不改动既有业务代码）
 * 功能：月度考勤异常摘要（不涉及任何薪资数据）
 * AI输出均为"草稿/建议"，仅供管理决策参考
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/hr/ai")
public class HrAiAssistController extends BaseController
{
    /** 考勤摘要：最多取前N条月度记录组装prompt */
    private static final int RECORD_LIMIT = 200;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private IHrAttendanceMonthlyService attendanceMonthlyService;

    /**
     * 月度考勤异常摘要：按年月拉取考勤月报，LLM生成出勤概况、缺勤榜、部门分布与管理建议
     * 注意：仅使用考勤数据，不触碰任何薪资相关数据
     */
    @PreAuthorize("@ss.hasPermi('hr:ai:attendanceSummary')")
    @GetMapping("/attendanceSummary/{yearMonth}")
    public AjaxResult attendanceSummary(@PathVariable("yearMonth") String yearMonth)
    {
        if (StringUtils.isEmpty(yearMonth))
        {
            return AjaxResult.error("年月参数不能为空");
        }
        HrAttendanceMonthly query = new HrAttendanceMonthly();
        query.setYearMonth(yearMonth);
        List<HrAttendanceMonthly> list = attendanceMonthlyService.selectHrAttendanceMonthlyList(query);
        if (list == null || list.isEmpty())
        {
            return AjaxResult.error(yearMonth + " 无考勤月报数据，请先执行月报生成");
        }

        StringBuilder data = new StringBuilder();
        int count = 0;
        for (HrAttendanceMonthly m : list)
        {
            if (count++ >= RECORD_LIMIT) break;
            data.append(StringUtils.nvl(m.getEmployeeName(), "-"))
                .append(" | 部门:").append(StringUtils.nvl(m.getDeptName(), "-"))
                .append(" | 应出勤:").append(m.getRequiredDays() == null ? "-" : m.getRequiredDays().toPlainString())
                .append("天 | 出勤:").append(m.getAttendDays() == null ? "0" : m.getAttendDays().toPlainString())
                .append("天 | 旷工:").append(m.getAbsentDays() == null ? "0" : m.getAbsentDays().toPlainString())
                .append("天 | 请假:").append(m.getLeaveDays() == null ? "0" : m.getLeaveDays().toPlainString())
                .append("天 | 外出:").append(m.getOutingDays() == null ? "0" : m.getOutingDays().toPlainString())
                .append("天 | 出差:").append(m.getTripDays() == null ? "0" : m.getTripDays().toPlainString())
                .append("天 | 加班:").append(m.getOvertimeHours() == null ? "0" : m.getOvertimeHours().toPlainString())
                .append("小时 | 迟到:").append(m.getLateCount() == null ? "0" : m.getLateCount())
                .append("次 | 早退:").append(m.getEarlyCount() == null ? "0" : m.getEarlyCount()).append("次");
            data.append("\n");
        }

        String prompt = "你是人力资源管理助手。根据以下" + yearMonth + "月考勤月报数据（共" + list.size()
                + "人，展示前" + count + "条），生成月度考勤异常摘要。\n"
                + "结构：一、出勤概况（整体出勤/请假/旷工情况） 二、缺勤较多的员工清单（只列员工姓名与旷工/请假天数/迟到早退次数，"
                + "严禁涉及或推测任何薪资、薪酬、工资信息） 三、部门分布（哪些部门考勤异常集中） 四、管理建议（考勤纪律、排班优化等）。\n"
                + "要求：结论先行，引用具体姓名与数字；数据中缺失的信息一律写【待补充】，不得编造。\n\n"
                + "【" + yearMonth + "月考勤月报（前" + count + "条）】\n" + data;

        AiResult result = aiClient.chatCached("hr", "attendance_summary", prompt);
        return AjaxResult.success(result.toMap());
    }
}
