package com.ruoyi.pms.domain.dto;

import java.math.BigDecimal;

/**
 * 企业信息查询结果DTO
 *
 * @author ruoyi
 */
public class PmsEnterpriseInfoDTO
{
    /** 企业名称 */
    private String enterpriseName;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 法人代表 */
    private String legalPerson;

    /** 注册资本（万元） */
    private BigDecimal registeredCapital;

    /** 成立日期 */
    private String establishedDate;

    /** 企业性质 */
    private String enterpriseNature;

    /** 注册地址 */
    private String address;

    /** 经营范围 */
    private String businessScope;

    /** 企业状态（存续/注销/吊销等） */
    private String enterpriseStatus;

    /** 数据来源 */
    private String dataSource;

    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }
    public String getCreditCode() { return creditCode; }
    public void setCreditCode(String creditCode) { this.creditCode = creditCode; }
    public String getLegalPerson() { return legalPerson; }
    public void setLegalPerson(String legalPerson) { this.legalPerson = legalPerson; }
    public BigDecimal getRegisteredCapital() { return registeredCapital; }
    public void setRegisteredCapital(BigDecimal registeredCapital) { this.registeredCapital = registeredCapital; }
    public String getEstablishedDate() { return establishedDate; }
    public void setEstablishedDate(String establishedDate) { this.establishedDate = establishedDate; }
    public String getEnterpriseNature() { return enterpriseNature; }
    public void setEnterpriseNature(String enterpriseNature) { this.enterpriseNature = enterpriseNature; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getBusinessScope() { return businessScope; }
    public void setBusinessScope(String businessScope) { this.businessScope = businessScope; }
    public String getEnterpriseStatus() { return enterpriseStatus; }
    public void setEnterpriseStatus(String enterpriseStatus) { this.enterpriseStatus = enterpriseStatus; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
}
