package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;
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
import com.ruoyi.mk.service.IMkNumberRuleService;

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
    @Autowired
    private ISysUserService userService;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

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
    @Transactional(rollbackFor = Exception.class)
    public int insertHrEmployee(HrEmployee hrEmployee)
    {
        if (StringUtils.isEmpty(hrEmployee.getEmployeeNo()))
        {
            hrEmployee.setEmployeeNo(mkNumberRuleService.generateNumber("hr_employee"));
        }
        syncSysUser(hrEmployee);
        int rows = hrEmployeeMapper.insertHrEmployee(hrEmployee);
        insertChildLists(hrEmployee);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateHrEmployee(HrEmployee hrEmployee)
    {
        // 明细子表采用"先删后插"的全量替换策略
        hrEmployeeFamilyMapper.deleteHrEmployeeFamilyByEmployeeId(hrEmployee.getEmployeeId());
        hrEmployeeEducationMapper.deleteHrEmployeeEducationByEmployeeId(hrEmployee.getEmployeeId());
        hrEmployeeWorkExpMapper.deleteHrEmployeeWorkExpByEmployeeId(hrEmployee.getEmployeeId());
        hrEmployeeQualificationMapper.deleteHrEmployeeQualificationByEmployeeId(hrEmployee.getEmployeeId());
        syncSysUser(hrEmployee);
        int rows = hrEmployeeMapper.updateHrEmployee(hrEmployee);
        insertChildLists(hrEmployee);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteHrEmployeeByIds(Long[] employeeIds)
    {
        for (Long employeeId : employeeIds)
        {
            hrEmployeeFamilyMapper.deleteHrEmployeeFamilyByEmployeeId(employeeId);
            hrEmployeeEducationMapper.deleteHrEmployeeEducationByEmployeeId(employeeId);
            hrEmployeeWorkExpMapper.deleteHrEmployeeWorkExpByEmployeeId(employeeId);
            hrEmployeeQualificationMapper.deleteHrEmployeeQualificationByEmployeeId(employeeId);
        }
        return hrEmployeeMapper.deleteHrEmployeeByIds(employeeIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteHrEmployeeById(Long employeeId)
    {
        hrEmployeeFamilyMapper.deleteHrEmployeeFamilyByEmployeeId(employeeId);
        hrEmployeeEducationMapper.deleteHrEmployeeEducationByEmployeeId(employeeId);
        hrEmployeeWorkExpMapper.deleteHrEmployeeWorkExpByEmployeeId(employeeId);
        hrEmployeeQualificationMapper.deleteHrEmployeeQualificationByEmployeeId(employeeId);
        return hrEmployeeMapper.deleteHrEmployeeById(employeeId);
    }

    /**
     * 保存明细子表（家庭成员/教育背景/工作经历/培训职称）
     */
    private void insertChildLists(HrEmployee hrEmployee)
    {
        Long employeeId = hrEmployee.getEmployeeId();
        String createBy = StringUtils.isNotEmpty(hrEmployee.getCreateBy()) ? hrEmployee.getCreateBy() : hrEmployee.getUpdateBy();
        if (hrEmployee.getFamilyList() != null)
        {
            for (HrEmployeeFamily family : hrEmployee.getFamilyList())
            {
                family.setEmployeeId(employeeId);
                family.setCreateBy(createBy);
                family.setDelFlag("0");
                hrEmployeeFamilyMapper.insertHrEmployeeFamily(family);
            }
        }
        if (hrEmployee.getEducationList() != null)
        {
            for (HrEmployeeEducation edu : hrEmployee.getEducationList())
            {
                edu.setEmployeeId(employeeId);
                edu.setCreateBy(createBy);
                edu.setDelFlag("0");
                hrEmployeeEducationMapper.insertHrEmployeeEducation(edu);
            }
        }
        if (hrEmployee.getWorkExpList() != null)
        {
            for (HrEmployeeWorkExp work : hrEmployee.getWorkExpList())
            {
                work.setEmployeeId(employeeId);
                work.setCreateBy(createBy);
                work.setDelFlag("0");
                hrEmployeeWorkExpMapper.insertHrEmployeeWorkExp(work);
            }
        }
        if (hrEmployee.getQualificationList() != null)
        {
            for (HrEmployeeQualification qual : hrEmployee.getQualificationList())
            {
                qual.setEmployeeId(employeeId);
                qual.setCreateBy(createBy);
                qual.setDelFlag("0");
                hrEmployeeQualificationMapper.insertHrEmployeeQualification(qual);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importHrEmployee(List<HrEmployee> employeeList, Boolean isUpdateSupport, String updateKey, String operName)
    {
        if (StringUtils.isNull(employeeList) || employeeList.size() == 0)
        {
            return AjaxResult.error("导入员工档案数据不能为空！");
        }
        // 默认按工号匹配
        if (StringUtils.isEmpty(updateKey))
        {
            updateKey = "employeeNo";
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (HrEmployee row : employeeList)
        {
            try
            {
                // 1. 基础校验
                if (StringUtils.isEmpty(row.getEmployeeNo()))
                {
                    throw new RuntimeException("工号不能为空");
                }
                if (StringUtils.isEmpty(row.getEmployeeName()))
                {
                    throw new RuntimeException("姓名不能为空");
                }
                // 2. 根据匹配字段查找已存在的员工
                HrEmployee matched;
                if ("idCard".equals(updateKey))
                {
                    if (StringUtils.isEmpty(row.getIdCard()))
                    {
                        throw new RuntimeException("匹配字段「身份证号」不能为空");
                    }
                    matched = hrEmployeeMapper.selectHrEmployeeByIdCard(row.getIdCard());
                }
                else
                {
                    matched = hrEmployeeMapper.selectHrEmployeeByEmployeeNo(row.getEmployeeNo());
                }

                if (matched != null)
                {
                    if (isUpdateSupport)
                    {
                        row.setEmployeeId(matched.getEmployeeId());
                        row.setUpdateBy(operName);
                        hrEmployeeMapper.updateHrEmployee(row);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、员工 " + row.getEmployeeName() + "（工号: " + row.getEmployeeNo() + "）更新成功");
                    }
                    else
                    {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "、员工 " + row.getEmployeeName() + " 已存在（工号: " + row.getEmployeeNo() + "）");
                    }
                }
                else
                {
                    row.setDelFlag("0");
                    if (StringUtils.isEmpty(row.getEmployeeStatus()))
                    {
                        row.setEmployeeStatus("在职");
                    }
                    row.setCreateBy(operName);
                    hrEmployeeMapper.insertHrEmployee(row);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、员工 " + row.getEmployeeName() + "（工号: " + row.getEmployeeNo() + "）导入成功");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String name = row.getEmployeeName() != null ? row.getEmployeeName() : "未知员工";
                failureMsg.append("<br/>" + failureNum + "、员工 " + name + " 导入失败：" + e.getMessage());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new RuntimeException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return AjaxResult.success(successMsg.toString());
    }
    /**
     * 新增/编辑员工时同步创建系统用户（填写了登录账号且尚未关联用户时）
     */
    private void syncSysUser(HrEmployee hrEmployee)
    {
        if (StringUtils.isEmpty(hrEmployee.getUserName()) || hrEmployee.getUserId() != null)
        {
            return;
        }
        if (StringUtils.isEmpty(hrEmployee.getPassword()))
        {
            throw new ServiceException("填写了登录账号，请设置初始密码");
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserName(hrEmployee.getUserName());
        sysUser.setNickName(hrEmployee.getEmployeeName());
        sysUser.setPhonenumber(hrEmployee.getPhone());
        sysUser.setEmail(hrEmployee.getEmail());
        sysUser.setPassword(SecurityUtils.encryptPassword(hrEmployee.getPassword()));
        sysUser.setStatus("0");
        sysUser.setRoleIds(hrEmployee.getRoleIds());
        sysUser.setCreateBy(hrEmployee.getCreateBy());
        sysUser.setRemark("员工档案创建");
        if (!userService.checkUserNameUnique(sysUser))
        {
            throw new ServiceException("登录账号「" + hrEmployee.getUserName() + "」已存在");
        }
        userService.insertUser(sysUser);
        hrEmployee.setUserId(sysUser.getUserId());
    }}
