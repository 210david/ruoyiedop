package com.ruoyi.food.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 检验索证索票附件表 food_insp_cert
 *
 * 放行前置：来料检验(IQC)判定放行前必须上传必传证件（动物检疫合格证明/检验报告等）
 *
 * @author ruoyi
 */
public class FoodInspCert extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 附件ID */
    private Long certId;

    /** 检验任务ID(qms_insp_task) */
    private Long taskId;

    /** 检验任务编号 */
    @Excel(name = "检验任务号", width = 20)
    private String taskNo;

    /** 批次号 */
    @Excel(name = "批次号", width = 20)
    private String batchNo;

    /** 证件类型(字典food_cert_type) */
    private String certType;

    /** 证件类型标签(非数据库字段) */
    @Excel(name = "证件类型", width = 18)
    private String certTypeLabel;

    /** 证件编号 */
    @Excel(name = "证件编号", width = 18)
    private String certNo;

    /** 文件名称 */
    @Excel(name = "文件名称", width = 30)
    private String fileName;

    /** 文件地址 */
    private String fileUrl;

    /** 上传人 */
    @Excel(name = "上传人", width = 10)
    private String uploadBy;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上传时间", width = 22, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    /** 删除标志 */
    private String delFlag;

    public Long getCertId() { return certId; }
    public void setCertId(Long certId) { this.certId = certId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getCertType() { return certType; }
    public void setCertType(String certType) { this.certType = certType; }
    public String getCertTypeLabel() { return certTypeLabel; }
    public void setCertTypeLabel(String certTypeLabel) { this.certTypeLabel = certTypeLabel; }
    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getUploadBy() { return uploadBy; }
    public void setUploadBy(String uploadBy) { this.uploadBy = uploadBy; }
    public Date getUploadTime() { return uploadTime; }
    public void setUploadTime(Date uploadTime) { this.uploadTime = uploadTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
