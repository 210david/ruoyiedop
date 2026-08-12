package com.ruoyi.safety.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

/**
 * 隐患管理对象 biz_safety_hazard
 *
 * @author ruoyi
 */
public class SafetyHazard extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 隐患ID */
    private Long hazardId;

    /** 隐患编号 */
    @Excel(name = "隐患编号")
    private String hazardCode;

    /** 关联风险点ID */
    private Long riskPointId;

    /** 隐患描述 */
    @Excel(name = "隐患描述")
    private String hazardDesc;

    /** 隐患类型 */
    @Excel(name = "隐患类型", readConverterExp = "1=人的不安全行为,2=物的不安全状态,3=管理缺陷,4=环境因素")
    private String hazardType;

    /** 隐患等级 */
    @Excel(name = "隐患等级", readConverterExp = "1=一般,2=重大")
    private String hazardLevel;

    /** 隐患图片 */
    @Excel(name = "隐患图片")
    private String photos;

    /** 发现人ID */
    private Long discoverPersonId;

    /** 发现人 */
    @Excel(name = "发现人")
    private String discoverPerson;

    /** 发现时间 */
    @Excel(name = "发现时间", width = 180)
    private Date discoverTime;

    /** 隐患来源 */
    @Excel(name = "隐患来源", readConverterExp = "1=随手拍,2=计划排查,3=专项排查,4=举报,5=上级交办")
    private String sourceType;

    /** 关联排查任务ID */
    private Long taskId;

    /** 责任部门ID */
    private Long deptId;

    /** 责任部门 */
    @Excel(name = "责任部门")
    private String deptName;

    /** 责任人ID */
    private Long personId;

    /** 责任人 */
    @Excel(name = "责任人")
    private String personName;

    /** 整改要求 */
    @Excel(name = "整改要求")
    private String rectifyReq;

    /** 整改期限 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "整改期限", width = 180, dateFormat = "yyyy-MM-dd")
    private Date rectifyDeadline;

    /** 隐患状态 */
    @Excel(name = "隐患状态", readConverterExp = "0=草稿,1=待审批,2=审批驳回,3=待整改,4=整改中,5=待验收,6=已闭环,7=超期未整改")
    private String hazardStatus;

    /** 整改说明 */
    private String rectifyDesc;

    /** 整改后图片 */
    private String rectifyPhotos;

    /** 整改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "整改时间", width = 180, dateFormat = "yyyy-MM-dd")
    private Date rectifyTime;

    /** 验收人ID */
    private Long verifyPersonId;

    /** 验收人 */
    @Excel(name = "验收人")
    private String verifyPerson;

    /** 验收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "验收时间", width = 180, dateFormat = "yyyy-MM-dd")
    private Date verifyTime;

    /** 验收结果 */
    @Excel(name = "验收结果", readConverterExp = "1=通过,2=不通过")
    private String verifyResult;

    /** 验收意见 */
    private String verifyComment;

    /** 审批人ID */
    private Long approvePersonId;

    /** 审批人 */
    @Excel(name = "审批人")
    private String approvePerson;

    /** 审批时间 */
    @Excel(name = "审批时间", width = 180)
    private Date approveTime;

    /** 审批结果 */
    @Excel(name = "审批结果", readConverterExp = "1=通过,2=不通过")
    private String approveResult;

    /** 审批意见 */
    private String approveComment;

    /** 所属企业ID */
    private Long enterpriseId;

    /** 删除标志 */
    private String delFlag;

    /** 审批日志列表 */
    private List<SafetyHazardAuditLog> auditLogList;

    public Long getHazardId() { return hazardId; }
    public void setHazardId(Long hazardId) { this.hazardId = hazardId; }
    public String getHazardCode() { return hazardCode; }
    public void setHazardCode(String hazardCode) { this.hazardCode = hazardCode; }
    public Long getRiskPointId() { return riskPointId; }
    public void setRiskPointId(Long riskPointId) { this.riskPointId = riskPointId; }
    public String getHazardDesc() { return hazardDesc; }
    public void setHazardDesc(String hazardDesc) { this.hazardDesc = hazardDesc; }
    public String getHazardType() { return hazardType; }
    public void setHazardType(String hazardType) { this.hazardType = hazardType; }
    public String getHazardLevel() { return hazardLevel; }
    public void setHazardLevel(String hazardLevel) { this.hazardLevel = hazardLevel; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
    public Long getDiscoverPersonId() { return discoverPersonId; }
    public void setDiscoverPersonId(Long discoverPersonId) { this.discoverPersonId = discoverPersonId; }
    public String getDiscoverPerson() { return discoverPerson; }
    public void setDiscoverPerson(String discoverPerson) { this.discoverPerson = discoverPerson; }
    public Date getDiscoverTime() { return discoverTime; }
    public void setDiscoverTime(Date discoverTime) { this.discoverTime = discoverTime; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public String getRectifyReq() { return rectifyReq; }
    public void setRectifyReq(String rectifyReq) { this.rectifyReq = rectifyReq; }
    public Date getRectifyDeadline() { return rectifyDeadline; }
    public void setRectifyDeadline(Date rectifyDeadline) { this.rectifyDeadline = rectifyDeadline; }
    public String getHazardStatus() { return hazardStatus; }
    public void setHazardStatus(String hazardStatus) { this.hazardStatus = hazardStatus; }
    public String getRectifyDesc() { return rectifyDesc; }
    public void setRectifyDesc(String rectifyDesc) { this.rectifyDesc = rectifyDesc; }
    public String getRectifyPhotos() { return rectifyPhotos; }
    public void setRectifyPhotos(String rectifyPhotos) { this.rectifyPhotos = rectifyPhotos; }
    public Date getRectifyTime() { return rectifyTime; }
    public void setRectifyTime(Date rectifyTime) { this.rectifyTime = rectifyTime; }
    public Long getVerifyPersonId() { return verifyPersonId; }
    public void setVerifyPersonId(Long verifyPersonId) { this.verifyPersonId = verifyPersonId; }
    public String getVerifyPerson() { return verifyPerson; }
    public void setVerifyPerson(String verifyPerson) { this.verifyPerson = verifyPerson; }
    public Date getVerifyTime() { return verifyTime; }
    public void setVerifyTime(Date verifyTime) { this.verifyTime = verifyTime; }
    public String getVerifyResult() { return verifyResult; }
    public void setVerifyResult(String verifyResult) { this.verifyResult = verifyResult; }
    public String getVerifyComment() { return verifyComment; }
    public void setVerifyComment(String verifyComment) { this.verifyComment = verifyComment; }
    public Long getApprovePersonId() { return approvePersonId; }
    public void setApprovePersonId(Long approvePersonId) { this.approvePersonId = approvePersonId; }
    public String getApprovePerson() { return approvePerson; }
    public void setApprovePerson(String approvePerson) { this.approvePerson = approvePerson; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
    public String getApproveResult() { return approveResult; }
    public void setApproveResult(String approveResult) { this.approveResult = approveResult; }
    public String getApproveComment() { return approveComment; }
    public void setApproveComment(String approveComment) { this.approveComment = approveComment; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<SafetyHazardAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<SafetyHazardAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}