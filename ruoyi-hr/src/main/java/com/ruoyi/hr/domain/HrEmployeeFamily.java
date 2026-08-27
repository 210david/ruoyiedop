package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrEmployeeFamily extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long familyId;
    @NotNull(message = "员工ID不能为空") @Excel(name = "员工ID")
    private Long employeeId;

    @Excel(name = "姓名")
    private String memberName;

    @Excel(name = "关系")
    private String relationship;

    @Excel(name = "性别")
    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "出生日期")
    private Date birthday;

    @Excel(name = "联系电话")
    private String phone;

    @Excel(name = "工作单位/职业")
    private String occupation;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "删除标志")
    private String delFlag;


    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Date getBirthday() { return birthday; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
