package com.ruoyi.safety.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考试手动添加人员对象 biz_safety_exam_user
 *
 * 考生来源=手动添加（attendee_source=2）时，场次与系统用户的关联名单。
 *
 * @author ruoyi
 */
public class SafetyExamUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long examUserId;

    /** 考试场次ID */
    private Long examId;

    /** 系统用户ID（sys_user） */
    private Long userId;

    /** 用户显示名（冗余） */
    private String userName;

    /** 归属部门ID（数据权限） */
    private Long deptId;

    /** 部门名称（查询时 join sys_dept 带出，非持久化） */
    private String deptName;

    /** 岗位名称（查询时 join 岗位关联带出，多岗顿号拼接，非持久化） */
    private String postName;

    private Long enterpriseId;
    private String delFlag;

    public Long getExamUserId() { return examUserId; }
    public void setExamUserId(Long examUserId) { this.examUserId = examUserId; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getPostName() { return postName; }
    public void setPostName(String postName) { this.postName = postName; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
