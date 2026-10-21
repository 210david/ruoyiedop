package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsDocDistribute extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long distributeId;
    private Long docId;
    @Excel(name = "文档编号") private String docNo;
    @Excel(name = "版本号") private String versionNo;
    @Excel(name = "类型", dictType = "qms_doc_distribute_type") private String distributeType;
    private Long deptId;
    @Excel(name = "部门") private String deptName;
    private Long userId;
    @Excel(name = "签收人") private String userName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date receiveTime;
    @JsonFormat(pattern = "yyyy-MM-dd") private Date borrowDeadline;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date returnTime;
    @Excel(name = "状态", dictType = "qms_doc_dist_status") private String distStatus;
    private String watermark;

    public Long getDistributeId() { return distributeId; }
    public void setDistributeId(Long distributeId) { this.distributeId = distributeId; }
    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getDistributeType() { return distributeType; }
    public void setDistributeType(String distributeType) { this.distributeType = distributeType; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Date getReceiveTime() { return receiveTime; }
    public void setReceiveTime(Date receiveTime) { this.receiveTime = receiveTime; }
    public Date getBorrowDeadline() { return borrowDeadline; }
    public void setBorrowDeadline(Date borrowDeadline) { this.borrowDeadline = borrowDeadline; }
    public Date getReturnTime() { return returnTime; }
    public void setReturnTime(Date returnTime) { this.returnTime = returnTime; }
    public String getDistStatus() { return distStatus; }
    public void setDistStatus(String distStatus) { this.distStatus = distStatus; }
    public String getWatermark() { return watermark; }
    public void setWatermark(String watermark) { this.watermark = watermark; }
}
