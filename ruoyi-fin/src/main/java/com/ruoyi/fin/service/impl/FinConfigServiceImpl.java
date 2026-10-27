package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinCostMethodConfig;
import com.ruoyi.fin.domain.FinFiscalConfig;
import com.ruoyi.fin.mapper.FinConfigMapper;
import com.ruoyi.fin.service.IFinConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务配置 Service 实现
 *
 * @author ruoyi
 */
@Service
public class FinConfigServiceImpl implements IFinConfigService
{
    private static final Logger log = LoggerFactory.getLogger(FinConfigServiceImpl.class);

    /** 系统默认计价方法（配置缺失时的兜底） */
    private static final String DEFAULT_COST_METHOD = "WEIGHTED_AVG";

    @Autowired
    private FinConfigMapper configMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<FinFiscalConfig> selectConfigList(FinFiscalConfig config)
    {
        return configMapper.selectConfigList(config);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue)
    {
        FinFiscalConfig c = configMapper.selectConfigByKey(configKey);
        if (c == null || c.getConfigValue() == null || c.getConfigValue().trim().isEmpty())
        {
            return defaultValue;
        }
        return c.getConfigValue();
    }

    @Override
    public BigDecimal getConfigNumber(String configKey, BigDecimal defaultValue)
    {
        String v = getConfigValue(configKey, null);
        if (v == null)
        {
            return defaultValue;
        }
        try
        {
            return new BigDecimal(v.trim());
        }
        catch (NumberFormatException e)
        {
            log.warn("配置 {} 的值不是合法数字：{}，使用默认值 {}", configKey, v, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public boolean getConfigBoolean(String configKey, boolean defaultValue)
    {
        String v = getConfigValue(configKey, null);
        if (v == null)
        {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateConfig(String configKey, String configValue)
    {
        if (configKey == null || configKey.trim().isEmpty())
        {
            throw new IllegalArgumentException("配置键不能为空");
        }
        if (configValue == null)
        {
            throw new IllegalArgumentException("配置值不能为空");
        }
        return configMapper.upsertConfig(configKey.trim(), configValue.trim(),
                SecurityUtils.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertConfig(FinFiscalConfig config)
    {
        if (configMapper.selectConfigByKey(config.getConfigKey()) != null)
        {
            throw new IllegalArgumentException("配置键已存在：" + config.getConfigKey());
        }
        config.setCreateBy(SecurityUtils.getUsername());
        return configMapper.insertConfig(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteConfig(String configKey)
    {
        // 内置配置不允许删除（删除会破坏引擎兜底）
        if (isBuiltinKey(configKey))
        {
            throw new IllegalArgumentException(
                    "内置配置「" + configKey + "」不允许删除，如需变更请修改其值");
        }
        return configMapper.deleteConfig(configKey);
    }

    @Override
    public List<FinCostMethodConfig> selectCostMethodList(FinCostMethodConfig config)
    {
        return configMapper.selectCostMethodList(config);
    }

    @Override
    public String getCostMethod(Long materialId)
    {
        // 优先级：物料级配置 > 全局配置 > 系统默认
        if (materialId != null)
        {
            FinCostMethodConfig item = configMapper.selectByMaterial(materialId);
            if (item != null && item.getCostMethod() != null)
            {
                return item.getCostMethod();
            }
        }
        FinCostMethodConfig global = configMapper.selectGlobalConfig();
        if (global != null && global.getCostMethod() != null)
        {
            return global.getCostMethod();
        }
        String fromConfig = getConfigValue("cost_method", null);
        return fromConfig != null ? fromConfig : DEFAULT_COST_METHOD;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCostMethod(FinCostMethodConfig config)
    {
        validateMethod(config);
        config.setStatus(config.getStatus() == null ? "0" : config.getStatus());
        config.setTaxIncluded(config.getTaxIncluded() == null ? "Y" : config.getTaxIncluded());
        config.setEffectiveDate(config.getEffectiveDate() == null
                ? new java.util.Date() : config.getEffectiveDate());
        config.setCompanyId(config.getCompanyId() == null ? 0L : config.getCompanyId());
        config.setCreateBy(SecurityUtils.getUsername());
        return configMapper.insertCostMethod(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCostMethod(FinCostMethodConfig config)
    {
        if (config.getConfigId() == null)
        {
            throw new IllegalArgumentException("配置ID不能为空");
        }
        validateMethod(config);
        config.setUpdateBy(SecurityUtils.getUsername());
        return configMapper.updateCostMethod(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCostMethodByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return 0;
        }
        // 全局默认配置不允许删除（否则所有物料失去计价依据）
        for (Long id : ids)
        {
            FinCostMethodConfig c = configMapper.selectCostMethodById(id);
            if (c != null && c.isGlobal())
            {
                throw new IllegalArgumentException("全局默认配置不允许删除");
            }
        }
        return configMapper.deleteCostMethodByIds(ids);
    }

    @Override
    public Map<String, BigDecimal> getArAgingRatio()
    {
        Map<String, BigDecimal> result = new HashMap<>();
        String json = getConfigValue("ar_aging_ratio", null);
        if (json == null || json.trim().isEmpty())
        {
            log.warn("未配置 ar_aging_ratio，坏账计提将使用代码内默认值");
            return result;
        }
        try
        {
            Map<String, BigDecimal> parsed =
                    objectMapper.readValue(json, new TypeReference<Map<String, BigDecimal>>() { });
            result.putAll(parsed);
        }
        catch (Exception e)
        {
            log.error("解析 ar_aging_ratio 配置失败：{}", json, e);
            throw new IllegalStateException("应收账龄计提比例配置格式错误，应为 JSON，例："
                    + "{\"1-30\":0.05,\"31-60\":0.10}");
        }
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 校验成本方法配置。
     */
    private void validateMethod(FinCostMethodConfig config)
    {
        if (config.getCostMethod() == null
                || (!FinCostMethodConfig.WEIGHTED_AVG.equals(config.getCostMethod())
                && !FinCostMethodConfig.FIFO.equals(config.getCostMethod())))
        {
            throw new IllegalArgumentException(
                    "计价方法非法，仅支持 WEIGHTED_AVG（加权平均）或 FIFO（先进先出）");
        }
        if (config.getTaxIncluded() != null
                && !"Y".equals(config.getTaxIncluded())
                && !"N".equals(config.getTaxIncluded()))
        {
            throw new IllegalArgumentException("含税标志只能是 Y 或 N");
        }
    }

    /**
     * 判断是否内置配置（不允许删除）。
     */
    private boolean isBuiltinKey(String key)
    {
        return "default_currency".equals(key)
                || "cost_method".equals(key)
                || "output_tax_rate".equals(key)
                || "ar_aging_ratio".equals(key)
                || "auto_voucher".equals(key);
    }
}