package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyIncident;

public interface ISafetyIncidentService {
    public SafetyIncident selectSafetyIncidentById(Long incidentId);
    public List<SafetyIncident> selectSafetyIncidentList(SafetyIncident safetyIncident);
    public int insertSafetyIncident(SafetyIncident safetyIncident);
    public int updateSafetyIncident(SafetyIncident safetyIncident);
    public int deleteSafetyIncidentByIds(Long[] incidentIds);
    public int deleteSafetyIncidentById(Long incidentId);
}