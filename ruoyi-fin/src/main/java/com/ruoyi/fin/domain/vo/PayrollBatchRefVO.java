package com.ruoyi.fin.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 跨模块只读视图对象 —— 薪酬发放批次（应付职工薪酬来源）
 *
 * <p>对应 biz_hr_payroll_batch，audit_status='3' 表示已发放。</p>
 *
 * @author ruoyi
 */
public class PayrollBatchRefVO
{
    /** 批次ID */
    private Long batchId;

    /** 批次编号 */
    private String batchNo;

    /** 年月 yyyy-MM */
    private String yearMonth;

    /** 发放日期 */
    private Date payDate;

    /** 总人数 */
    private Integer totalCount;

    /** 总金额（应付职工薪酬计提依据） */
    private BigDecimal totalAmount;

    /** 发薪银行 */
    private String bankName;

    /** 银行回单号 */
    private String bankReceiptNo;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public Date getPayDate() { return payDate; }
    public void setPayDate(Date payDate) { this.payDate = payDate; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankReceiptNo() { return bankReceiptNo; }
    public void setBankReceiptNo(String bankReceiptNo) { this.bankReceiptNo = bankReceiptNo; }
}
