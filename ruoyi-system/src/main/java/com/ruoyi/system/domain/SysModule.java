package com.ruoyi.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 系统模块开关对象 sys_module
 *
 * @author ruoyi
 */
public class SysModule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模块ID */
    private Long moduleId;

    /** 模块名称 */
    private String moduleName;

    /** 模块标识 */
    private String moduleKey;

    /** 顶层菜单ID */
    private Long rootMenuId;

    /** 模块状态（0启用 1停用） */
    private String status;

    /** 是否核心模块（1=核心，不可关闭 0=普通） */
    private String isCore;

    /** 显示顺序 */
    private Integer orderNum;

    /** 关联角色数量（统计字段，非表字段） */
    private Integer roleCount;

    /** 关联定时任务数量（统计字段，非表字段） */
    private Integer jobCount;

    public Long getModuleId()
    {
        return moduleId;
    }

    public void setModuleId(Long moduleId)
    {
        this.moduleId = moduleId;
    }

    @NotBlank(message = "模块名称不能为空")
    @Size(min = 0, max = 50, message = "模块名称长度不能超过50个字符")
    public String getModuleName()
    {
        return moduleName;
    }

    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName;
    }

    @NotBlank(message = "模块标识不能为空")
    @Size(min = 0, max = 50, message = "模块标识长度不能超过50个字符")
    public String getModuleKey()
    {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey)
    {
        this.moduleKey = moduleKey;
    }

    public Long getRootMenuId()
    {
        return rootMenuId;
    }

    public void setRootMenuId(Long rootMenuId)
    {
        this.rootMenuId = rootMenuId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getIsCore()
    {
        return isCore;
    }

    public void setIsCore(String isCore)
    {
        this.isCore = isCore;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public Integer getRoleCount()
    {
        return roleCount;
    }

    public void setRoleCount(Integer roleCount)
    {
        this.roleCount = roleCount;
    }

    public Integer getJobCount()
    {
        return jobCount;
    }

    public void setJobCount(Integer jobCount)
    {
        this.jobCount = jobCount;
    }
}
