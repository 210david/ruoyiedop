package com.ruoyi.dms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/** 点检路线 dms_inspection_route */
public class DmsInspectionRoute extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long routeId;
    @Excel(name = "路线名称") private String routeName;
    @Excel(name = "路线编码") private String routeCode;
    private Long deptId;
    @Excel(name = "部门") private String deptName;
    @Excel(name = "周期类型", readConverterExp = "0=日检,1=周检,2=月检") private String cycleType;
    @Excel(name = "周期值") private Integer cycleValue;
    private String equipmentIds;
    private String inspectionItems;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用") private String status;
    private String delFlag;

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getRouteCode() { return routeCode; }
    public void setRouteCode(String routeCode) { this.routeCode = routeCode; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getCycleType() { return cycleType; }
    public void setCycleType(String cycleType) { this.cycleType = cycleType; }
    public Integer getCycleValue() { return cycleValue; }
    public void setCycleValue(Integer cycleValue) { this.cycleValue = cycleValue; }
    public String getEquipmentIds() { return equipmentIds; }
    public void setEquipmentIds(String equipmentIds) { this.equipmentIds = equipmentIds; }
    public String getInspectionItems() { return inspectionItems; }
    public void setInspectionItems(String inspectionItems) { this.inspectionItems = inspectionItems; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
