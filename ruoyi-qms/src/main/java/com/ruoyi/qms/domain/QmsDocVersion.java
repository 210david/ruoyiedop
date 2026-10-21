package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsDocVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long versionId;
    private Long docId;
    @Excel(name = "文档编号") private String docNo;
    @Excel(name = "版本号") private String versionNo;
    private String fileUrl;
    private String changeLog;
    @Excel(name = "版本状态") private String versionStatus;
    private String publishBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date publishTime;
    private String obsoleteBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date obsoleteTime;
    private String esigUser;
    private String esigMeaning;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date esigTime;

    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getChangeLog() { return changeLog; }
    public void setChangeLog(String changeLog) { this.changeLog = changeLog; }
    public String getVersionStatus() { return versionStatus; }
    public void setVersionStatus(String versionStatus) { this.versionStatus = versionStatus; }
    public String getPublishBy() { return publishBy; }
    public void setPublishBy(String publishBy) { this.publishBy = publishBy; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public String getObsoleteBy() { return obsoleteBy; }
    public void setObsoleteBy(String obsoleteBy) { this.obsoleteBy = obsoleteBy; }
    public Date getObsoleteTime() { return obsoleteTime; }
    public void setObsoleteTime(Date obsoleteTime) { this.obsoleteTime = obsoleteTime; }
    public String getEsigUser() { return esigUser; }
    public void setEsigUser(String esigUser) { this.esigUser = esigUser; }
    public String getEsigMeaning() { return esigMeaning; }
    public void setEsigMeaning(String esigMeaning) { this.esigMeaning = esigMeaning; }
    public Date getEsigTime() { return esigTime; }
    public void setEsigTime(Date esigTime) { this.esigTime = esigTime; }
}
