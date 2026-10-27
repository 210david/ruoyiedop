package com.ruoyi.bid.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 投标项目对象 bid_project
 */
public class BidProject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long projectId;
    @Excel(name = "投标编号")
    private String bidNo;
    private Long noticeId;
    @Excel(name = "投标项目名称")
    private String projectName;
    @Excel(name = "招标人")
    private String tenderee;
    private String agent;
    private String regionCode;
    @Excel(name = "行业分类", dictType = "bid_industry")
    private String industry;
    @Excel(name = "项目预算")
    private BigDecimal budgetAmount;
    @Excel(name = "我方报价")
    private BigDecimal bidAmount;
    /** 投标状态（字典 bid_project_status） */
    @Excel(name = "投标状态", dictType = "bid_project_status")
    private String status;
    /** 承接方式（字典 bid_join_type） */
    @Excel(name = "承接方式", dictType = "bid_join_type")
    private String joinType;
    private String partner;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date applyDeadline;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "递交截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitDeadline;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开标时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date openTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结果公示时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date resultTime;
    /** 未中标/废标原因（字典 bid_fail_reason） */
    @Excel(name = "未中标原因", dictType = "bid_fail_reason")
    private String failReason;
    private Long ownerUserId;
    @Excel(name = "负责人")
    private String ownerName;
    private Long deptId;
    private String delFlag;

    /** 进度节点列表（子表） */
    private List<BidProjectNode> nodeList = new ArrayList<>();

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getBidNo() { return bidNo; }
    public void setBidNo(String bidNo) { this.bidNo = bidNo; }
    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getTenderee() { return tenderee; }
    public void setTenderee(String tenderee) { this.tenderee = tenderee; }
    public String getAgent() { return agent; }
    public void setAgent(String agent) { this.agent = agent; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getJoinType() { return joinType; }
    public void setJoinType(String joinType) { this.joinType = joinType; }
    public String getPartner() { return partner; }
    public void setPartner(String partner) { this.partner = partner; }
    public Date getApplyDeadline() { return applyDeadline; }
    public void setApplyDeadline(Date applyDeadline) { this.applyDeadline = applyDeadline; }
    public Date getSubmitDeadline() { return submitDeadline; }
    public void setSubmitDeadline(Date submitDeadline) { this.submitDeadline = submitDeadline; }
    public Date getOpenTime() { return openTime; }
    public void setOpenTime(Date openTime) { this.openTime = openTime; }
    public Date getResultTime() { return resultTime; }
    public void setResultTime(Date resultTime) { this.resultTime = resultTime; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<BidProjectNode> getNodeList() { return nodeList; }
    public void setNodeList(List<BidProjectNode> nodeList) { this.nodeList = nodeList; }
}
