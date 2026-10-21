package com.ruoyi.bid.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资质资料对象 bid_qualification
 */
public class BidQualification extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long qualificationId;
    /** 资料类型（字典 bid_qual_type） */
    @Excel(name = "资料类型", dictType = "bid_qual_type")
    private String qualType;
    @Excel(name = "名称")
    private String name;
    @Excel(name = "证书编号")
    private String certNo;
    @Excel(name = "资质等级")
    private String qualLevel;
    /** 归属（1企业 2人员） */
    @Excel(name = "归属", readConverterExp = "1=企业,2=人员")
    private String ownerType;
    private Long ownerUserId;
    @Excel(name = "持有人")
    private String ownerName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发证日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date issueDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期至", width = 20, dateFormat = "yyyy-MM-dd")
    private Date expireDate;
    private Integer warnDays;
    private String fileUrl;
    private Long deptId;
    private String delFlag;

    /** 到期剩余天数（非库字段，展示用） */
    private Long remainDays;

    /** 到期状态筛选（非库字段：soon30/soon60/long） */
    private String expireStatus;

    public String getExpireStatus() { return expireStatus; }
    public void setExpireStatus(String expireStatus) { this.expireStatus = expireStatus; }

    public Long getQualificationId() { return qualificationId; }
    public void setQualificationId(Long qualificationId) { this.qualificationId = qualificationId; }
    public String getQualType() { return qualType; }
    public void setQualType(String qualType) { this.qualType = qualType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }
    public String getQualLevel() { return qualLevel; }
    public void setQualLevel(String qualLevel) { this.qualLevel = qualLevel; }
    public String getOwnerType() { return ownerType; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public Integer getWarnDays() { return warnDays; }
    public void setWarnDays(Integer warnDays) { this.warnDays = warnDays; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Long getRemainDays() { return remainDays; }
    public void setRemainDays(Long remainDays) { this.remainDays = remainDays; }
}
