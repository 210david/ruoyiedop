package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 结账检查记录 fin_closing
 *
 * <p>每次结账把N 项检查结果逐条留痕，便于事后追溯
 * 「当时为什么能结账」。反结账时清理该期间记录。</p>
 *
 * @author ruoyi
 */
public class FinClosing extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结账记录ID */
    private Long closingId;

    /** 预留账套ID */
    private Long companyId;

    /** 会计期间 yyyyMM */
    private String yearMonth;

    /** 归属部门ID */
    private Long deptId;

    /** 检查项编码 */
    private String checkCode;

    /** 检查项名称 */
    private String checkName;

    /** 检查结果 0未通过 1通过 */
    private String checkResult;

    /** 检查详情（失败原因） */
    private String checkDetail;

    /** 检查人 */
    private String checkBy;

    /** 检查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    public Long getClosingId() { return closingId; }
    public void setClosingId(Long closingId) { this.closingId = closingId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getCheckCode() { return checkCode; }
    public void setCheckCode(String checkCode) { this.checkCode = checkCode; }

    public String getCheckName() { return checkName; }
    public void setCheckName(String checkName) { this.checkName = checkName; }

    public String getCheckResult() { return checkResult; }
    public void setCheckResult(String checkResult) { this.checkResult = checkResult; }

    public String getCheckDetail() { return checkDetail; }
    public void setCheckDetail(String checkDetail) { this.checkDetail = checkDetail; }

    public String getCheckBy() { return checkBy; }
    public void setCheckBy(String checkBy) { this.checkBy = checkBy; }

    public Date getCheckTime() { return checkTime; }
    public void setCheckTime(Date checkTime) { this.checkTime = checkTime; }
}
