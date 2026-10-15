package com.ruoyi.food.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * HACCP CCP控制点模板表 food_ccp_template
 *
 * @author ruoyi
 */
public class FoodCcpTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long templateId;

    /** 模板名称 */
    private String templateName;

    /** 工艺路线ID */
    private Long routeId;

    /** 工艺路线名称 */
    private String routeName;

    /** 工序名称 */
    private String processName;

    /** 控制点名称 */
    private String ccpName;

    /** 监控项类型(字典food_ccp_monitor_type) */
    private String monitorType;

    /** 限值类型(字典food_ccp_limit_type) */
    private String limitType;

    /** 限值下限 */
    private BigDecimal limitMin;

    /** 限值上限 */
    private BigDecimal limitMax;

    /** 枚举限值 */
    private String limitValue;

    /** 单位 */
    private String unit;

    /** 监控频率 */
    private String monitorFreq;

    /** 纠正措施 */
    private String correctiveAction;

    /** 状态(0正常1停用) */
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getCcpName() { return ccpName; }
    public void setCcpName(String ccpName) { this.ccpName = ccpName; }
    public String getMonitorType() { return monitorType; }
    public void setMonitorType(String monitorType) { this.monitorType = monitorType; }
    public String getLimitType() { return limitType; }
    public void setLimitType(String limitType) { this.limitType = limitType; }
    public BigDecimal getLimitMin() { return limitMin; }
    public void setLimitMin(BigDecimal limitMin) { this.limitMin = limitMin; }
    public BigDecimal getLimitMax() { return limitMax; }
    public void setLimitMax(BigDecimal limitMax) { this.limitMax = limitMax; }
    public String getLimitValue() { return limitValue; }
    public void setLimitValue(String limitValue) { this.limitValue = limitValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getMonitorFreq() { return monitorFreq; }
    public void setMonitorFreq(String monitorFreq) { this.monitorFreq = monitorFreq; }
    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
