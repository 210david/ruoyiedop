package com.ruoyi.integration.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 外部平台用户绑定关系（如钉钉 userid 与平台员工工号映射）
 *
 * @author ruoyi
 */
public class IntegrationUserBinding extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long bindingId;

    /** 连接器类型（dingtalk / wecom） */
    private String connectorType;

    /** 外部平台用户ID（钉钉 userid） */
    private String externalUserid;

    /** 外部平台姓名 */
    private String externalName;

    /** 外部平台手机号 */
    private String mobile;

    /** 外部平台工号（钉钉 jobNumber，按工号匹配时使用） */
    private String externalJobNo;

    /** 平台员工工号（匹配成功后回填） */
    private String employeeNo;

    /** 平台员工ID */
    private Long employeeId;

    /** 平台员工姓名 */
    private String employeeName;

    /** 匹配方式（auto=手机号自动匹配 manual=人工绑定） */
    private String matchType;

    /** 最后同步时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date syncTime;

    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }
    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }
    public String getExternalUserid() { return externalUserid; }
    public void setExternalUserid(String externalUserid) { this.externalUserid = externalUserid; }
    public String getExternalName() { return externalName; }
    public void setExternalName(String externalName) { this.externalName = externalName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getExternalJobNo() { return externalJobNo; }
    public void setExternalJobNo(String externalJobNo) { this.externalJobNo = externalJobNo; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public Date getSyncTime() { return syncTime; }
    public void setSyncTime(Date syncTime) { this.syncTime = syncTime; }
}
