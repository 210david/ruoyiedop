package com.ruoyi.ai.service;

import java.util.Map;

/**
 * AI行业化预警 Service接口（P3）
 *
 * @author ruoyi
 */
public interface IAiBizAlertService
{
    /**
     * 临期批次出库建议（FEFO口径，AI解读）
     *
     * @param days 临期天数阈值（默认30）
     */
    public Map<String, Object> expiryAdvice(Integer days);

    /**
     * 产出率/损耗异常解读（AI解读）
     *
     * @param threshold 损耗率阈值%（默认10）
     */
    public Map<String, Object> lossInterpret(java.math.BigDecimal threshold);
}
