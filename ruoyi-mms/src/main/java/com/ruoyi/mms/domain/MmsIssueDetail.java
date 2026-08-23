package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 领料明细 mms_issue_detail
 *
 * @author ruoyi
 */
public class MmsIssueDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long detailId;

    /** 领料单ID */
    private Long issueId;

    @Excel(name = "序号")
    private Integer seq;

    private Long materialId;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "物料名称")
    private String materialName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "单位")
    private String unit;

    /** 单件用量（来源BOM） */
    @Excel(name = "单件用量")
    private BigDecimal usageQty;

    /** 建议数量（单件用量×工单数量） */
    @Excel(name = "建议数量")
    private BigDecimal suggestedQty;

    @Excel(name = "领料数量")
    private BigDecimal issueQty;

    @Excel(name = "批次号")
    private String batchNo;

    /** 已退数量（多次退料累计，非持久化） */
    @Excel(name = "已退数量")
    private BigDecimal returnedQty;

    /** 可退数量（领料数量-已退数量，非持久化） */
    @Excel(name = "可退数量")
    private BigDecimal availableQty;

    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getUsageQty() { return usageQty; }
    public void setUsageQty(BigDecimal usageQty) { this.usageQty = usageQty; }
    public BigDecimal getSuggestedQty() { return suggestedQty; }
    public void setSuggestedQty(BigDecimal suggestedQty) { this.suggestedQty = suggestedQty; }
    public BigDecimal getIssueQty() { return issueQty; }
    public void setIssueQty(BigDecimal issueQty) { this.issueQty = issueQty; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getReturnedQty() { return returnedQty; }
    public void setReturnedQty(BigDecimal returnedQty) { this.returnedQty = returnedQty; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public void setAvailableQty(BigDecimal availableQty) { this.availableQty = availableQty; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
