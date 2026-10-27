package com.ruoyi.fin.domain.vo;

/**
 * 自动记账扫描结果
 *
 * @author ruoyi
 */
public class AutoVoucherScanResult
{
    /** 本次生成凭证总张数 */
    private int total;

    /** 进项发票处理笔数 */
    private int invoiceCount;

    /** 发货单处理笔数 */
    private int shipmentCount;

    /** 出库单处理笔数 */
    private int outboundCount;

    /** 薪酬发放处理笔数 */
    private int payrollCount;

    /** 失败笔数 */
    private int failedCount;

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getInvoiceCount() { return invoiceCount; }
    public void setInvoiceCount(int invoiceCount) { this.invoiceCount = invoiceCount; }

    public int getShipmentCount() { return shipmentCount; }
    public void setShipmentCount(int shipmentCount) { this.shipmentCount = shipmentCount; }

    public int getOutboundCount() { return outboundCount; }
    public void setOutboundCount(int outboundCount) { this.outboundCount = outboundCount; }

    public int getPayrollCount() { return payrollCount; }
    public void setPayrollCount(int payrollCount) { this.payrollCount = payrollCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    /**
     * 累加一张凭证的处理结果。
     *
     * @param sourceType 来源单据类型
     */
    public void addOne(String sourceType)
    {
        this.total++;
        if ("invoice".equals(sourceType))
        {
            this.invoiceCount++;
        }
        else if ("shipment".equals(sourceType))
        {
            this.shipmentCount++;
        }
        else if ("outbound".equals(sourceType))
        {
            this.outboundCount++;
        }
        else if ("payroll".equals(sourceType))
        {
            this.payrollCount++;
        }
    }
}
