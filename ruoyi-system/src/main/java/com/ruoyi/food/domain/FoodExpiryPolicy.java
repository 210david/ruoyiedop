package com.ruoyi.food.domain;

import jakarta.validation.constraints.NotNull;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品效期策略对象 food_expiry_policy
 *
 * 三级匹配顺序：物料 → 物料类别 → 默认（material_id=0 且 material_type=''）
 *
 * @author ruoyi
 */
public class FoodExpiryPolicy extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 策略ID */
    private Long policyId;

    /** 策略名称 */
    private String policyName;

    /** 适用物料ID（0=不限） */
    private Long materialId;

    /** 适用物料类别（空=不限） */
    private String materialType;

    /** 临期预警天数 */
    private Integer warnDays;

    /** 过期冻结天数（0=过期即冻结） */
    private Integer freezeDays;

    /** 最小可发运剩余天数 */
    private Integer minShipDays;

    /** 是否启用FEFO（0否 1是） */
    private String fefoEnabled;

    /** 状态（0启用 1停用） */
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 物料名称（关联查询，非表字段） */
    private String materialName;

    public Long getPolicyId()
    {
        return policyId;
    }

    public void setPolicyId(Long policyId)
    {
        this.policyId = policyId;
    }

    public String getPolicyName()
    {
        return policyName;
    }

    public void setPolicyName(String policyName)
    {
        this.policyName = policyName;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialType()
    {
        return materialType;
    }

    public void setMaterialType(String materialType)
    {
        this.materialType = materialType;
    }

    @NotNull(message = "临期预警天数不能为空")
    public Integer getWarnDays()
    {
        return warnDays;
    }

    public void setWarnDays(Integer warnDays)
    {
        this.warnDays = warnDays;
    }

    public Integer getFreezeDays()
    {
        return freezeDays;
    }

    public void setFreezeDays(Integer freezeDays)
    {
        this.freezeDays = freezeDays;
    }

    public Integer getMinShipDays()
    {
        return minShipDays;
    }

    public void setMinShipDays(Integer minShipDays)
    {
        this.minShipDays = minShipDays;
    }

    public String getFefoEnabled()
    {
        return fefoEnabled;
    }

    public void setFefoEnabled(String fefoEnabled)
    {
        this.fefoEnabled = fefoEnabled;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }
}
