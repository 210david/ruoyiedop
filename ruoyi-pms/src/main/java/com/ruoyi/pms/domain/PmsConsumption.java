package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 零散采购耗用登记 pms_consumption
 *
 * @author ruoyi
 */
public class PmsConsumption extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 耗用ID */
    private Long consumptionId;

    /** 耗用单号 */
    @Excel(name = "耗用单号")
    private String consumptionNo;

    /** 采购订单ID */
    private Long orderId;

    /** 采购订单号 */
    @Excel(name = "采购订单号")
    private String orderNo;

    /** 收货单ID */
    private Long receiveId;

    /** 收货单号 */
    @Excel(name = "收货单号")
    private String receiveNo;

    /** 供应商 */
    @Excel(name = "供应商")
    private String supplierName;

    /** 耗用总数量 */
    @Excel(name = "耗用总数量")
    private BigDecimal totalQty;

    /** 耗用总金额 */
    @Excel(name = "耗用总金额")
    private BigDecimal totalAmount;

    /** 使用部门ID */
    private Long deptId;

    /** 使用部门 */
    @Excel(name = "使用部门")
    private String deptName;

    /** 领用人ID */
    private Long userId;

    /** 领用人 */
    @Excel(name = "领用人")
    private String userName;

    /** 使用日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "使用日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date useDate;

    /** 用途说明 */
    @Excel(name = "用途说明")
    private String usagePurpose;

    /** 状态（0待登记 1已登记） */
    @Excel(name = "状态", readConverterExp = "0=待登记,1=已登记")
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 耗用明细列表 */
    private List<PmsConsumptionDetail> detailList;

    public Long getConsumptionId() { return consumptionId; }
    public void setConsumptionId(Long consumptionId) { this.consumptionId = consumptionId; }
    public String getConsumptionNo() { return consumptionNo; }
    public void setConsumptionNo(String consumptionNo) { this.consumptionNo = consumptionNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getReceiveId() { return receiveId; }
    public void setReceiveId(Long receiveId) { this.receiveId = receiveId; }
    public String getReceiveNo() { return receiveNo; }
    public void setReceiveNo(String receiveNo) { this.receiveNo = receiveNo; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Date getUseDate() { return useDate; }
    public void setUseDate(Date useDate) { this.useDate = useDate; }
    public String getUsagePurpose() { return usagePurpose; }
    public void setUsagePurpose(String usagePurpose) { this.usagePurpose = usagePurpose; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PmsConsumptionDetail> getDetailList() { return detailList; }
    public void setDetailList(List<PmsConsumptionDetail> detailList) { this.detailList = detailList; }
}
