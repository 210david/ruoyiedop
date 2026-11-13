package com.ruoyi.integration.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 集成同步日志
 *
 * @author ruoyi
 */
public class IntegrationSyncLog
{
    private static final long serialVersionUID = 1L;

    private Long logId;

    /** 连接器类型 */
    private String connectorType;

    /** 业务类型（attendance_sync / contact_sync） */
    private String bizType;

    /** 业务关键字（日期范围等） */
    private String bizKey;

    /** 状态（0成功 1失败） */
    private String status;

    /** 结果摘要（成功条数/失败原因） */
    private String resultMsg;

    /** 耗时（毫秒） */
    private Long costMs;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getBizKey() { return bizKey; }
    public void setBizKey(String bizKey) { this.bizKey = bizKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultMsg() { return resultMsg; }
    public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }
    public Long getCostMs() { return costMs; }
    public void setCostMs(Long costMs) { this.costMs = costMs; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
