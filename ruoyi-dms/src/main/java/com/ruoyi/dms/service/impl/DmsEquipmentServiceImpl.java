package com.ruoyi.dms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
import com.ruoyi.dms.service.IDmsEquipmentService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class DmsEquipmentServiceImpl implements IDmsEquipmentService
{
    @Autowired
    private DmsEquipmentMapper dmsEquipmentMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<DmsEquipment> selectEquipmentList(DmsEquipment equipment)
    {
        return dmsEquipmentMapper.selectEquipmentList(equipment);
    }

    @Override
    public DmsEquipment selectEquipmentById(Long equipmentId)
    {
        return dmsEquipmentMapper.selectEquipmentById(equipmentId);
    }

    @Override
    public int insertEquipment(DmsEquipment equipment)
    {
        equipment.setDelFlag("0");
        if (StringUtils.isEmpty(equipment.getEquipmentCode()))
        {
            equipment.setEquipmentCode(mkNumberRuleService.generateNumber("dms_equipment"));
        }
        return dmsEquipmentMapper.insertEquipment(equipment);
    }

    @Override
    public int updateEquipment(DmsEquipment equipment)
    {
        return dmsEquipmentMapper.updateEquipment(equipment);
    }

    @Override
    public int deleteEquipmentByIds(Long[] equipmentIds)
    {
        return dmsEquipmentMapper.deleteEquipmentByIds(equipmentIds);
    }
}
