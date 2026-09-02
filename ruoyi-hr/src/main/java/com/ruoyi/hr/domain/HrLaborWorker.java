package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrLaborWorker extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long laborId;
    @NotBlank(message = "姓名不能为空")
    @Excel(name = "姓名")
    private String workerName;
    @Excel(name = "身份证号")
    private String idCard;
    @Excel(name = "联系电话")
    private String phone;
    @Excel(name = "用工形态")
    private String workerType;
    @Excel(name = "派遣公司")
    private String dispatchCompany;
    @Excel(name = "开户银行")
    private String bankName;
    @Excel(name = "银行卡号")
    private String bankAccount;
    @Excel(name = "分配组织ID")
    private Long orgId;
    /** 部门名称（关联 sys_dept 回显，非表字段） */
    private String orgName;
    @Excel(name = "岗位")
    private String positionName;
    @NotNull(message = "用工开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "用工开始日期")
    private Date startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "用工结束日期")
    private Date endDate;
    @Excel(name = "结算方式")
    private String payType;
    @Excel(name = "日薪标准")
    private BigDecimal dailyWage;
    @Excel(name = "协议编号")
    private String agreementNo;
    @Excel(name = "是否转正")
    private String toRegular;
    @Excel(name = "转正后员工ID")
    private Long employeeId;
    /** 转正后员工姓名（关联 biz_hr_employee 回显，非表字段） */
    private String employeeName;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getLaborId() { return laborId; }
    public void setLaborId(Long laborId) { this.laborId = laborId; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getWorkerType() { return workerType; }
    public void setWorkerType(String workerType) { this.workerType = workerType; }
    public String getDispatchCompany() { return dispatchCompany; }
    public void setDispatchCompany(String dispatchCompany) { this.dispatchCompany = dispatchCompany; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }
    public BigDecimal getDailyWage() { return dailyWage; }
    public void setDailyWage(BigDecimal dailyWage) { this.dailyWage = dailyWage; }
    public String getAgreementNo() { return agreementNo; }
    public void setAgreementNo(String agreementNo) { this.agreementNo = agreementNo; }
    public String getToRegular() { return toRegular; }
    public void setToRegular(String toRegular) { this.toRegular = toRegular; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
