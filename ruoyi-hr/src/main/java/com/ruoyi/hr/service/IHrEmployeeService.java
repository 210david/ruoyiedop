package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrEmployee;

public interface IHrEmployeeService {
    public HrEmployee selectHrEmployeeById(Long employeeId);
    public List<HrEmployee> selectHrEmployeeList(HrEmployee hrEmployee);
    public int insertHrEmployee(HrEmployee hrEmployee);
    public int updateHrEmployee(HrEmployee hrEmployee);
    public int deleteHrEmployeeByIds(Long[] employeeIds);
    public int deleteHrEmployeeById(Long employeeId);
}
