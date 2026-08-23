package com.ruoyi.mms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流转卡 mms_flow_card
 *
 * 流转卡状态：
 * 0(在制) → 1(完工) / 2(报废)
 *
 * @author ruoyi
 */
public class MmsFlowCard extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long cardId;

    @Excel(name = "流转卡号")
    private String cardCode;

    private Long workOrderId;

    @Excel(name = "工单编号")
    private String workOrderNo;

    @Excel(name = "批次号")
    private String batchNo;

    @Excel(name = "当前工序顺序号")
    private Integer opSeq;

    @Excel(name = "当前工序")
    private String processName;

    @Excel(name = "状态", readConverterExp = "0=在制,1=完工,2=报废")
    private String status;

    private String qrContent;

    @Excel(name = "打印次数")
    private Integer printCount;

    private String delFlag;

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }
    public String getCardCode() { return cardCode; }
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Integer getOpSeq() { return opSeq; }
    public void setOpSeq(Integer opSeq) { this.opSeq = opSeq; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getQrContent() { return qrContent; }
    public void setQrContent(String qrContent) { this.qrContent = qrContent; }
    public Integer getPrintCount() { return printCount; }
    public void setPrintCount(Integer printCount) { this.printCount = printCount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
