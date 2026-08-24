package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 退料明细 mms_return_detail
 *
 * @author ruoyi
 */
public class MmsReturnDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long detailId;

    /** 退料单ID */
    private Long returnId;

    @Excel(name = "序号")
    private Integer seq;

    private Long materialId;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "物料名称")
    private String materialName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "单位", readConverterExp = "0=个,1=件,2=箱,3=kg,4=吨,5=米,6=把,7=千米,8=厘米,9=套,10=台,11=条")
    private String unit;

    @Excel(name = "退料数量")
    private BigDecimal returnQty;

    /** 领料数量（来源领料明细，只读参考） */
    @Excel(name = "领料数量")
    private BigDecimal issueQty;

    /** 已退数量（多次退料累计，非持久化） */
    @Excel(name = "已退数量")
    private BigDecimal returnedQty;

    /** 可退数量（领料数量-已退数量，非持久化） */
    @Excel(name = "可退数量")
    private BigDecimal availableQty;

    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }
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
    public BigDecimal getReturnQty() { return returnQty; }
    public void setReturnQty(BigDecimal returnQty) { this.returnQty = returnQty; }
    public BigDecimal getIssueQty() { return issueQty; }
    public void setIssueQty(BigDecimal issueQty) { this.issueQty = issueQty; }
    public BigDecimal getReturnedQty() { return returnedQty; }
    public void setReturnedQty(BigDecimal returnedQty) { this.returnedQty = returnedQty; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public void setAvailableQty(BigDecimal availableQty) { this.availableQty = availableQty; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
