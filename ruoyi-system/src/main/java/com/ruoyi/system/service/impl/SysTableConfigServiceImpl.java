package com.ruoyi.system.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysTableConfig;
import com.ruoyi.system.mapper.SysTableConfigMapper;
import com.ruoyi.system.service.ISysTableConfigService;

/**
 * 表格列配置 Service 实现（列宽 + 列显隐）
 *
 * 优先级：个人配置 > 全局配置 > 默认值
 *
 * @author ruoyi
 */
@Service
public class SysTableConfigServiceImpl implements ISysTableConfigService
{
    @Autowired
    private SysTableConfigMapper configMapper;

    /**
     * 获取某页面合并后的列宽配置
     * 先加载全局配置，再用个人配置覆盖
     *
     * @param tableKey 页面标识
     * @param userId 当前用户ID
     * @return 列宽映射 { colProp: width }
     */
    @Override
    public Map<String, Integer> getMergedWidths(String tableKey, Long userId)
    {
        Map<String, Integer> result = new HashMap<>();

        // 1. 先加载全局配置
        List<SysTableConfig> globalConfigs = configMapper.selectGlobalConfigByTableKey(tableKey);
        for (SysTableConfig config : globalConfigs)
        {
            if (config.getColWidth() != null)
            {
                result.put(config.getColProp(), config.getColWidth());
            }
        }

        // 2. 再用个人配置覆盖（优先级更高）
        if (userId != null)
        {
            List<SysTableConfig> personalConfigs = configMapper.selectPersonalConfig(tableKey, userId);
            for (SysTableConfig config : personalConfigs)
            {
                if (config.getColWidth() != null)
                {
                    result.put(config.getColProp(), config.getColWidth());
                }
            }
        }

        return result;
    }

    /**
     * 获取某页面合并后的列显隐配置
     * 先加载全局配置，再用个人配置覆盖
     *
     * @param tableKey 页面标识
     * @param userId 当前用户ID
     * @return 列显隐映射 { colProp: visible }
     */
    @Override
    public Map<String, Boolean> getMergedVisibility(String tableKey, Long userId)
    {
        Map<String, Boolean> result = new HashMap<>();

        // 1. 先加载全局配置
        List<SysTableConfig> globalConfigs = configMapper.selectGlobalConfigByTableKey(tableKey);
        for (SysTableConfig config : globalConfigs)
        {
            if (config.getColVisible() != null)
            {
                result.put(config.getColProp(), config.getColVisible());
            }
        }

        // 2. 再用个人配置覆盖（优先级更高）
        if (userId != null)
        {
            List<SysTableConfig> personalConfigs = configMapper.selectPersonalConfig(tableKey, userId);
            for (SysTableConfig config : personalConfigs)
            {
                if (config.getColVisible() != null)
                {
                    result.put(config.getColProp(), config.getColVisible());
                }
            }
        }

        return result;
    }

    /**
     * 保存列宽配置
     * 管理员保存为全局配置（G），普通用户保存为个人配置（U）
     *
     * @param tableKey 页面标识
     * @param colProp 列属性名
     * @param colWidth 列宽
     * @param userId 当前用户ID
     * @param username 当前用户名
     */
    @Override
    public void saveColumnWidth(String tableKey, String colProp, Integer colWidth, Long userId, String username)
    {
        boolean isAdmin = SecurityUtils.isAdmin(userId);

        // 构建查询条件：判断该配置是否已存在
        SysTableConfig query = new SysTableConfig();
        query.setTableKey(tableKey);
        query.setColProp(colProp);
        if (isAdmin)
        {
            query.setConfigType("G");
            query.setUserId(null);
        }
        else
        {
            query.setConfigType("U");
            query.setUserId(userId);
        }

        SysTableConfig existing = configMapper.selectConfig(query);

        if (existing != null)
        {
            // 更新已有记录
            existing.setColWidth(colWidth);
            existing.setUpdateBy(username);
            configMapper.updateConfig(existing);
        }
        else
        {
            // 新增记录
            SysTableConfig config = new SysTableConfig();
            config.setTableKey(tableKey);
            config.setColProp(colProp);
            config.setColWidth(colWidth);
            config.setConfigType(isAdmin ? "G" : "U");
            config.setUserId(isAdmin ? null : userId);
            config.setCreateBy(username);
            config.setCreateTime(DateUtils.getNowDate());
            configMapper.insertConfig(config);
        }
    }

    /**
     * 保存列显隐配置
     * 管理员保存为全局配置（G），普通用户保存为个人配置（U）
     *
     * @param tableKey 页面标识
     * @param colProp 列属性名
     * @param colVisible 是否可见
     * @param userId 当前用户ID
     * @param username 当前用户名
     */
    @Override
    public void saveColumnVisible(String tableKey, String colProp, Boolean colVisible, Long userId, String username)
    {
        boolean isAdmin = SecurityUtils.isAdmin(userId);

        // 构建查询条件：判断该配置是否已存在
        SysTableConfig query = new SysTableConfig();
        query.setTableKey(tableKey);
        query.setColProp(colProp);
        if (isAdmin)
        {
            query.setConfigType("G");
            query.setUserId(null);
        }
        else
        {
            query.setConfigType("U");
            query.setUserId(userId);
        }

        SysTableConfig existing = configMapper.selectConfig(query);

        if (existing != null)
        {
            // 更新已有记录
            existing.setColVisible(colVisible);
            existing.setUpdateBy(username);
            configMapper.updateConfig(existing);
        }
        else
        {
            // 新增记录
            SysTableConfig config = new SysTableConfig();
            config.setTableKey(tableKey);
            config.setColProp(colProp);
            config.setColVisible(colVisible);
            config.setConfigType(isAdmin ? "G" : "U");
            config.setUserId(isAdmin ? null : userId);
            config.setCreateBy(username);
            config.setCreateTime(DateUtils.getNowDate());
            configMapper.insertConfig(config);
        }
    }

    /**
     * 根据主键删除
     */
    @Override
    public int deleteConfigById(Long configId)
    {
        return configMapper.deleteConfigById(configId);
    }

    /**
     * 批量删除
     */
    @Override
    public int deleteConfigByIds(Long[] configIds)
    {
        return configMapper.deleteConfigByIds(configIds);
    }
}
