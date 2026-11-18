package com.ruoyi.flowable.service;

import java.util.List;
import java.util.Map;

/**
 * 流程表单动态选项 Provider SPI
 *
 * 表单字段（select/radio）配置 optionsSource 后，前端获取表单时由后端
 * 按数据源动态填充 selectOptions，使表单选项跟随业务数据实时变化
 * （如打卡补卡的「补卡班次」选项来自考勤模块班次配置 biz_hr_shift）。
 *
 * 字段配置示例：
 * <pre>{ "field": "shift_type", "form": "select", "optionsSource": "hrShift", ... }</pre>
 *
 * 使用方式：业务模块实现本接口并注册为 Spring Bean；
 * FlowFormServiceImpl 返回表单详情时按 optionsSource 匹配并合并选项。
 * 字段上已有的静态 selectOptions 作为 Provider 不可用时的兜底。
 *
 * @author ruoyi
 */
public interface FlowFormOptionsProvider
{
    /** 选项 DTO：label 显示文本 / value 提交值 */
    class FlowFormOption
    {
        private String label;
        private String value;

        public FlowFormOption() { }

        public FlowFormOption(String label, String value)
        {
            this.label = label;
            this.value = value;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    /**
     * 选项数据源标识，对应字段配置的 optionsSource（如 hrShift）
     */
    String source();

    /**
     * 返回该数据源的选项列表
     *
     * @param ctx 上下文：field=字段配置JSON对象, userId=当前用户ID（可为null）, formId=表单ID（可为null）
     * @return 选项列表，null 或空表示无可用选项（回退字段静态 selectOptions）
     */
    List<FlowFormOption> options(Map<String, Object> ctx);
}
