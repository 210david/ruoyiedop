package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrEmployeeQualification;

public interface IHrEmployeeQualificationService {
    public HrEmployeeQualification selectHrEmployeeQualificationByQualificationId(Long qualificationId);
    public List<HrEmployeeQualification> selectHrEmployeeQualificationList(HrEmployeeQualification hrEmployeeQualification);
    public int insertHrEmployeeQualification(HrEmployeeQualification hrEmployeeQualification);
    public int updateHrEmployeeQualification(HrEmployeeQualification hrEmployeeQualification);
    public int deleteHrEmployeeQualificationByQualificationId(Long qualificationId);
    public int deleteHrEmployeeQualificationByIds(Long[] qualificationIds);
}
