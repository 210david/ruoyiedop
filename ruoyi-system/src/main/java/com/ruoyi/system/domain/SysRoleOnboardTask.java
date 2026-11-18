package com.ruoyi.system.domain;

/**
 * 岗位新人任务模板
 *
 * 一个「岗位角色」对应一条业务主线（3~5 步），每步都是真实业务动作，
 * 而不是「点击这里了解功能」式的空教程。
 *
 * @author ruoyi
 */
public class SysRoleOnboardTask
{
    /** 主键 */
    private Long id;

    /** 角色ID（岗位） */
    private Long roleId;

    /** 角色名称（非表字段，联表带出用于展示） */
    private String roleName;

    /** 步骤序号 */
    private Integer seq;

    /** 任务标题 */
    private String title;

    /** 完成标准说明 */
    private String description;

    /** 任务落点菜单 */
    private String menuPath;

    /** 判定方式（MENU_VISITED=访问过该菜单 MANUAL=手工确认） */
    private String checkType;

    /** 判定参数 */
    private String checkParam;

    /** 是否启用（0启用 1停用） */
    private String enabled;

    /** 是否已完成（非表字段，按判定规则实时计算） */
    private Boolean done;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public String getRoleName()
    {
        return roleName;
    }

    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    public Integer getSeq()
    {
        return seq;
    }

    public void setSeq(Integer seq)
    {
        this.seq = seq;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getMenuPath()
    {
        return menuPath;
    }

    public void setMenuPath(String menuPath)
    {
        this.menuPath = menuPath;
    }

    public String getCheckType()
    {
        return checkType;
    }

    public void setCheckType(String checkType)
    {
        this.checkType = checkType;
    }

    public String getCheckParam()
    {
        return checkParam;
    }

    public void setCheckParam(String checkParam)
    {
        this.checkParam = checkParam;
    }

    public String getEnabled()
    {
        return enabled;
    }

    public void setEnabled(String enabled)
    {
        this.enabled = enabled;
    }

    public Boolean getDone()
    {
        return done;
    }

    public void setDone(Boolean done)
    {
        this.done = done;
    }
}
