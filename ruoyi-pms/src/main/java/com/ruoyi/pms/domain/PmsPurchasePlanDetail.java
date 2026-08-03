package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购计划明细表 pms_purchase_plan_detail
 *
 * @author ruoyi
 */
public class PmsPurchasePlanDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 计划ID */
    private Long planId;

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

    /** 计划数量 */
    @Excel(name = "计划数量")
    private BigDecimal planQuantity;

    /** 预算单价 */
    @Excel(name = "预算单价")
    private BigDecimal budgetPrice;

    /** 预算金额 */
    @Excel(name = "预算金额")
    private BigDecimal budgetAmount;

    /** 实际采购数量 */
    @Excel(name = "实际采购数量")
    private BigDecimal actualQuantity;

    /** 实际采购金额 */
    @Excel(name = "实际采购金额")
    private BigDecimal actualAmount;

    /** 需求日期 */
    private String requireDate;

    /** 备注 */
    private String remark;

    /** 删除标志 */
    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

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

    public BigDecimal getPlanQuantity() { return planQuantity; }
    public void setPlanQuantity(BigDecimal planQuantity) { this.planQuantity = planQuantity; }

    public BigDecimal getBudgetPrice() { return budgetPrice; }
    public void setBudgetPrice(BigDecimal budgetPrice) { this.budgetPrice = budgetPrice; }

    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public BigDecimal getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(BigDecimal actualQuantity) { this.actualQuantity = actualQuantity; }

    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }

    public String getRequireDate() { return requireDate; }
    public void setRequireDate(String requireDate) { this.requireDate = requireDate; }

    @Override
    public String getRemark() { return remark; }
    @Override
    public void setRemark(String remark) { this.remark = remark; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return "PmsPurchasePlanDetail{" +
                "detailId=" + detailId +
                ", planId=" + planId +
                ", materialId=" + materialId +
                ", materialCode='" + materialCode + '\'' +
                ", materialName='" + materialName + '\'' +
                ", specification='" + specification + '\'' +
                ", unit='" + unit + '\'' +
                ", planQuantity=" + planQuantity +
                ", budgetPrice=" + budgetPrice +
                ", budgetAmount=" + budgetAmount +
                ", actualQuantity=" + actualQuantity +
                ", actualAmount=" + actualAmount +
                ", requireDate='" + requireDate + '\'' +
                ", remark='" + remark + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
