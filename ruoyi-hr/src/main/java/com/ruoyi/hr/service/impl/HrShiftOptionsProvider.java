package com.ruoyi.hr.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ruoyi.flowable.service.FlowFormOptionsProvider;

/**
 * 考勤班次表单动态选项 Provider
 *
 * 为流程表单提供「补卡班次」等字段的动态选项，数据来源为考勤模块班次配置
 * biz_hr_shift（新增/停用班次后表单选项实时跟随，无需单独维护字典）。
 *
 * 字段配置：{ "form": "select", "optionsSource": "hrShift", ... }
 *
 * @author ruoyi
 */
@Service
public class HrShiftOptionsProvider implements FlowFormOptionsProvider
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String source()
    {
        return "hrShift";
    }

    @Override
    public List<FlowFormOption> options(Map<String, Object> ctx)
    {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT shift_name FROM biz_hr_shift WHERE status = '0' AND del_flag = '0' ORDER BY shift_id");
        if (rows.isEmpty())
        {
            // 无可用班次时返回 null，前端回退字段静态 selectOptions
            return null;
        }
        List<FlowFormOption> options = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            String name = row.get("shift_name") == null ? null : row.get("shift_name").toString();
            if (name != null && !name.isEmpty())
            {
                options.add(new FlowFormOption(name, name));
            }
        }
        return options;
    }
}
