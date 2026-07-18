package com.ruoyi.dms.service;

import java.util.List;
import com.ruoyi.dms.domain.DmsEquipment;

public interface IDmsEquipmentService
{
    public List<DmsEquipment> selectEquipmentList(DmsEquipment equipment);
    public DmsEquipment selectEquipmentById(Long equipmentId);
    public int insertEquipment(DmsEquipment equipment);
    public int updateEquipment(DmsEquipment equipment);
    public int deleteEquipmentByIds(Long[] equipmentIds);
}
