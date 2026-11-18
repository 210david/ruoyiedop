package com.ruoyi.system.domain;

/**
 * 用户常用功能（首页快捷入口）
 *
 * 两种来源：
 * 1. source = 'A'：系统按用户实际访问频次自动沉淀，长期未访问可被淘汰；
 * 2. source = 'M'：用户手动固定，不受频次淘汰影响，始终排在前面。
 *
 * @author ruoyi
 */
public class SysUserShortcut
{
    /** 主键 */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 菜单路由（唯一标识） */
    private String menuPath;

    /** 菜单名称 */
    private String menuTitle;

    /** 菜单图标 */
    private String menuIcon;

    /** 所属模块编码 */
    private String moduleCode;

    /** 来源（A=按频次自动 M=用户手动固定） */
    private String source;

    /** 访问次数 */
    private Integer visitCount;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getMenuPath()
    {
        return menuPath;
    }

    public void setMenuPath(String menuPath)
    {
        this.menuPath = menuPath;
    }

    public String getMenuTitle()
    {
        return menuTitle;
    }

    public void setMenuTitle(String menuTitle)
    {
        this.menuTitle = menuTitle;
    }

    public String getMenuIcon()
    {
        return menuIcon;
    }

    public void setMenuIcon(String menuIcon)
    {
        this.menuIcon = menuIcon;
    }

    public String getModuleCode()
    {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode)
    {
        this.moduleCode = moduleCode;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public Integer getVisitCount()
    {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount)
    {
        this.visitCount = visitCount;
    }
}
