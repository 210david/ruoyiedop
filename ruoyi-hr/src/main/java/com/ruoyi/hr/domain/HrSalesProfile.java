package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 营销岗员工档案扩展对象 biz_hr_sales_profile
 *
 * 需求三：员工档案扩展营销岗属性（销售区域/渠道、职级、提成方案、入职营销目标）
 */
public class HrSalesProfile extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 档案ID */
    private Long profileId;

    /** 员工ID（biz_hr_employee） */
    private Long employeeId;

    /** 员工编号 */
    @Excel(name = "员工编号")
    private String employeeNo;

    /** 员工姓名 */
    @Excel(name = "员工姓名")
    private String employeeName;

    /** 关联系统用户ID（营销业绩归属：mk_customer.user_id） */
    private Long userId;

    /** 销售区域 */
    @Excel(name = "销售区域")
    private String salesRegion;

    /** 销售渠道 */
    @Excel(name = "销售渠道")
    private String channelName;

    /** 职级（1业务员 2主管 3经理） */
    @Excel(name = "职级", dictType = "hr_sales_level")
    private String salesLevel;

    /** 关联提成方案ID */
    private Long commissionPlanId;

    /** 入职营销目标（年度） */
    @Excel(name = "年度营销目标")
    private BigDecimal entryTarget;

    /** 删除标志 */
    private String delFlag;

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSalesRegion() { return salesRegion; }
    public void setSalesRegion(String salesRegion) { this.salesRegion = salesRegion; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getSalesLevel() { return salesLevel; }
    public void setSalesLevel(String salesLevel) { this.salesLevel = salesLevel; }
    public Long getCommissionPlanId() { return commissionPlanId; }
    public void setCommissionPlanId(Long commissionPlanId) { this.commissionPlanId = commissionPlanId; }
    public BigDecimal getEntryTarget() { return entryTarget; }
    public void setEntryTarget(BigDecimal entryTarget) { this.entryTarget = entryTarget; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
