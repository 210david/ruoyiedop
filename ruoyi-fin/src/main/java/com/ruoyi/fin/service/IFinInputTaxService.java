package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinInputTax;
import com.ruoyi.fin.domain.FinInventoryValuation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 进项税抵扣与存货跌价准备 Service 接口
 *
 * @author ruoyi
 */
public interface IFinInputTaxService
{
    // ==================== 进项税 ====================

    /**
     * 查询进项税列表
     *
     * @param tax 查询条件
     * @return 列表
     */
    List<FinInputTax> selectInputTaxList(FinInputTax tax);

    /**
     * 由应付单生成进项税台账
     *
     * @param apId 应付单ID
     * @return 进项税ID
     */
    Long generateFromAp(Long apId);

    /**
     * 标记已抵扣
     *
     * @param taxId        进项税ID
     * @param deductPeriod 抵扣所属期间
     * @return 影响行数
     */
    int markDeducted(Long taxId, String deductPeriod);

    /**
     * 标记作废
     *
     * @param taxId 进项税ID
     * @return 影响行数
     */
    int markVoid(Long taxId);

    /**
     * 批量标记过期（超过认证抵扣期12 个月）
     *
     * @return 影响行数
     */
    int markExpired();

    /**
     * 进项税汇总
     *
     * @param deductStatus 抵扣状态
     * @return 汇总
     */
    List<Map<String, Object>> summary(String deductStatus);

    /**
     * 即将过期的待抵扣进项税（预警）
     *
     * @param days 提前预警天数
     * @return 列表
     */
    List<FinInputTax> expiringSoon(Integer days);

    /**
     * 已过期但状态仍为待抵扣的笔数
     *
     * @return 笔数
     */
    int countOverdue();

    // ==================== 存货跌价准备 ====================

    /**
     * 查询跌价准备列表
     *
     * @param val 查询条件
     * @return 列表
     */
    List<FinInventoryValuation> selectValuationList(FinInventoryValuation val);

    /**
     * 待计提跌价准备的存货清单
     *
     * @param period 会计期间
     * @return 清单
     */
    List<Map<String, Object>> pendingProvision(String period);

    /**
     * 执行跌价计提
     *
     * @param period      会计期间
     * @param materialId  物料ID
     * @param ratio       跌价比例（0~1）
     * @return 计提金额
     */
    BigDecimal provision(String period, Long materialId, BigDecimal ratio);

    /**
     * 冲回跌价准备
     *
     * @param valId 记录ID
     * @return 影响行数
     */
    int reverseProvision(Long valId);

    /**
     * 期间跌价准备汇总
     *
     * @param period 会计期间
     * @return 汇总
     */
    Map<String, Object> provisionSummary(String period);
}