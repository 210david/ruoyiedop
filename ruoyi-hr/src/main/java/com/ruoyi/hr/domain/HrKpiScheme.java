package com.ruoyi.hr.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.List;

/**
 * KPI考核方案对象 biz_hr_kpi_scheme
 *
 * 需求三：绩效考核方案配置（周期/指标/权重/目标/评分规则），方案启用后方可发起考核
 */
public class HrKpiScheme extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 方案ID */
    private Long schemeId;

    /** 方案名称 */
    @Excel(name = "方案名称")
    private String schemeName;

    /** 考核周期（1月 2季 3年） */
    @Excel(name = "考核周期", dictType = "hr_kpi_period_type")
    private String periodType;

    /** 状态（0草稿 1启用 2停用） */
    @Excel(name = "状态")
    private String schemeStatus;

    /** 删除标志 */
    private String delFlag;

    /** 考核指标明细 */
    private List<HrKpiItem> items;

    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }
    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    public String getSchemeStatus() { return schemeStatus; }
    public void setSchemeStatus(String schemeStatus) { this.schemeStatus = schemeStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<HrKpiItem> getItems() { return items; }
    public void setItems(List<HrKpiItem> items) { this.items = items; }
}
