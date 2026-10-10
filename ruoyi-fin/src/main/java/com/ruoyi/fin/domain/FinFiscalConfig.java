package com.ruoyi.fin.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 财务配置 fin_fiscal_config
 *
 * <p><b>键值配置表</b>：config_key 为唯一键，config_value 存配置值（字符串）。
 * 当前已有 5 项：</p>
 * <table border="1">
 *   <tr><th>config_key</th><th>用途</th></tr>
 *   <tr><td>default_currency</td><td>默认币种（CNY）</td></tr>
 *   <tr><td>cost_method</td><td>默认计价方法（WEIGHTED_AVG/FIFO）</td></tr>
 *   <tr><td>output_tax_rate</td><td>默认销售税率（应收单拆分用）</td></tr>
 *   <tr><td>ar_aging_ratio</td><td>应收账龄坏账计提比例（JSON）</td></tr>
 *   <tr><td>auto_voucher</td><td>自动记账开关（true/false）</td></tr>
 * </table>
 *
 * <p><b>注意</b>：本表无 status 字段，删除用 del_flag='2' 逻辑删除。</p>
 *
 * @author ruoyi
 */
public class FinFiscalConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long configId;

    /** 预留账套ID */
    private Long companyId;

    /** 配置键 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置名称 */
    private String configName;

    /** 配置分组 */
    private String configGroup;

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }

    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }

    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }

    public String getConfigGroup() { return configGroup; }
    public void setConfigGroup(String configGroup) { this.configGroup = configGroup; }
}