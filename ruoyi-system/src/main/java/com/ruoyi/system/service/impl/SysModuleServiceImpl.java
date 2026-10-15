package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.SysModule;
import com.ruoyi.system.mapper.SysModuleMapper;
import com.ruoyi.system.service.ISysModuleService;

/**
 * 系统模块开关 服务实现
 *
 * 级联停用/启用流程（V1.1方案）：
 * 1. 条件更新 sys_module 状态（乐观并发控制，状态不一致时报错回滚）；
 * 2. 停用：菜单整树置停用、启用中的关联角色/任务停用并追加"[模块停用:key]"标记；
 *    启用：菜单整树恢复、仅恢复带标记的角色/任务并移除标记；
 * 3. 事务提交后清除 Redis 登录令牌缓存，强制所有在线用户重新登录以刷新权限。
 *
 * @author ruoyi
 */
@Service
public class SysModuleServiceImpl implements ISysModuleService
{
    @Autowired
    private SysModuleMapper moduleMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public List<SysModule> selectModuleList(SysModule module)
    {
        return moduleMapper.selectModuleList(module);
    }

    @Override
    public SysModule selectModuleById(Long moduleId)
    {
        return moduleMapper.selectModuleById(moduleId);
    }

    @Override
    public SysModule selectModuleByKey(String moduleKey)
    {
        return moduleMapper.selectModuleByKey(moduleKey);
    }

    @Override
    @Transactional
    public int changeStatus(Long moduleId, String status, String operName)
    {
        SysModule module = moduleMapper.selectModuleById(moduleId);
        if (module == null)
        {
            throw new ServiceException("模块不存在或已被删除");
        }
        if ("1".equals(status) && "1".equals(module.getIsCore()))
        {
            throw new ServiceException("模块【" + module.getModuleName() + "】为核心模块，不允许停用");
        }
        if (!"0".equals(status) && !"1".equals(status))
        {
            throw new ServiceException("非法的模块状态值");
        }

        // 1. 条件更新模块状态（防并发：仅当状态仍为读取时的旧值时生效）
        int rows = moduleMapper.updateModuleStatusConditional(moduleId, module.getStatus(), status, operName);
        if (rows == 0)
        {
            throw new ServiceException("模块状态已被其他用户修改，请刷新后重试");
        }

        // 2. 级联处理菜单树与关联角色/任务
        List<Long> menuIds = module.getRootMenuId() == null ? new ArrayList<>() : moduleMapper.selectMenuChildIds(module.getRootMenuId());
        if ("1".equals(status))
        {
            if (!menuIds.isEmpty())
            {
                moduleMapper.updateMenuStatusByMenuIds(menuIds, "1");
            }
            moduleMapper.disableModuleRoles(module.getModuleKey());
            moduleMapper.disableModuleJobs(module.getModuleKey());
        }
        else
        {
            if (!menuIds.isEmpty())
            {
                moduleMapper.updateMenuStatusByMenuIds(menuIds, "0");
            }
            moduleMapper.enableModuleRoles(module.getModuleKey());
            moduleMapper.enableModuleJobs(module.getModuleKey());
        }

        // 3. 事务提交后清除登录令牌缓存，强制所有在线用户重新登录以刷新权限
        registerTokenClearAfterCommit();

        return rows;
    }

    @Override
    @Transactional
    public int enableAll(String operName)
    {
        int count = 0;
        List<SysModule> modules = moduleMapper.selectModuleList(new SysModule());
        for (SysModule module : modules)
        {
            if ("1".equals(module.getStatus()) && !"1".equals(module.getIsCore()))
            {
                changeStatus(module.getModuleId(), "0", operName);
                count++;
            }
        }
        return count;
    }

    /**
     * 注册事务提交成功后的登录缓存清理（提交失败/回滚时不清理）
     */
    private void registerTokenClearAfterCommit()
    {
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                @Override
                public void afterCommit()
                {
                    Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
                    if (keys != null && !keys.isEmpty())
                    {
                        redisCache.deleteObject(keys);
                    }
                }
            });
        }
        else
        {
            Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
            if (keys != null && !keys.isEmpty())
            {
                redisCache.deleteObject(keys);
            }
        }
    }
}
