package com.ruoyi.dms.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.dms.domain.DmsEquipment;

public interface IDmsEquipmentService
{
    public List<DmsEquipment> selectEquipmentList(DmsEquipment equipment);
    public DmsEquipment selectEquipmentById(Long equipmentId);
    public int insertEquipment(DmsEquipment equipment);
    public int updateEquipment(DmsEquipment equipment);
    public int deleteEquipmentByIds(Long[] equipmentIds);

    /**
     * 导入设备台账数据
     *
     * @param equipmentList 设备数据列表
     * @param isUpdateSupport 是否更新已存在的设备
     * @param updateKey 更新匹配字段（equipmentName=名称, equipmentCode=设备编号, assetCode=资产编号, serialNumber=序列号）
     * @param operName 操作人
     * @return 导入结果（含成功/失败明细）
     */
    public AjaxResult importEquipment(List<DmsEquipment> equipmentList, Boolean isUpdateSupport, String updateKey, String operName);
}
