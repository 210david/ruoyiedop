package com.ruoyi.hr.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 排班模板 对象 biz_hr_schedule_template
 */
public class HrScheduleTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long templateId;

    /** 模板名称 */
    @NotBlank(message = "模板名称不能为空")
    @Excel(name = "模板名称")
    private String templateName;

    /** 模板编码 */
    @Excel(name = "模板编码")
    private String templateCode;

    /** 循环天数 */
    @NotNull(message = "循环天数不能为空")
    @Excel(name = "循环天数")
    private Integer cycleDays;

    /** 班次序列JSON */
    @NotBlank(message = "班次序列不能为空")
    private String shiftSequence;

    /** 轮转方向(0正向1反向) */
    @Excel(name = "轮转方向")
    private String rotationType;

    /** 模板说明 */
    @Excel(name = "模板说明")
    private String description;

    /** 状态(0启用1停用) */
    @Excel(name = "状态")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public Integer getCycleDays() { return cycleDays; }
    public void setCycleDays(Integer cycleDays) { this.cycleDays = cycleDays; }
    public String getShiftSequence() { return shiftSequence; }
    public void setShiftSequence(String shiftSequence) { this.shiftSequence = shiftSequence; }
    public String getRotationType() { return rotationType; }
    public void setRotationType(String rotationType) { this.rotationType = rotationType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
