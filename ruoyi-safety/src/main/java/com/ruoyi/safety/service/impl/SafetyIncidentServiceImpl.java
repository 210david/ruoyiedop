package com.ruoyi.safety.service.impl;
import java.util.HashMap;
import java.util.Map;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyIncident;
import com.ruoyi.safety.mapper.SafetyIncidentMapper;
import com.ruoyi.safety.service.ISafetyIncidentService;

@Service
public class SafetyIncidentServiceImpl implements ISafetyIncidentService
{
    @Autowired
    private SafetyIncidentMapper safetyIncidentMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyIncident selectSafetyIncidentById(Long incidentId) { return safetyIncidentMapper.selectSafetyIncidentById(incidentId); }

    @Override
    public List<SafetyIncident> selectSafetyIncidentList(SafetyIncident safetyIncident) { return safetyIncidentMapper.selectSafetyIncidentList(safetyIncident); }

    @Override
    public int insertSafetyIncident(SafetyIncident safetyIncident) {
        if (StringUtils.isEmpty(safetyIncident.getIncidentCode())) {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(safetyIncident.getIncidentLevel())) { params.put("incidentLevel", safetyIncident.getIncidentLevel()); }
        safetyIncident.setIncidentCode(mkNumberRuleService.generateNumber("safety_incident", params));
        }
        return safetyIncidentMapper.insertSafetyIncident(safetyIncident); }

    @Override
    public int updateSafetyIncident(SafetyIncident safetyIncident) { return safetyIncidentMapper.updateSafetyIncident(safetyIncident); }

    @Override
    public int deleteSafetyIncidentByIds(Long[] incidentIds) { return safetyIncidentMapper.deleteSafetyIncidentByIds(incidentIds); }

    @Override
    public int deleteSafetyIncidentById(Long incidentId) { return safetyIncidentMapper.deleteSafetyIncidentById(incidentId); }
}