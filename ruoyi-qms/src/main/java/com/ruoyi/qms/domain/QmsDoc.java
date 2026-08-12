package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsDoc extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long docId;
    @Excel(name = "文档编号") private String docNo;
    @Excel(name = "文档标题") private String docTitle;
    @Excel(name = "分类") private String docCategory;
    @Excel(name = "版本") private String versionNo;
    @Excel(name = "状态") private String docStatus;
    private Long deptId;
    @Excel(name = "归属部门") private String deptName;
    private Long tplId;
    @Excel(name = "文件附件") private String fileUrl;
    @JsonFormat(pattern = "yyyy-MM-dd") private Date publishDate;
    @JsonFormat(pattern = "yyyy-MM-dd") private Date obsoleteDate;
    private String delFlag;
    private String status;

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getDocTitle() { return docTitle; }
    public void setDocTitle(String docTitle) { this.docTitle = docTitle; }
    public String getDocCategory() { return docCategory; }
    public void setDocCategory(String docCategory) { this.docCategory = docCategory; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getDocStatus() { return docStatus; }
    public void setDocStatus(String docStatus) { this.docStatus = docStatus; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getTplId() { return tplId; }
    public void setTplId(Long tplId) { this.tplId = tplId; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Date getPublishDate() { return publishDate; }
    public void setPublishDate(Date publishDate) { this.publishDate = publishDate; }
    public Date getObsoleteDate() { return obsoleteDate; }
    public void setObsoleteDate(Date obsoleteDate) { this.obsoleteDate = obsoleteDate; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
