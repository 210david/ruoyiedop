package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 会计期间 fin_period
 *
 * <p>状态 OPEN未结账/CLOSED已结账。已结账期间不允许新增或修改凭证，
 * 需先反结账（解锁）才能操作——这是账簿封期的基本约束。</p>
 *
 * @author ruoyi
 */
public class FinPeriod extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 状态：未结账 */
    public static final String STATUS_OPEN = "OPEN";

    /** 状态：已结账 */
    public static final String STATUS_CLOSED = "CLOSED";

    /** 期间ID */
    private Long periodId;

    /** 预留账套ID */
    private Long companyId;

    /** 年月 yyyyMM */
    private String yearMonth;

    /** 期间名称 */
    private String periodName;

    /** 状态 OPEN/CLOSED */
    private String status;

    /** 结账人 */
    private String closeBy;

    /** 结账时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;

    /** 反结账人 */
    private String unlockBy;

    /** 反结账时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date unlockTime;

    /** 反结账原因 */
    private String unlockReason;

    public Long getPeriodId() { return periodId; }
    public void setPeriodId(Long periodId) { this.periodId = periodId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public String getPeriodName() { return periodName; }
    public void setPeriodName(String periodName) { this.periodName = periodName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCloseBy() { return closeBy; }
    public void setCloseBy(String closeBy) { this.closeBy = closeBy; }

    public Date getCloseTime() { return closeTime; }
    public void setCloseTime(Date closeTime) { this.closeTime = closeTime; }

    public String getUnlockBy() { return unlockBy; }
    public void setUnlockBy(String unlockBy) { this.unlockBy = unlockBy; }

    public Date getUnlockTime() { return unlockTime; }
    public void setUnlockTime(Date unlockTime) { this.unlockTime = unlockTime; }

    public String getUnlockReason() { return unlockReason; }
    public void setUnlockReason(String unlockReason) { this.unlockReason = unlockReason; }
}
