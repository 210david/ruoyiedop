package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.domain.HrEmployeeFamily;
import com.ruoyi.hr.domain.HrEmployeeEducation;
import com.ruoyi.hr.domain.HrEmployeeWorkExp;
import com.ruoyi.hr.domain.HrEmployeeQualification;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.mapper.HrEmployeeFamilyMapper;
import com.ruoyi.hr.mapper.HrEmployeeEducationMapper;
import com.ruoyi.hr.mapper.HrEmployeeWorkExpMapper;
import com.ruoyi.hr.mapper.HrEmployeeQualificationMapper;
import com.ruoyi.hr.service.IHrEmployeeService;

@Service
public class HrEmployeeServiceImpl implements IHrEmployeeService
{
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;
    @Autowired
    private HrEmployeeFamilyMapper hrEmployeeFamilyMapper;
    @Autowired
    private HrEmployeeEducationMapper hrEmployeeEducationMapper;
    @Autowired
    private HrEmployeeWorkExpMapper hrEmployeeWorkExpMapper;
    @Autowired
    private HrEmployeeQualificationMapper hrEmployeeQualificationMapper;

    @Override
    public HrEmployee selectHrEmployeeById(Long employeeId)
    {
        HrEmployee employee = hrEmployeeMapper.selectHrEmployeeById(employeeId);
        if (employee != null)
        {
            // 加载家庭成员
            HrEmployeeFamily familyQuery = new HrEmployeeFamily();
            familyQuery.setEmployeeId(employeeId);
            employee.setFamilyList(hrEmployeeFamilyMapper.selectHrEmployeeFamilyList(familyQuery));

            // 加载教育背景
            HrEmployeeEducation eduQuery = new HrEmployeeEducation();
            eduQuery.setEmployeeId(employeeId);
            employee.setEducationList(hrEmployeeEducationMapper.selectHrEmployeeEducationList(eduQuery));

            // 加载工作经历
            HrEmployeeWorkExp workQuery = new HrEmployeeWorkExp();
            workQuery.setEmployeeId(employeeId);
            employee.setWorkExpList(hrEmployeeWorkExpMapper.selectHrEmployeeWorkExpList(workQuery));

            // 加载培训/职称/资格认证
            HrEmployeeQualification qualQuery = new HrEmployeeQualification();
            qualQuery.setEmployeeId(employeeId);
            employee.setQualificationList(hrEmployeeQualificationMapper.selectHrEmployeeQualificationList(qualQuery));
        }
        return employee;
    }

    @Override
    public List<HrEmployee> selectHrEmployeeList(HrEmployee hrEmployee) { return hrEmployeeMapper.selectHrEmployeeList(hrEmployee); }

    @Override
    public int insertHrEmployee(HrEmployee hrEmployee) { return hrEmployeeMapper.insertHrEmployee(hrEmployee); }

    @Override
    public int updateHrEmployee(HrEmployee hrEmployee) { return hrEmployeeMapper.updateHrEmployee(hrEmployee); }

    @Override
    public int deleteHrEmployeeByIds(Long[] employeeIds) { return hrEmployeeMapper.deleteHrEmployeeByIds(employeeIds); }

    @Override
    public int deleteHrEmployeeById(Long employeeId) { return hrEmployeeMapper.deleteHrEmployeeById(employeeId); }
}
