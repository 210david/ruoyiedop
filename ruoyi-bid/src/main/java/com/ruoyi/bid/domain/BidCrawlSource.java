package com.ruoyi.bid.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采集源配置对象 bid_crawl_source
 */
public class BidCrawlSource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long sourceId;
    @Excel(name = "采集源名称")
    private String sourceName;
    /** 采集源类型（字典 bid_source_type） */
    @Excel(name = "采集源类型", dictType = "bid_source_type")
    private String sourceType;
    private String baseUrl;
    private String parserCode;
    private String parseConfig;
    private Integer crawlInterval;
    /** 启用状态（字典 sys_normal_disable） */
    private String enabled;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近执行时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastRunTime;
    /** 最近执行结果（字典 bid_crawl_status） */
    @Excel(name = "最近执行结果", dictType = "bid_crawl_status")
    private String lastRunStatus;
    @Excel(name = "最近执行说明")
    private String lastRunMsg;
    private String delFlag;

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getParserCode() { return parserCode; }
    public void setParserCode(String parserCode) { this.parserCode = parserCode; }
    public String getParseConfig() { return parseConfig; }
    public void setParseConfig(String parseConfig) { this.parseConfig = parseConfig; }
    public Integer getCrawlInterval() { return crawlInterval; }
    public void setCrawlInterval(Integer crawlInterval) { this.crawlInterval = crawlInterval; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Date getLastRunTime() { return lastRunTime; }
    public void setLastRunTime(Date lastRunTime) { this.lastRunTime = lastRunTime; }
    public String getLastRunStatus() { return lastRunStatus; }
    public void setLastRunStatus(String lastRunStatus) { this.lastRunStatus = lastRunStatus; }
    public String getLastRunMsg() { return lastRunMsg; }
    public void setLastRunMsg(String lastRunMsg) { this.lastRunMsg = lastRunMsg; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
