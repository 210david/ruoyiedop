package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 招投标档案 mk_bid_archive
 *
 * @author ruoyi
 */
public class MkBidArchive extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 投标ID */
    private Long bidId;

    /** 投标编号 */
    @Excel(name = "投标编号")
    private String bidNo;

    /** 投标项目名称 */
    @Excel(name = "投标项目名称")
    private String bidName;

    /** 招标编号 */
    @Excel(name = "招标编号")
    private String tenderNo;

    /** 招标机构 */
    @Excel(name = "招标机构")
    private String tenderOrg;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 投标日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "投标日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date bidDate;

    /** 开标日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开标日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date openDate;

    /** 投标金额 */
    @Excel(name = "投标金额")
    private BigDecimal bidAmount;

    /** 保证金 */
    @Excel(name = "保证金")
    private BigDecimal deposit;

    /** 状态（0编制 1已投标 2中标 3未中标 4废标） */
    @Excel(name = "状态", dictType = "mk_bid_status")
    private String bidStatus;

    /** 中标金额 */
    @Excel(name = "中标金额")
    private BigDecimal winAmount;

    /** 关联合同ID */
    private Long contractId;

    /** 关联合同编号 */
    @Excel(name = "关联合同")
    private String contractNo;

    /** 结果日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结果日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date resultDate;

    /** 投标附件 */
    private String attachment;

    /** 投标明细 */
    private List<MkBidItem> itemList;

    public Long getBidId() { return bidId; }
    public void setBidId(Long bidId) { this.bidId = bidId; }
    public String getBidNo() { return bidNo; }
    public void setBidNo(String bidNo) { this.bidNo = bidNo; }
    public String getBidName() { return bidName; }
    public void setBidName(String bidName) { this.bidName = bidName; }
    public String getTenderNo() { return tenderNo; }
    public void setTenderNo(String tenderNo) { this.tenderNo = tenderNo; }
    public String getTenderOrg() { return tenderOrg; }
    public void setTenderOrg(String tenderOrg) { this.tenderOrg = tenderOrg; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Date getBidDate() { return bidDate; }
    public void setBidDate(Date bidDate) { this.bidDate = bidDate; }
    public Date getOpenDate() { return openDate; }
    public void setOpenDate(Date openDate) { this.openDate = openDate; }
    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
    public String getBidStatus() { return bidStatus; }
    public void setBidStatus(String bidStatus) { this.bidStatus = bidStatus; }
    public BigDecimal getWinAmount() { return winAmount; }
    public void setWinAmount(BigDecimal winAmount) { this.winAmount = winAmount; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public Date getResultDate() { return resultDate; }
    public void setResultDate(Date resultDate) { this.resultDate = resultDate; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public List<MkBidItem> getItemList() { return itemList; }
    public void setItemList(List<MkBidItem> itemList) { this.itemList = itemList; }

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
