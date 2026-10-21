package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinCostMethodConfig;
import com.ruoyi.fin.domain.FinFiscalConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 财务配置 Mapper（键值配置 + 成本方法配置）
 *
 * @author ruoyi
 */
public interface FinConfigMapper
{
    // ==================== 键值配置 ====================

    /**
     * 查询配置列表
     *
     * @param config 查询条件
     * @return 配置列表
     */
    List<FinFiscalConfig> selectConfigList(FinFiscalConfig config);

    /**
     * 按配置键查询
     *
     * @param configKey 配置键
     * @return 配置，不存在返回 null
     */
    FinFiscalConfig selectConfigByKey(@Param("configKey") String configKey);

    /**
     * 按分组查询配置
     *
     * @param configGroup 分组
     * @return 配置列表
     */
    List<FinFiscalConfig> selectByGroup(@Param("configGroup") String configGroup);

    /**
     * 按键更新配置值（不存在则插入）
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @param updateBy    操作人
     * @return 影响行数
     */
    int upsertConfig(@Param("configKey") String configKey,
                     @Param("configValue") String configValue,
                     @Param("updateBy") String updateBy);

    /**
     * 新增配置
     *
     * @param config 配置
     * @return 影响行数
     */
    int insertConfig(FinFiscalConfig config);

    /**
     * 删除配置
     *
     * @param configKey 配置键
     * @return 影响行数
     */
    int deleteConfig(@Param("configKey") String configKey);

    // ==================== 成本方法配置 ====================

    /**
     * 查询成本方法配置列表
     *
     * @param config 查询条件
     * @return 配置列表
     */
    List<FinCostMethodConfig> selectCostMethodList(FinCostMethodConfig config);

    /**
     * 按ID查询
     *
     * @param configId 配置ID
     * @return 配置
     */
    FinCostMethodConfig selectCostMethodById(@Param("configId") Long configId);

    /**
     * 取某物料生效的成本方法配置（物料级）
     *
     * @param materialId 物料ID
     * @return 配置，不存在返回 null
     */
    FinCostMethodConfig selectByMaterial(@Param("materialId") Long materialId);

    /**
     * 取全局默认成本方法配置（material_id 为空）
     *
     * @return 配置，不存在返回 null
     */
    FinCostMethodConfig selectGlobalConfig();

    /**
     * 新增成本方法配置
     *
     * @param config 配置
     * @return 影响行数
     */
    int insertCostMethod(FinCostMethodConfig config);

    /**
     * 修改成本方法配置
     *
     * @param config 配置
     * @return 影响行数
     */
    int updateCostMethod(FinCostMethodConfig config);

    /**
     * 删除成本方法配置
     *
     * @param configIds 配置ID数组
     * @return 影响行数
     */
    int deleteCostMethodByIds(Long[] configIds);

    /**
     * 已配置成本方法的物料数
     *
     * @return 物料数（不含全局）
     */
    int countMaterialCostMethod();
}