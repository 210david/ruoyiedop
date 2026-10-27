package com.ruoyi.system.service;

import java.util.Map;
import com.ruoyi.system.domain.SysTableConfig;

/**
 * 表格列配置 Service 接口（列宽 + 列显隐）
 *
 * 优先级：个人配置 > 全局配置 > 默认值
 *
 * @author ruoyi
 */
public interface ISysTableConfigService
{
    /**
     * 获取某页面合并后的列宽配置（个人覆盖全局）
     *
     * @param tableKey 页面标识
     * @param userId 当前用户ID
     * @return 列宽映射 { colProp: width }
     */
    public Map<String, Integer> getMergedWidths(String tableKey, Long userId);

    /**
     * 获取某页面合并后的列显隐配置（个人覆盖全局）
     *
     * @param tableKey 页面标识
     * @param userId 当前用户ID
     * @return 列显隐映射 { colProp: visible }
     */
    public Map<String, Boolean> getMergedVisibility(String tableKey, Long userId);

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
    public void saveColumnWidth(String tableKey, String colProp, Integer colWidth, Long userId, String username);

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
    public void saveColumnVisible(String tableKey, String colProp, Boolean colVisible, Long userId, String username);

    /**
     * 获取某页面合并后的列顺序配置（个人覆盖全局）
     *
     * @param tableKey 页面标识
     * @param userId 当前用户ID
     * @return 列顺序映射 { colProp: order }
     */
    public Map<String, Integer> getMergedOrders(String tableKey, Long userId);

    /**
     * 保存列顺序配置
     * 管理员保存为全局配置（G），普通用户保存为个人配置（U）
     *
     * @param tableKey 页面标识
     * @param colProp 列属性名
     * @param colOrder 列顺序
     * @param userId 当前用户ID
     * @param username 当前用户名
     */
    public void saveColumnOrder(String tableKey, String colProp, Integer colOrder, Long userId, String username);

    /**
     * 根据主键删除
     *
     * @param configId 主键ID
     * @return 影响行数
     */
    public int deleteConfigById(Long configId);

    /**
     * 批量删除
     *
     * @param configIds 主键ID数组
     * @return 影响行数
     */
    public int deleteConfigByIds(Long[] configIds);
}
