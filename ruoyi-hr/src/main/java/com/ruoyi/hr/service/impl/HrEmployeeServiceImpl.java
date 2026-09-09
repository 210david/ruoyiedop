package com.ruoyi.hr.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
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
                // 数字字段校验
                if (row.getSocialWorkYears() != null && row.getSocialWorkYears() < 0)
                {
                    throw new RuntimeException("社会工龄不能为负数");
                }
                if (row.getCompanyWorkYears() != null && row.getCompanyWorkYears() < 0)
                {
                    throw new RuntimeException("本公司工龄不能为负数");
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
                        successList.add(buildSuccessItem(row, "更新成功"));
                    }
                    else
                    {
                        failureNum++;
                        failureList.add(buildFailureItem(row, "员工已存在（工号: " + row.getEmployeeNo() + "），未勾选更新选项"));
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
                    successList.add(buildSuccessItem(row, "导入成功"));
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 200) { errMsg = errMsg.substring(0, 200) + "..."; }
                failureList.add(buildFailureItem(row, errMsg != null ? errMsg : "未知错误"));
            }
        }

        // 构建返回结果（结构化明细）
        AjaxResult ajax = AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0 && successNum == 0)
        {
            ajax.put("code", 500);
            ajax.put("msg", "导入失败，共 " + failureNum + " 条数据异常");
        }
        else if (failureNum > 0)
        {
            ajax.put("msg", "部分导入成功：成功 " + successNum + " 条，失败 " + failureNum + " 条");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条");
        }
        return ajax;
    }

    /**
     * 构建成功明细项
     */
    private Map<String, Object> buildSuccessItem(HrEmployee row, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("employeeNo", row.getEmployeeNo() != null ? row.getEmployeeNo() : "");
        item.put("employeeName", row.getEmployeeName() != null ? row.getEmployeeName() : "");
        item.put("gender", row.getGender() != null ? row.getGender() : "");
        item.put("idCard", row.getIdCard() != null ? row.getIdCard() : "");
        item.put("birthday", row.getBirthday() != null ? row.getBirthday().toString() : "");
        item.put("nation", row.getNation() != null ? row.getNation() : "");
        item.put("politicsStatus", row.getPoliticsStatus() != null ? row.getPoliticsStatus() : "");
        item.put("maritalStatus", row.getMaritalStatus() != null ? row.getMaritalStatus() : "");
        item.put("phone", row.getPhone() != null ? row.getPhone() : "");
        item.put("email", row.getEmail() != null ? row.getEmail() : "");
        item.put("emergencyContact", row.getEmergencyContact() != null ? row.getEmergencyContact() : "");
        item.put("emergencyPhone", row.getEmergencyPhone() != null ? row.getEmergencyPhone() : "");
        item.put("nativePlace", row.getNativePlace() != null ? row.getNativePlace() : "");
        item.put("address", row.getAddress() != null ? row.getAddress() : "");
        item.put("eduLevel", row.getEduLevel() != null ? row.getEduLevel() : "");
        item.put("graduationSchool", row.getGraduationSchool() != null ? row.getGraduationSchool() : "");
        item.put("major", row.getMajor() != null ? row.getMajor() : "");
        item.put("graduationDate", row.getGraduationDate() != null ? row.getGraduationDate().toString() : "");
        item.put("socialWorkYears", row.getSocialWorkYears() != null ? row.getSocialWorkYears().toString() : "");
        item.put("companyWorkYears", row.getCompanyWorkYears() != null ? row.getCompanyWorkYears().toString() : "");
        item.put("positionName", row.getPositionName() != null ? row.getPositionName() : "");
        item.put("skillLevel", row.getSkillLevel() != null ? row.getSkillLevel() : "");
        item.put("workType", row.getWorkType() != null ? row.getWorkType() : "");
        item.put("employeeType", row.getEmployeeType() != null ? row.getEmployeeType() : "");
        item.put("entryDate", row.getEntryDate() != null ? row.getEntryDate().toString() : "");
        item.put("regularDate", row.getRegularDate() != null ? row.getRegularDate().toString() : "");
        item.put("leaveDate", row.getLeaveDate() != null ? row.getLeaveDate().toString() : "");
        item.put("employeeStatus", row.getEmployeeStatus() != null ? row.getEmployeeStatus() : "");
        item.put("bankName", row.getBankName() != null ? row.getBankName() : "");
        item.put("bankAccount", row.getBankAccount() != null ? row.getBankAccount() : "");
        item.put("socialSecurityNo", row.getSocialSecurityNo() != null ? row.getSocialSecurityNo() : "");
        item.put("socialSecurityStatus", row.getSocialSecurityStatus() != null ? row.getSocialSecurityStatus() : "");
        item.put("houseFundNo", row.getHouseFundNo() != null ? row.getHouseFundNo() : "");
        item.put("leaveReason", row.getLeaveReason() != null ? row.getLeaveReason() : "");
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项（列与导入模板对齐）
     */
    private Map<String, Object> buildFailureItem(HrEmployee row, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("employeeNo", row.getEmployeeNo() != null ? row.getEmployeeNo() : "");
        item.put("employeeName", row.getEmployeeName() != null ? row.getEmployeeName() : "");
        item.put("gender", row.getGender() != null ? row.getGender() : "");
        item.put("idCard", row.getIdCard() != null ? row.getIdCard() : "");
        item.put("birthday", row.getBirthday() != null ? row.getBirthday().toString() : "");
        item.put("nation", row.getNation() != null ? row.getNation() : "");
        item.put("politicsStatus", row.getPoliticsStatus() != null ? row.getPoliticsStatus() : "");
        item.put("maritalStatus", row.getMaritalStatus() != null ? row.getMaritalStatus() : "");
        item.put("phone", row.getPhone() != null ? row.getPhone() : "");
        item.put("email", row.getEmail() != null ? row.getEmail() : "");
        item.put("emergencyContact", row.getEmergencyContact() != null ? row.getEmergencyContact() : "");
        item.put("emergencyPhone", row.getEmergencyPhone() != null ? row.getEmergencyPhone() : "");
        item.put("nativePlace", row.getNativePlace() != null ? row.getNativePlace() : "");
        item.put("address", row.getAddress() != null ? row.getAddress() : "");
        item.put("eduLevel", row.getEduLevel() != null ? row.getEduLevel() : "");
        item.put("graduationSchool", row.getGraduationSchool() != null ? row.getGraduationSchool() : "");
        item.put("major", row.getMajor() != null ? row.getMajor() : "");
        item.put("graduationDate", row.getGraduationDate() != null ? row.getGraduationDate().toString() : "");
        item.put("socialWorkYears", row.getSocialWorkYears() != null ? row.getSocialWorkYears().toString() : "");
        item.put("companyWorkYears", row.getCompanyWorkYears() != null ? row.getCompanyWorkYears().toString() : "");
        item.put("positionName", row.getPositionName() != null ? row.getPositionName() : "");
        item.put("skillLevel", row.getSkillLevel() != null ? row.getSkillLevel() : "");
        item.put("workType", row.getWorkType() != null ? row.getWorkType() : "");
        item.put("employeeType", row.getEmployeeType() != null ? row.getEmployeeType() : "");
        item.put("entryDate", row.getEntryDate() != null ? row.getEntryDate().toString() : "");
        item.put("regularDate", row.getRegularDate() != null ? row.getRegularDate().toString() : "");
        item.put("leaveDate", row.getLeaveDate() != null ? row.getLeaveDate().toString() : "");
        item.put("employeeStatus", row.getEmployeeStatus() != null ? row.getEmployeeStatus() : "");
        item.put("bankName", row.getBankName() != null ? row.getBankName() : "");
        item.put("bankAccount", row.getBankAccount() != null ? row.getBankAccount() : "");
        item.put("socialSecurityNo", row.getSocialSecurityNo() != null ? row.getSocialSecurityNo() : "");
        item.put("socialSecurityStatus", row.getSocialSecurityStatus() != null ? row.getSocialSecurityStatus() : "");
        item.put("houseFundNo", row.getHouseFundNo() != null ? row.getHouseFundNo() : "");
        item.put("leaveReason", row.getLeaveReason() != null ? row.getLeaveReason() : "");
        item.put("errorReason", errorReason);
        return item;
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
