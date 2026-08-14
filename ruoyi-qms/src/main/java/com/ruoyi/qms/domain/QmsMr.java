package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsMr extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long mrId;
    @Excel(name = "评审编号") private String mrNo;
    @Excel(name = "评审标题") private String mrTitle;
    @Excel(name = "年度") private Integer mrYear;
    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "评审日期", dateFormat = "yyyy-MM-dd") private Date mrDate;
    @Excel(name = "主持人") private String chairperson;
    private String participants;
    private String inputSummary;
    private String resolution;
    private String actionItems;
    @Excel(name = "状态", dictType = "qms_mr_status") private String mrStatus;
    private String delFlag;
    private String status;

    public Long getMrId() { return mrId; }
    public void setMrId(Long mrId) { this.mrId = mrId; }
    public String getMrNo() { return mrNo; }
    public void setMrNo(String mrNo) { this.mrNo = mrNo; }
    public String getMrTitle() { return mrTitle; }
    public void setMrTitle(String mrTitle) { this.mrTitle = mrTitle; }
    public Integer getMrYear() { return mrYear; }
    public void setMrYear(Integer mrYear) { this.mrYear = mrYear; }
    public Date getMrDate() { return mrDate; }
    public void setMrDate(Date mrDate) { this.mrDate = mrDate; }
    public String getChairperson() { return chairperson; }
    public void setChairperson(String chairperson) { this.chairperson = chairperson; }
    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }
    public String getInputSummary() { return inputSummary; }
    public void setInputSummary(String inputSummary) { this.inputSummary = inputSummary; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public String getActionItems() { return actionItems; }
    public void setActionItems(String actionItems) { this.actionItems = actionItems; }
    public String getMrStatus() { return mrStatus; }
    public void setMrStatus(String mrStatus) { this.mrStatus = mrStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
