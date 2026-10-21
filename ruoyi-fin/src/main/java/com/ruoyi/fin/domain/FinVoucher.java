package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 记账凭证主表 fin_voucher
 *
 * <p>【P0-3 幂等】唯一键 (source_module, source_type, source_id, rule_key, del_flag)
 * 自动记账重复触发时由数据库拦截，Service 捕获 DuplicateKeyException 后返回已有凭证。</p>
 *
 * <p>【P0-4 期间】period 由 voucher_date 派生，不允许手工指定。</p>
 *
 * <p>【数据权限】dept_id + user_id 供 BizDeptFillAspect 自动填充与 @DataScope 过滤。</p>
 *
 * @author ruoyi
 */
public class FinVoucher extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 状态：草稿 */
    public static final String STATUS_DRAFT = "DRAFT";

    /** 状态：已审核 */
    public static final String STATUS_AUDITED = "AUDITED";

    /** 状态：已记账 */
    public static final String STATUS_POSTED = "POSTED";

    /** 状态：已冲销 */
    public static final String STATUS_CANCELLED = "CANCELLED";

    /** 类型：手工 */
    public static final String TYPE_MANUAL = "MANUAL";

    /** 类型：自动 */
    public static final String TYPE_AUTO = "AUTO";

    /** 类型：红字冲销 */
    public static final String TYPE_CANCEL = "CANCEL";

    /** 凭证ID */
    private Long voucherId;

    /** 凭证号 */
    @Excel(name = "凭证号")
    private String voucherNo;

    /** 预留账套ID */
    private Long companyId;

    /** 凭证日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "凭证日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date voucherDate;

    /** 会计期间 YYYYMM（由 voucher_date 派生，不可手填） */
    @Excel(name = "会计期间")
    private String period;

    /** 类型 MANUAL/AUTO/CANCEL */
    @Excel(name = "凭证类型", readConverterExp = "MANUAL=手工,AUTO=自动,CANCEL=红字冲销")
    private String voucherType;

    /** 来源模块 */
    private String sourceModule;

    /** 来源单据类型 */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据号 */
    @Excel(name = "来源单据号")
    private String sourceKey;

    /** 命中的记账规则键（手工为空串） */
    private String ruleKey;

    /** 价税合计 */
    @Excel(name = "价税合计")
    private BigDecimal totalAmount;

    /** 分录行数 */
    private Integer entryCount;

    /** 状态 DRAFT/AUDITED/POSTED/CANCELLED */
    @Excel(name = "状态", readConverterExp = "DRAFT=草稿,AUDITED=已审核,POSTED=已记账,CANCELLED=已冲销")
    private String status;

    /** 附件数 */
    private Integer attachCount;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditOpinion;

    /** 归属部门ID（BizDeptFillAspect 自动填充） */
    private Long deptId;

    /** 制单人ID（@DataScope userAlias 用） */
    private Long userId;

    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }

    public String getVoucherNo() { return voucherNo; }
    public void setVoucherNo(String voucherNo) { this.voucherNo = voucherNo; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Date getVoucherDate() { return voucherDate; }
    public void setVoucherDate(Date voucherDate) { this.voucherDate = voucherDate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getVoucherType() { return voucherType; }
    public void setVoucherType(String voucherType) { this.voucherType = voucherType; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }

    public String getRuleKey() { return ruleKey; }
    public void setRuleKey(String ruleKey) { this.ruleKey = ruleKey; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Integer getEntryCount() { return entryCount; }
    public void setEntryCount(Integer entryCount) { this.entryCount = entryCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getAttachCount() { return attachCount; }
    public void setAttachCount(Integer attachCount) { this.attachCount = attachCount; }

    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }

    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }

    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
