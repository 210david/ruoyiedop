package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrEmployeeFamily;

public interface HrEmployeeFamilyMapper {
    public HrEmployeeFamily selectHrEmployeeFamilyByFamilyId(Long familyId);
    public List<HrEmployeeFamily> selectHrEmployeeFamilyList(HrEmployeeFamily hrEmployeeFamily);
    public int insertHrEmployeeFamily(HrEmployeeFamily hrEmployeeFamily);
    public int updateHrEmployeeFamily(HrEmployeeFamily hrEmployeeFamily);
    public int deleteHrEmployeeFamilyByFamilyId(Long familyId);
    public int deleteHrEmployeeFamilyByIds(Long[] familyIds);
public int deleteHrEmployeeFamilyByEmployeeId(Long employeeId);
}
