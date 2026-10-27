package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrEmployeeEducation;

public interface IHrEmployeeEducationService {
    public HrEmployeeEducation selectHrEmployeeEducationByEducationId(Long educationId);
    public List<HrEmployeeEducation> selectHrEmployeeEducationList(HrEmployeeEducation hrEmployeeEducation);
    public int insertHrEmployeeEducation(HrEmployeeEducation hrEmployeeEducation);
    public int updateHrEmployeeEducation(HrEmployeeEducation hrEmployeeEducation);
    public int deleteHrEmployeeEducationByEducationId(Long educationId);
    public int deleteHrEmployeeEducationByIds(Long[] educationIds);
}
