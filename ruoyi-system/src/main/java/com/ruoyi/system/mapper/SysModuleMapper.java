package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysModule;

/**
 * 系统模块开关 数据层
 *
 * @author ruoyi
 */
public interface SysModuleMapper
{
    /**
     * 查询模块列表（含角色/任务统计）
     */
    public List<SysModule> selectModuleList(SysModule module);

    /**
     * 根据模块ID查询
     */
    public SysModule selectModuleById(Long moduleId);

    /**
     * 根据模块标识查询
     */
    public SysModule selectModuleByKey(String moduleKey);

    /**
     * 条件更新模块状态（乐观并发控制：仅当当前状态等于期望旧值时生效）
     *
     * @return 影响行数
     */
    public int updateModuleStatusConditional(@Param("moduleId") Long moduleId, @Param("oldStatus") String oldStatus, @Param("newStatus") String newStatus, @Param("updateBy") String updateBy);

    /**
     * 查询指定菜单及其全部子孙菜单ID（含自身）
     */
    public List<Long> selectMenuChildIds(Long menuId);

    /**
     * 批量更新菜单状态
     */
    public int updateMenuStatusByMenuIds(@Param("menuIds") List<Long> menuIds, @Param("status") String status);

    /**
     * 停用模块关联角色（仅停用当前启用中的，并追加停用标记备注）
     */
    public int disableModuleRoles(String moduleKey);

    /**
     * 恢复模块关联角色（仅恢复带停用标记的，并移除标记）
     */
    public int enableModuleRoles(String moduleKey);

    /**
     * 暂停模块关联定时任务（仅暂停当前正常状态的，并追加停用标记备注）
     */
    public int disableModuleJobs(String moduleKey);

    /**
     * 恢复模块关联定时任务（仅恢复带停用标记的，并移除标记）
     */
    public int enableModuleJobs(String moduleKey);
}
