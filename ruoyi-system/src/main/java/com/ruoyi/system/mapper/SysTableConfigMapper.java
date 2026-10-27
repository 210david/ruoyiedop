package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysTableConfig;

/**
 * 表格列宽配置 数据层
 *
 * @author ruoyi
 */
public interface SysTableConfigMapper
{
    /**
     * 查询某页面的全局列宽配置（config_type='G'）
     *
     * @param tableKey 页面标识
     * @return 列宽配置列表
     */
    public List<SysTableConfig> selectGlobalConfigByTableKey(@Param("tableKey") String tableKey);

    /**
     * 查询某用户在某页面的个人列宽配置（config_type='U'）
     *
     * @param tableKey 页面标识
     * @param userId 用户ID
     * @return 列宽配置列表
     */
    public List<SysTableConfig> selectPersonalConfig(@Param("tableKey") String tableKey, @Param("userId") Long userId);

    /**
     * 查询某页面+某类型+某用户的具体配置（用于判断是否存在）
     *
     * @param config 查询条件
     * @return 配置记录
     */
    public SysTableConfig selectConfig(SysTableConfig config);

    /**
     * 新增列宽配置
     *
     * @param config 列宽配置
     * @return 影响行数
     */
    public int insertConfig(SysTableConfig config);

    /**
     * 修改列宽配置
     *
     * @param config 列宽配置
     * @return 影响行数
     */
    public int updateConfig(SysTableConfig config);

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
