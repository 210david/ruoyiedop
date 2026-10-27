package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工单BOM快照 mms_wo_bom_snapshot
 *
 * 工单下达时从BOM明细复制，此后只读，保证在制工单不受BOM变更影响。
 *
 * @author ruoyi
 */
public class MmsWoBomSnapshot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long snapshotId;

    private Long workOrderId;

    private Long bomId;

    private String bomNo;

    private String bomVersion;

    private Integer seq;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String specModel;

    private String unit;

    private BigDecimal usageQty;

    private BigDecimal lossRate;

    private String isKeyMaterial;

    private String supplyType;

    private Long pickStoreId;

    private String pickStoreName;

    private String isPhantom;

    private String delFlag;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public String getBomNo() { return bomNo; }
    public void setBomNo(String bomNo) { this.bomNo = bomNo; }
    public String getBomVersion() { return bomVersion; }
    public void setBomVersion(String bomVersion) { this.bomVersion = bomVersion; }
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
    public BigDecimal getLossRate() { return lossRate; }
    public void setLossRate(BigDecimal lossRate) { this.lossRate = lossRate; }
    public String getIsKeyMaterial() { return isKeyMaterial; }
    public void setIsKeyMaterial(String isKeyMaterial) { this.isKeyMaterial = isKeyMaterial; }
    public String getSupplyType() { return supplyType; }
    public void setSupplyType(String supplyType) { this.supplyType = supplyType; }
    public Long getPickStoreId() { return pickStoreId; }
    public void setPickStoreId(Long pickStoreId) { this.pickStoreId = pickStoreId; }
    public String getPickStoreName() { return pickStoreName; }
    public void setPickStoreName(String pickStoreName) { this.pickStoreName = pickStoreName; }
    public String getIsPhantom() { return isPhantom; }
    public void setIsPhantom(String isPhantom) { this.isPhantom = isPhantom; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
