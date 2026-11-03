package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 经销商专项报表对象（P2-02：分经销商销量/返利/账龄/流向汇总）
 *
 * @author ruoyi
 */
public class MkDealerReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 客户ID */
    private Long customerId;

    /** 客户编号 */
    @Excel(name = "客户编号")
    private String customerNo;

    /** 经销商名称 */
    @Excel(name = "经销商名称")
    private String customerName;

    /** 经销层级（1一级 2二级） */
    @Excel(name = "经销层级", dictType = "mk_dealer_level")
    private String dealerLevel;

    /** 授权区域 */
    @Excel(name = "授权区域")
    private String authRegions;

    /** 信用状态（0正常 1冻结） */
    @Excel(name = "信用状态", dictType = "mk_credit_status")
    private String creditStatus;

    /** 档案状态（0正常 1停用，查询条件） */
    private String status;

    /** 有效订单数（统计期内） */
    @Excel(name = "有效订单数")
    private Long orderCount;

    /** 销售额（统计期内有效订单合计） */
    @Excel(name = "销售额")
    private BigDecimal orderAmount;

    /** 销量（统计期内发货数量合计） */
    @Excel(name = "销量")
    private BigDecimal shippedQty;

    /** 累计返利 */
    @Excel(name = "累计返利")
    private BigDecimal rebateTotal;

    /** 已兑现返利 */
    @Excel(name = "已兑现返利")
    private BigDecimal redeemedAmount;

    /** 已冲抵返利 */
    @Excel(name = "已冲抵返利")
    private BigDecimal offsetAmount;

    /** 未结返利（未全部冲抵余额） */
    @Excel(name = "未结返利")
    private BigDecimal unsettledRebate;

    /** 应收余额（未收合计） */
    @Excel(name = "应收余额")
    private BigDecimal receivableAmount;

    /** 逾期金额（计划回款日已过且未收） */
    @Excel(name = "逾期金额")
    private BigDecimal overdueAmount;

    /** 最长逾期天数 */
    @Excel(name = "最长逾期天数")
    private Long worstAgingDays;

    /** 流向登记条数（统计期内） */
    @Excel(name = "流向条数")
    private Long flowCount;

    /** 流向数量合计（统计期内） */
    @Excel(name = "流向数量")
    private BigDecimal flowQty;

    /** 下游客户数（去重） */
    @Excel(name = "下游客户数")
    private Long downstreamCount;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getDealerLevel() { return dealerLevel; }
    public void setDealerLevel(String dealerLevel) { this.dealerLevel = dealerLevel; }
    public String getAuthRegions() { return authRegions; }
    public void setAuthRegions(String authRegions) { this.authRegions = authRegions; }
    public String getCreditStatus() { return creditStatus; }
    public void setCreditStatus(String creditStatus) { this.creditStatus = creditStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOrderCount() { return orderCount; }
    public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public BigDecimal getShippedQty() { return shippedQty; }
    public void setShippedQty(BigDecimal shippedQty) { this.shippedQty = shippedQty; }
    public BigDecimal getRebateTotal() { return rebateTotal; }
    public void setRebateTotal(BigDecimal rebateTotal) { this.rebateTotal = rebateTotal; }
    public BigDecimal getRedeemedAmount() { return redeemedAmount; }
    public void setRedeemedAmount(BigDecimal redeemedAmount) { this.redeemedAmount = redeemedAmount; }
    public BigDecimal getOffsetAmount() { return offsetAmount; }
    public void setOffsetAmount(BigDecimal offsetAmount) { this.offsetAmount = offsetAmount; }
    public BigDecimal getUnsettledRebate() { return unsettledRebate; }
    public void setUnsettledRebate(BigDecimal unsettledRebate) { this.unsettledRebate = unsettledRebate; }
    public BigDecimal getReceivableAmount() { return receivableAmount; }
    public void setReceivableAmount(BigDecimal receivableAmount) { this.receivableAmount = receivableAmount; }
    public BigDecimal getOverdueAmount() { return overdueAmount; }
    public void setOverdueAmount(BigDecimal overdueAmount) { this.overdueAmount = overdueAmount; }
    public Long getWorstAgingDays() { return worstAgingDays; }
    public void setWorstAgingDays(Long worstAgingDays) { this.worstAgingDays = worstAgingDays; }
    public Long getFlowCount() { return flowCount; }
    public void setFlowCount(Long flowCount) { this.flowCount = flowCount; }
    public BigDecimal getFlowQty() { return flowQty; }
    public void setFlowQty(BigDecimal flowQty) { this.flowQty = flowQty; }
    public Long getDownstreamCount() { return downstreamCount; }
    public void setDownstreamCount(Long downstreamCount) { this.downstreamCount = downstreamCount; }
}
