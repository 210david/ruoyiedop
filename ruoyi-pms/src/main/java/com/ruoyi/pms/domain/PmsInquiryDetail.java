package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 询价明细表 pms_inquiry_detail
 *
 * @author ruoyi
 */
public class PmsInquiryDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 询价ID */
    private Long inquiryId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specification;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 采购数量 */
    @Excel(name = "采购数量")
    private BigDecimal quantity;

    /** 最高限价 */
    @Excel(name = "最高限价")
    private BigDecimal maxPrice;

    /** 备注 */
    private String remark;

    /** 删除标志 */
    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }

    public Long getInquiryId() { return inquiryId; }
    public void setInquiryId(Long inquiryId) { this.inquiryId = inquiryId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    @Override
    public String getRemark() { return remark; }
    @Override
    public void setRemark(String remark) { this.remark = remark; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return "PmsInquiryDetail{" +
                "detailId=" + detailId +
                ", inquiryId=" + inquiryId +
                ", materialId=" + materialId +
                ", materialCode='" + materialCode + '\'' +
                ", materialName='" + materialName + '\'' +
                ", specification='" + specification + '\'' +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                ", maxPrice=" + maxPrice +
                ", remark='" + remark + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
