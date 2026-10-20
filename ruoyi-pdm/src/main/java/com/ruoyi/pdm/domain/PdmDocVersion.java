package com.ruoyi.pdm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 设计资料版本对象 pdm_doc_version
 *
 * @author ruoyi
 */
public class PdmDocVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 版本ID */
    private Long versionId;

    /** 资料类型（字典 pdm_version_biz_type） */
    @Excel(name = "资料类型", dictType = "pdm_version_biz_type")
    private String bizType;

    /** 资料业务ID */
    private Long bizId;

    /** 版本号（系统自动分配，如 V1.0） */
    @Excel(name = "版本号")
    private String versionNo;

    /** 版本说明 */
    private String versionDesc;

    /** 生效标志（0=历史版本,1=生效版本） */
    @Excel(name = "生效标志", readConverterExp = "0=历史版本,1=生效版本")
    private String effectiveFlag;

    /** 生效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTime;

    /** 失效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date invalidTime;

    /** 生效依据（字典 pdm_version_effect_basis） */
    @Excel(name = "生效依据", dictType = "pdm_version_effect_basis")
    private String effectBasis;

    /** 生效依据单号（定稿方案号/变更单号） */
    @Excel(name = "生效依据单号")
    private String effectRef;

    /** 是否定型版本组合成员（0=否,1=是） */
    private String isFinalSet;

    /** 资料文件相对路径 */
    private String fileUrl;

    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getVersionDesc() { return versionDesc; }
    public void setVersionDesc(String versionDesc) { this.versionDesc = versionDesc; }
    public String getEffectiveFlag() { return effectiveFlag; }
    public void setEffectiveFlag(String effectiveFlag) { this.effectiveFlag = effectiveFlag; }
    public Date getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(Date effectiveTime) { this.effectiveTime = effectiveTime; }
    public Date getInvalidTime() { return invalidTime; }
    public void setInvalidTime(Date invalidTime) { this.invalidTime = invalidTime; }
    public String getEffectBasis() { return effectBasis; }
    public void setEffectBasis(String effectBasis) { this.effectBasis = effectBasis; }
    public String getEffectRef() { return effectRef; }
    public void setEffectRef(String effectRef) { this.effectRef = effectRef; }
    public String getIsFinalSet() { return isFinalSet; }
    public void setIsFinalSet(String isFinalSet) { this.isFinalSet = isFinalSet; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
