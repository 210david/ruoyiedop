package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrEmployeeQualification;

public interface HrEmployeeQualificationMapper {
    public HrEmployeeQualification selectHrEmployeeQualificationByQualificationId(Long qualificationId);
    public List<HrEmployeeQualification> selectHrEmployeeQualificationList(HrEmployeeQualification hrEmployeeQualification);
    public int insertHrEmployeeQualification(HrEmployeeQualification hrEmployeeQualification);
    public int updateHrEmployeeQualification(HrEmployeeQualification hrEmployeeQualification);
    public int deleteHrEmployeeQualificationByQualificationId(Long qualificationId);
    public int deleteHrEmployeeQualificationByIds(Long[] qualificationIds);
public int deleteHrEmployeeQualificationByEmployeeId(Long employeeId);
}
