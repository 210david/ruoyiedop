package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 电子签名记录表 qms_esig_record
 *
 * 用于合规行业（GMP/HACCP）的电子签名留痕，
 * 记录检验判定、NCR审批等关键操作的电子签名。
 *
 * @author ruoyi
 */
public class QmsEsigRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 签名记录ID */
    private Long esigId;

    /** 业务类型（insp_judge=检验判定, ncr_approve=NCR审批, ncr_disposition=NCR处置, ncr_verify=NCR验证） */
    @Excel(name = "业务类型")
    private String bizType;

    /** 业务ID（检验任务ID / NCR ID） */
    private Long bizId;

    /** 业务编号（任务编号 / NCR编号） */
    @Excel(name = "业务编号")
    private String bizNo;

    /** 签名用户ID */
    private Long userId;

    /** 签名用户名 */
    @Excel(name = "签名用户")
    private String userName;

    /** 签名用户昵称 */
    @Excel(name = "签名用户姓名")
    private String nickName;

    /** 签名含义（如：检验员判定、质量经理审批、处置执行等） */
    @Excel(name = "签名含义")
    private String sigMeaning;

    /** 签名意见/备注 */
    @Excel(name = "签名意见")
    private String sigOpinion;

    /** 签名结果（pass=通过, fail=不通过, reject=驳回） */
    @Excel(name = "签名结果")
    private String sigResult;

    /** 签名IP地址 */
    @Excel(name = "IP地址")
    private String sigIp;

    /** 签名时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "签名时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date sigTime;

    public Long getEsigId() { return esigId; }
    public void setEsigId(Long esigId) { this.esigId = esigId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getSigMeaning() { return sigMeaning; }
    public void setSigMeaning(String sigMeaning) { this.sigMeaning = sigMeaning; }
    public String getSigOpinion() { return sigOpinion; }
    public void setSigOpinion(String sigOpinion) { this.sigOpinion = sigOpinion; }
    public String getSigResult() { return sigResult; }
    public void setSigResult(String sigResult) { this.sigResult = sigResult; }
    public String getSigIp() { return sigIp; }
    public void setSigIp(String sigIp) { this.sigIp = sigIp; }
    public Date getSigTime() { return sigTime; }
    public void setSigTime(Date sigTime) { this.sigTime = sigTime; }
}
