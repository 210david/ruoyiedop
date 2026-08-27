package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotBlank;

public class HrEmployee extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long employeeId;
    @NotBlank(message = "工号不能为空")
    @Excel(name = "工号")
    private String employeeNo;
    @NotBlank(message = "姓名不能为空")
    @Excel(name = "姓名")
    private String employeeName;
    @Excel(name = "性别")
    private String gender;
    @Excel(name = "身份证号")
    private String idCard;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出生日期")
    private Date birthday;
    @Excel(name = "民族")
    private String nation;
    @Excel(name = "政治面貌")
    private String politicsStatus;
    @Excel(name = "婚姻状况")
    private String maritalStatus;
    @Excel(name = "联系电话")
    private String phone;
    @Excel(name = "邮箱")
    private String email;
    @Excel(name = "紧急联系人")
    private String emergencyContact;
    @Excel(name = "紧急联系电话")
    private String emergencyPhone;
    @Excel(name = "籍贯")
    private String nativePlace;
    @Excel(name = "现住址")
    private String address;
    @Excel(name = "学历")
    private String eduLevel;
    @Excel(name = "毕业院校")
    private String graduationSchool;
    @Excel(name = "专业")
    private String major;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "毕业日期")
    private Date graduationDate;
    @Excel(name = "社会工龄")
    private Integer socialWorkYears;
    @Excel(name = "本公司工龄")
    private Integer companyWorkYears;
    @Excel(name = "所属组织ID")
    private Long orgId;
    @Excel(name = "岗位ID")
    private Long positionId;
    @Excel(name = "岗位名称")
    private String positionName;
    @Excel(name = "技能等级")
    private String skillLevel;
    @Excel(name = "工时制度")
    private String workType;
    @Excel(name = "用工类型")
    private String employeeType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入职日期")
    private Date entryDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "转正日期")
    private Date regularDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "离职日期")
    private Date leaveDate;
    @Excel(name = "员工状态")
    private String employeeStatus;
    @Excel(name = "开户银行")
    private String bankName;
    @Excel(name = "银行卡号")
    private String bankAccount;
    @Excel(name = "社保号")
    private String socialSecurityNo;
    @Excel(name = "参保状态")
    private String socialSecurityStatus;
    @Excel(name = "公积金号")
    private String houseFundNo;
    @Excel(name = "离职原因")
    private String leaveReason;
    @Excel(name = "黑名单")
    private String blacklistFlag;
    @Excel(name = "黑名单原因")
    private String blacklistReason;
    @Excel(name = "关联用户ID")
    private Long userId;
    @Excel(name = "照片URL")
    private String photo;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    /** 明细子表 */
    private List<HrEmployeeFamily> familyList;
    private List<HrEmployeeEducation> educationList;
    private List<HrEmployeeWorkExp> workExpList;
    private List<HrEmployeeQualification> qualificationList;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public Date getBirthday() { return birthday; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public String getNation() { return nation; }
    public void setNation(String nation) { this.nation = nation; }
    public String getPoliticsStatus() { return politicsStatus; }
    public void setPoliticsStatus(String politicsStatus) { this.politicsStatus = politicsStatus; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }
    public String getNativePlace() { return nativePlace; }
    public void setNativePlace(String nativePlace) { this.nativePlace = nativePlace; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEduLevel() { return eduLevel; }
    public void setEduLevel(String eduLevel) { this.eduLevel = eduLevel; }
    public String getGraduationSchool() { return graduationSchool; }
    public void setGraduationSchool(String graduationSchool) { this.graduationSchool = graduationSchool; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Date getGraduationDate() { return graduationDate; }
    public void setGraduationDate(Date graduationDate) { this.graduationDate = graduationDate; }
    public Integer getSocialWorkYears() { return socialWorkYears; }
    public void setSocialWorkYears(Integer socialWorkYears) { this.socialWorkYears = socialWorkYears; }
    public Integer getCompanyWorkYears() { return companyWorkYears; }
    public void setCompanyWorkYears(Integer companyWorkYears) { this.companyWorkYears = companyWorkYears; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }
    public Date getEntryDate() { return entryDate; }
    public void setEntryDate(Date entryDate) { this.entryDate = entryDate; }
    public Date getRegularDate() { return regularDate; }
    public void setRegularDate(Date regularDate) { this.regularDate = regularDate; }
    public Date getLeaveDate() { return leaveDate; }
    public void setLeaveDate(Date leaveDate) { this.leaveDate = leaveDate; }
    public String getEmployeeStatus() { return employeeStatus; }
    public void setEmployeeStatus(String employeeStatus) { this.employeeStatus = employeeStatus; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getSocialSecurityNo() { return socialSecurityNo; }
    public void setSocialSecurityNo(String socialSecurityNo) { this.socialSecurityNo = socialSecurityNo; }
    public String getSocialSecurityStatus() { return socialSecurityStatus; }
    public void setSocialSecurityStatus(String socialSecurityStatus) { this.socialSecurityStatus = socialSecurityStatus; }
    public String getHouseFundNo() { return houseFundNo; }
    public void setHouseFundNo(String houseFundNo) { this.houseFundNo = houseFundNo; }
    public String getLeaveReason() { return leaveReason; }
    public void setLeaveReason(String leaveReason) { this.leaveReason = leaveReason; }
    public String getBlacklistFlag() { return blacklistFlag; }
    public void setBlacklistFlag(String blacklistFlag) { this.blacklistFlag = blacklistFlag; }
    public String getBlacklistReason() { return blacklistReason; }
    public void setBlacklistReason(String blacklistReason) { this.blacklistReason = blacklistReason; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<HrEmployeeFamily> getFamilyList() { return familyList; }
    public void setFamilyList(List<HrEmployeeFamily> familyList) { this.familyList = familyList; }
    public List<HrEmployeeEducation> getEducationList() { return educationList; }
    public void setEducationList(List<HrEmployeeEducation> educationList) { this.educationList = educationList; }
    public List<HrEmployeeWorkExp> getWorkExpList() { return workExpList; }
    public void setWorkExpList(List<HrEmployeeWorkExp> workExpList) { this.workExpList = workExpList; }
    public List<HrEmployeeQualification> getQualificationList() { return qualificationList; }
    public void setQualificationList(List<HrEmployeeQualification> qualificationList) { this.qualificationList = qualificationList; }
}
