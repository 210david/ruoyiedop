package com.ruoyi.ai.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI调用审计日志 ai_call_log
 * 统计/追溯/日志类：数据权限豁免（仅AI中心权限控制，管理员/管理层可查）
 *
 * @author ruoyi
 */
public class AiCallLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 业务模块（dms/qms/pms/mms/wms/mk/hr/safety/platform） */
    @Excel(name = "模块")
    private String module;

    /** 功能标识 */
    @Excel(name = "功能")
    private String feature;

    /** 操作人ID */
    private Long userId;

    /** 操作人名称 */
    @Excel(name = "操作人")
    private String userName;

    /** 状态（0成功 1降级 2失败 3限额拦截） */
    @Excel(name = "状态", readConverterExp = "0=成功,1=降级,2=失败,3=限额拦截")
    private String status;

    /** 耗时（毫秒） */
    @Excel(name = "耗时(ms)")
    private Integer durationMs;

    /** 提示词摘要（脱敏后前500字） */
    private String promptDigest;

    /** 错误信息 */
    private String errorMsg;

    /** 调用时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "调用时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date callTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getFeature() { return feature; }
    public void setFeature(String feature) { this.feature = feature; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }
    public String getPromptDigest() { return promptDigest; }
    public void setPromptDigest(String promptDigest) { this.promptDigest = promptDigest; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public Date getCallTime() { return callTime; }
    public void setCallTime(Date callTime) { this.callTime = callTime; }
}
