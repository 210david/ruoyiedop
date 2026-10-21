package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 联系人 mk_contact
 *
 * @author ruoyi
 */
public class MkContact extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 联系人ID */
    private Long contactId;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "所属客户")
    private String customerName;

    /** 姓名 */
    @Excel(name = "姓名")
    private String name;

    /** 性别 */
    @Excel(name = "性别", dictType = "sys_user_sex")
    private String gender;

    /** 职位 */
    @Excel(name = "职位")
    private String position;

    /** 所属部门 */
    private String department;

    /** 角色标签 */
    @Excel(name = "角色标签")
    private String roleTag;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 微信号 */
    private String wechat;

    /** QQ号 */
    private String qq;

    /** 是否关键联系人 */
    @Excel(name = "关键联系人", readConverterExp = "0=否,1=是")
    private String isKey;

    /** 是否主要联系人 */
    private String isPrimary;

    /** 归属销售ID */
    private Long ownerUserId;

    /** 归属销售名称 */
    private String ownerUserName;

    /** 最后联系时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastContactTime;

    /** 下次联系时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextContactTime;

    /** 个人特点 */
    private String personalTrait;

    /** 删除标志 */
    private String delFlag;

    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRoleTag() { return roleTag; }
    public void setRoleTag(String roleTag) { this.roleTag = roleTag; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWechat() { return wechat; }
    public void setWechat(String wechat) { this.wechat = wechat; }
    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }
    public String getIsKey() { return isKey; }
    public void setIsKey(String isKey) { this.isKey = isKey; }
    public String getIsPrimary() { return isPrimary; }
    public void setIsPrimary(String isPrimary) { this.isPrimary = isPrimary; }
    public String getPersonalTrait() { return personalTrait; }
    public void setPersonalTrait(String personalTrait) { this.personalTrait = personalTrait; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerUserName() { return ownerUserName; }
    public void setOwnerUserName(String ownerUserName) { this.ownerUserName = ownerUserName; }
    public Date getLastContactTime() { return lastContactTime; }
    public void setLastContactTime(Date lastContactTime) { this.lastContactTime = lastContactTime; }
    public Date getNextContactTime() { return nextContactTime; }
    public void setNextContactTime(Date nextContactTime) { this.nextContactTime = nextContactTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
