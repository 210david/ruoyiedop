package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysModule;

/**
 * 系统模块开关 服务层
 *
 * @author ruoyi
 */
public interface ISysModuleService
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
     * 切换模块启停状态（级联：菜单树、关联角色、关联定时任务，事务提交后强制在线用户重新登录）
     *
     * @param moduleId 模块ID
     * @param status   目标状态（0启用 1停用）
     * @param operName 操作人
     * @return 影响行数
     */
    public int changeStatus(Long moduleId, String status, String operName);

    /**
     * 一键恢复全部已停用模块（核心模块不参与）
     *
     * @param operName 操作人
     * @return 恢复的模块数量
     */
    public int enableAll(String operName);
}
