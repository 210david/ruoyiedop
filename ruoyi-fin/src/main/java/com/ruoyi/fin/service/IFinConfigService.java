package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinCostMethodConfig;
import com.ruoyi.fin.domain.FinFiscalConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 财务配置 Service 接口
 *
 * <p>统一管理两张配置表：
 * <ul>
 *   <li>{@code fin_fiscal_config} —— 键值配置（税率、账龄比例、开关）</li>
 *   <li>{@code fin_cost_method_config} —— 成本方法配置（物料级 + 全局）</li>
 * </ul></p>
 *
 * @author ruoyi
 */
public interface IFinConfigService
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
     * 按键取配置值（带默认值回落）
     *
     * @param configKey    配置键
     * @param defaultValue 默认值（配置不存在时返回）
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 取数值型配置
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 数值
     */
    BigDecimal getConfigNumber(String configKey, BigDecimal defaultValue);

    /**
     * 取布尔型配置
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return true/false
     */
    boolean getConfigBoolean(String configKey, boolean defaultValue);

    /**
     * 更新配置值（不存在则插入）
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 影响行数
     */
    int updateConfig(String configKey, String configValue);

    /**
     * 新增配置项
     *
     * @param config 配置
     * @return 影响行数
     */
    int insertConfig(FinFiscalConfig config);

    /**
     * 删除配置项
     *
     * @param configKey 配置键
     * @return 影响行数
     */
    int deleteConfig(String configKey);

    // ==================== 成本方法配置 ====================

    /**
     * 查询成本方法配置列表
     *
     * @param config 查询条件
     * @return 配置列表
     */
    List<FinCostMethodConfig> selectCostMethodList(FinCostMethodConfig config);

    /**
     * 取某物料的成本方法（物料级 &gt; 全局 &gt; 系统默认）
     *
     * @param materialId 物料ID
     * @return 计价方法
     */
    String getCostMethod(Long materialId);

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
     * @param ids 配置ID数组
     * @return 影响行数
     */
    int deleteCostMethodByIds(Long[] ids);

    /**
     * 取应收账龄计提比例（解析 ar_aging_ratio JSON）
     *
     * @return 区间 → 比例的映射，键形如 "1-30"、"90+"
     */
    Map<String, BigDecimal> getArAgingRatio();
}