package com.ruoyi.mk.domain.vo;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 营销管理统计报表 VO
 *
 * 用于各类统计查询的通用返回对象，字段按需使用。
 *
 * @author ruoyi
 */
public class MkDashboardVO extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分类名称（阶段名/状态名/行业名/月份等） */
    private String name;

    /** 分类编码 */
    private String code;

    /** 数量 */
    private Integer count;

    /** 金额 */
    private BigDecimal amount;

    /** 加权金额 */
    private BigDecimal weightedAmount;

    /** 赢率 */
    private Integer winRate;

    /** 日期（趋势统计用） */
    private String date;

    /** 用户ID */
    private Long userId;

    /** 用户名称 */
    private String userName;

    /** 客户数 */
    private Integer customerCount;

    /** 线索数 */
    private Integer leadCount;

    /** 商机数 */
    private Integer opportunityCount;

    /** 合同数 */
    private Integer contractCount;

    /** 订单数 */
    private Integer orderCount;

    /** 合同总金额 */
    private BigDecimal contractAmount;

    /** 订单总金额 */
    private BigDecimal orderAmount;

    /** 回款总金额 */
    private BigDecimal paymentAmount;

    /** 商机预计总金额 */
    private BigDecimal expectedAmount;

    /** 赢单金额 */
    private BigDecimal wonAmount;

    /** 活动数 */
    private Integer activityCount;

    /** 活动获取线索数 */
    private Integer activityLeadCount;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 排名 */
    private Integer rank;

    /** 赢单商机数 */
    private Integer wonCount;

    /** 输单商机数 */
    private Integer lostCount;

    /** 商机总数（赢单+输单） */
    private Integer totalDealCount;

    /** 平均成交金额 */
    private BigDecimal avgDealAmount;

    /** 转化率 */
    private BigDecimal conversionRate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getWeightedAmount() { return weightedAmount; }
    public void setWeightedAmount(BigDecimal weightedAmount) { this.weightedAmount = weightedAmount; }
    public Integer getWinRate() { return winRate; }
    public void setWinRate(Integer winRate) { this.winRate = winRate; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Integer getCustomerCount() { return customerCount; }
    public void setCustomerCount(Integer customerCount) { this.customerCount = customerCount; }
    public Integer getLeadCount() { return leadCount; }
    public void setLeadCount(Integer leadCount) { this.leadCount = leadCount; }
    public Integer getOpportunityCount() { return opportunityCount; }
    public void setOpportunityCount(Integer opportunityCount) { this.opportunityCount = opportunityCount; }
    public Integer getContractCount() { return contractCount; }
    public void setContractCount(Integer contractCount) { this.contractCount = contractCount; }
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    public BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(BigDecimal contractAmount) { this.contractAmount = contractAmount; }
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }
    public BigDecimal getWonAmount() { return wonAmount; }
    public void setWonAmount(BigDecimal wonAmount) { this.wonAmount = wonAmount; }
    public Integer getActivityCount() { return activityCount; }
    public void setActivityCount(Integer activityCount) { this.activityCount = activityCount; }
    public Integer getActivityLeadCount() { return activityLeadCount; }
    public void setActivityLeadCount(Integer activityLeadCount) { this.activityLeadCount = activityLeadCount; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Integer getWonCount() { return wonCount; }
    public void setWonCount(Integer wonCount) { this.wonCount = wonCount; }
    public Integer getLostCount() { return lostCount; }
    public void setLostCount(Integer lostCount) { this.lostCount = lostCount; }
    public Integer getTotalDealCount() { return totalDealCount; }
    public void setTotalDealCount(Integer totalDealCount) { this.totalDealCount = totalDealCount; }
    public BigDecimal getAvgDealAmount() { return avgDealAmount; }
    public void setAvgDealAmount(BigDecimal avgDealAmount) { this.avgDealAmount = avgDealAmount; }
    public BigDecimal getConversionRate() { return conversionRate; }
    public void setConversionRate(BigDecimal conversionRate) { this.conversionRate = conversionRate; }
}
