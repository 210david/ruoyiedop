package com.ruoyi.fin.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 坏账计提台账 fin_bad_debt
 *
 * <p>计提方式：</p>
 * <ul>
 *   <li>AGE 按账龄区间比例（默认，比例在 fin_fiscal_config.ar_aging_ratio 配置）</li>
 *   <li>PERIOD 期末按固定比例对全部应收计提</li>
 *   <li>SPECIFIC 单项计提（指定客户全额或指定金额）</li>
 * </ul>
 *
 * @author ruoyi
 */
public class FinBadDebt extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 计提方式：按账龄 */
    public static final String TYPE_AGE = "AGE";

    /** 计提方式：期末按比例 */
    public static final String TYPE_PERIOD = "PERIOD";

    /** 计提方式：单项 */
    public static final String TYPE_SPECIFIC = "SPECIFIC";

    /** 坏账计提ID */
    private Long badDebtId;

    /** 预留账套ID */
    private Long companyId;

    /** 计提单号 */
    @Excel(name = "计提单号")
    private String provisionNo;

    /** 计提所属期间 */
    @Excel(name = "计提期间")
    private String period;

    /** 关联应收单ID（单笔计提） */
    private Long arId;

    /** 客户ID */
    private Long partnerId;

    /** 客户名称 */
    @Excel(name = "客户")
    private String partnerName;

    /** 计提基数（应收账款余额） */
    @Excel(name = "计提基数")
    private BigDecimal receivableAmount;

    /** 计提比例 */
    @Excel(name = "计提比例")
    private BigDecimal provisionRatio;

    /** 本次计提金额 */
    @Excel(name = "计提金额")
    private BigDecimal provisionAmount;

    /** 计提方式 */
    private String provisionType;

    /** 归属部门ID */
    private Long deptId;

    /** 计提人ID */
    private Long userId;

    public Long getBadDebtId() { return badDebtId; }
    public void setBadDebtId(Long badDebtId) { this.badDebtId = badDebtId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getProvisionNo() { return provisionNo; }
    public void setProvisionNo(String provisionNo) { this.provisionNo = provisionNo; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Long getArId() { return arId; }
    public void setArId(Long arId) { this.arId = arId; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public BigDecimal getReceivableAmount() { return receivableAmount; }
    public void setReceivableAmount(BigDecimal receivableAmount) { this.receivableAmount = receivableAmount; }

    public BigDecimal getProvisionRatio() { return provisionRatio; }
    public void setProvisionRatio(BigDecimal provisionRatio) { this.provisionRatio = provisionRatio; }

    public BigDecimal getProvisionAmount() { return provisionAmount; }
    public void setProvisionAmount(BigDecimal provisionAmount) { this.provisionAmount = provisionAmount; }

    public String getProvisionType() { return provisionType; }
    public void setProvisionType(String provisionType) { this.provisionType = provisionType; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}