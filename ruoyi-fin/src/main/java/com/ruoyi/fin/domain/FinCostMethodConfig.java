package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 成本方法配置 fin_cost_method_config
 *
 * <p><b>计价方法优先级</b>：物料级配置 &gt; 全局默认（material_id 为空）。
 * 引擎取值时先查物料级，查不到用全局。</p>
 *
 * <p><b>为什么需要独立表</b>：不同物料可能采用不同计价方法
 * （如辅料用先进先出、原料用加权平均），也便于未来税率变化时
 * 对不同物料设置不同的含税口径。</p>
 *
 * @author ruoyi
 */
public class FinCostMethodConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 计价方法：加权平均 */
    public static final String WEIGHTED_AVG = "WEIGHTED_AVG";

    /** 计价方法：先进先出 */
    public static final String FIFO = "FIFO";

    /** 配置ID */
    private Long configId;

    /** 物料ID（空=全局默认） */
    private Long materialId;

    /** 物料编码（冗余，查询用） */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称（冗余） */
    @Excel(name = "物料名称")
    private String materialName;

    /** 计价方法 WEIGHTED_AVG/FIFO */
    @Excel(name = "计价方法", readConverterExp = "WEIGHTED_AVG=加权平均,FIFO=先进先出")
    private String costMethod;

    /** 入库单价是否含税 Y/N */
    private String taxIncluded;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", dateFormat = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 预留账套ID */
    private Long companyId;

    /** 状态 0启用 1停用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getCostMethod() { return costMethod; }
    public void setCostMethod(String costMethod) { this.costMethod = costMethod; }

    public String getTaxIncluded() { return taxIncluded; }
    public void setTaxIncluded(String taxIncluded) { this.taxIncluded = taxIncluded; }

    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * 该配置是否为全局默认（未指定物料）。
     *
     * @return true=全局默认
     */
    public boolean isGlobal()
    {
        return materialId == null;
    }
}