package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 仓库结构表 wms_warehouse（仓库/仓区/仓位 树形结构）
 *
 * @author ruoyi
 */
public class WmsWarehouse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long warehouseId;

    /** 父级ID */
    private Long parentId;

    /** 祖级列表 */
    private String ancestors;

    /** 编码（自动生成） */
    @Excel(name = "编码")
    private String warehouseCode;

    /** 名称 */
    @Excel(name = "名称")
    private String warehouseName;

    /** 节点类型（1=仓库 2=仓区 3=仓位） */
    @Excel(name = "节点类型", readConverterExp = "1=仓库,2=仓区,3=仓位")
    private String nodeType;

    /** 管理部门ID（仓库级） */
    private Long deptId;

    /** 管理部门名称 */
    @Excel(name = "管理部门")
    private String deptName;

    /** 负责人用户ID（仓库级） */
    private Long managerId;

    /** 负责人名称 */
    @Excel(name = "负责人")
    private String managerName;

    /** 仓库地址（仓库级） */
    private String address;

    /** 库区类型（仓区级） */
    @Excel(name = "库区类型", readConverterExp = "0=存储区,1=收货区,2=发货区,3=退货区,4=暂存区")
    private String areaType;

    /** 库位类型（仓位级） */
    @Excel(name = "库位类型", readConverterExp = "0=货架位,1=地堆位,2=暂存位")
    private String locationType;

    /** 库位容量（仓位级） */
    @Excel(name = "库位容量")
    private BigDecimal capacity;

    /** 库位状态（仓位级，0空闲 1占用 2锁定） */
    @Excel(name = "库位状态", readConverterExp = "0=空闲,1=占用,2=锁定")
    private String locationStatus;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getAncestors() { return ancestors; }
    public void setAncestors(String ancestors) { this.ancestors = ancestors; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }
    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }
    public BigDecimal getCapacity() { return capacity; }
    public void setCapacity(BigDecimal capacity) { this.capacity = capacity; }
    public String getLocationStatus() { return locationStatus; }
    public void setLocationStatus(String locationStatus) { this.locationStatus = locationStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
