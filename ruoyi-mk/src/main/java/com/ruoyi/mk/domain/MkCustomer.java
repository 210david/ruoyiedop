package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 企业客户 mk_customer
 *
 * @author ruoyi
 */
public class MkCustomer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 客户ID */
    private Long customerId;

    /** 客户编号 */
    @Excel(name = "客户编号")
    private String customerNo;

    /** 企业名称 */
    @Excel(name = "企业名称")
    private String customerName;

    /** 统一社会信用代码 */
    @Excel(name = "统一社会信用代码")
    private String creditCode;

    /** 客户等级 */
    @Excel(name = "客户等级", dictType = "marketing_customer_level")
    private String customerLevel;

    /** 客户来源 */
    private String customerSource;

    /** 所属行业 */
    private String industry;

    /** 企业规模 */
    private String companySize;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区 */
    private String district;

    /** 详细地址 */
    private String address;

    /** 官网 */
    private String website;

    /** 客户状态 */
    @Excel(name = "客户状态", dictType = "marketing_customer_status")
    private String customerStatus;

    /** 首次接触时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "首次接触时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date firstContactDate;

    /** 合作开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合作开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date cooperationDate;

    /** 累计交易额 */
    @Excel(name = "累计交易额")
    private BigDecimal totalAmount;

    /** 负责人ID */
    private Long userId;

    /** 负责人名称 */
    @Excel(name = "负责人")
    private String userName;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    @Excel(name = "部门")
    private String deptName;

    /** 标签ID数组（非数据库字段，用于表单提交） */
    private Long[] tagIds;

    /** 标签名称（非数据库字段，用于列表展示） */
    private String tagNames;

    /** 删除标志 */
    private String delFlag;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCreditCode() { return creditCode; }
    public void setCreditCode(String creditCode) { this.creditCode = creditCode; }
    public String getCustomerLevel() { return customerLevel; }
    public void setCustomerLevel(String customerLevel) { this.customerLevel = customerLevel; }
    public String getCustomerSource() { return customerSource; }
    public void setCustomerSource(String customerSource) { this.customerSource = customerSource; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getCustomerStatus() { return customerStatus; }
    public void setCustomerStatus(String customerStatus) { this.customerStatus = customerStatus; }
    public Date getFirstContactDate() { return firstContactDate; }
    public void setFirstContactDate(Date firstContactDate) { this.firstContactDate = firstContactDate; }
    public Date getCooperationDate() { return cooperationDate; }
    public void setCooperationDate(Date cooperationDate) { this.cooperationDate = cooperationDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long[] getTagIds() { return tagIds; }
    public void setTagIds(Long[] tagIds) { this.tagIds = tagIds; }
    public String getTagNames() { return tagNames; }
    public void setTagNames(String tagNames) { this.tagNames = tagNames; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
