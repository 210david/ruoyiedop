package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrEmployee;

public interface HrEmployeeMapper {
    public HrEmployee selectHrEmployeeById(Long employeeId);
    public List<HrEmployee> selectHrEmployeeList(HrEmployee hrEmployee);
    public int insertHrEmployee(HrEmployee hrEmployee);
    public int updateHrEmployee(HrEmployee hrEmployee);
    public int deleteHrEmployeeByIds(Long[] employeeIds);
    public int deleteHrEmployeeById(Long employeeId);
}
