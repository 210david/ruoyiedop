package com.ruoyi.dms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 设备台账 dms_equipment
 *
 * @author ruoyi
 */
public class DmsEquipment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备ID */
    private Long equipmentId;

    /** 设备编号 */
    @Excel(name = "设备编号")
    private String equipmentCode;

    /** 设备名称 */
    @Excel(name = "设备名称")
    private String equipmentName;

    /** 设备分类ID */
    private Long categoryId;

    /** 设备分类名称（关联查询） */
    @Excel(name = "设备分类")
    private String categoryName;

    /** 型号 */
    @Excel(name = "型号")
    private String model;

    /** 序列号 */
    @Excel(name = "序列号")
    private String serialNumber;

    /** 制造商 */
    @Excel(name = "制造商")
    private String manufacturer;

    /** 供应商 */
    @Excel(name = "供应商")
    private String supplier;

    /** 购置日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "购置日期", width = 20)
    private Date purchaseDate;

    /** 原值 */
    @Excel(name = "原值")
    private BigDecimal originalValue;

    /** 使用部门ID */
    private Long deptId;

    /** 使用部门名称（关联查询） */
    @Excel(name = "使用部门")
    private String deptName;

    /** 安装位置 */
    @Excel(name = "安装位置")
    private String installLocation;

    /** 设备状态（字典 dms_equipment_status） */
    @Excel(name = "设备状态", dictType = "dms_equipment_status")
    private String equipmentStatus;

    /** 责任人ID */
    private Long responsibleId;

    /** 责任人名称（关联查询） */
    @Excel(name = "责任人")
    private String responsibleName;

    /** 设备图片URL */
    private String equipmentImage;

    /** 设备附件资料URL */
    private String attachmentUrl;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public Date getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }
    public BigDecimal getOriginalValue() { return originalValue; }
    public void setOriginalValue(BigDecimal originalValue) { this.originalValue = originalValue; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getInstallLocation() { return installLocation; }
    public void setInstallLocation(String installLocation) { this.installLocation = installLocation; }
    public String getEquipmentStatus() { return equipmentStatus; }
    public void setEquipmentStatus(String equipmentStatus) { this.equipmentStatus = equipmentStatus; }
    public Long getResponsibleId() { return responsibleId; }
    public void setResponsibleId(Long responsibleId) { this.responsibleId = responsibleId; }
    public String getResponsibleName() { return responsibleName; }
    public void setResponsibleName(String responsibleName) { this.responsibleName = responsibleName; }
    public String getEquipmentImage() { return equipmentImage; }
    public void setEquipmentImage(String equipmentImage) { this.equipmentImage = equipmentImage; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
