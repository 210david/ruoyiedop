package com.ruoyi.safety.service.impl;
import java.util.HashMap;
import java.util.Map;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyEquipment;
import com.ruoyi.safety.mapper.SafetyEquipmentMapper;
import com.ruoyi.safety.service.ISafetyEquipmentService;

@Service
public class SafetyEquipmentServiceImpl implements ISafetyEquipmentService
{
    @Autowired
    private SafetyEquipmentMapper safetyEquipmentMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyEquipment selectSafetyEquipmentById(Long equipmentId) { return safetyEquipmentMapper.selectSafetyEquipmentById(equipmentId); }

    @Override
    public List<SafetyEquipment> selectSafetyEquipmentList(SafetyEquipment safetyEquipment) { return safetyEquipmentMapper.selectSafetyEquipmentList(safetyEquipment); }

    @Override
    public int insertSafetyEquipment(SafetyEquipment safetyEquipment) {
        if (StringUtils.isEmpty(safetyEquipment.getEquipmentCode())) {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(safetyEquipment.getEquipmentCategory())) { params.put("equipmentCategory", safetyEquipment.getEquipmentCategory()); }
        safetyEquipment.setEquipmentCode(mkNumberRuleService.generateNumber("safety_equipment", params));
        }
        return safetyEquipmentMapper.insertSafetyEquipment(safetyEquipment); }

    @Override
    public int updateSafetyEquipment(SafetyEquipment safetyEquipment) { return safetyEquipmentMapper.updateSafetyEquipment(safetyEquipment); }

    @Override
    public int deleteSafetyEquipmentByIds(Long[] equipmentIds) { return safetyEquipmentMapper.deleteSafetyEquipmentByIds(equipmentIds); }

    @Override
    public int deleteSafetyEquipmentById(Long equipmentId) { return safetyEquipmentMapper.deleteSafetyEquipmentById(equipmentId); }
}