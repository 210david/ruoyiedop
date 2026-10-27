package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinInputTax;
import com.ruoyi.fin.domain.FinInventoryValuation;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 进项税抵扣与存货跌价准备 Mapper
 *
 * @author ruoyi
 */
public interface FinInputTaxMapper
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
     * 按应付单ID查询进项税
     *
     * @param apId 应付单ID
     * @return 进项税记录
     */
    FinInputTax selectByApId(@Param("apId") Long apId);

    /**
     * 新增进项税
     *
     * @param tax 进项税
     * @return 影响行数
     */
    int insertInputTax(FinInputTax tax);

    /**
     * 标记已抵扣
     *
     * @param taxId        进项税ID
     * @param deductPeriod 抵扣所属期间
     * @param voucherId    关联凭证ID
     * @return 影响行数
     */
    int markDeducted(@Param("taxId") Long taxId,
                     @Param("deductPeriod") String deductPeriod,
                     @Param("voucherId") Long voucherId);

    /**
     * 标记作废
     *
     * @param taxId 进项税ID
     * @return 影响行数
     */
    int markVoid(@Param("taxId") Long taxId);

    /**
     * 标记已过期（跑批用：超过认证抵扣期限仍未抵扣）
     *
     * @param today 今日
     * @return 影响行数
     */
    int markExpired(@Param("today") Date today);

    /**
     * 进项税汇总（抵扣台账取数）
     *
     * @param deductStatus 抵扣状态（空查全部）
     * @return 按状态汇总
     */
    List<Map<String, Object>> sumByStatus(@Param("deductStatus") String deductStatus);

    /**
     * 即将过期的进项税（预警）
     *
     * @param today 今日
     * @param days  提前预警天数
     * @return 列表
     */
    List<FinInputTax> selectExpiringSoon(@Param("today") Date today,
                                         @Param("days") Integer days,
                                         @Param("limit") int limit);

    /**
     * 已过期但状态仍为待抵扣的数量
     *
     * @param today 今日
     * @return 笔数
     */
    int countOverdue(@Param("today") Date today);

    // ==================== 存货跌价准备 ====================

    /**
     * 查询跌价准备列表
     *
     * @param val 查询条件
     * @return 列表
     */
    List<FinInventoryValuation> selectValuationList(FinInventoryValuation val);

    /**
     * 待计提跌价准备的存货（取自成本台账）
     *
     * <p>已在本期计提过的物料不再重复出现，避免重复计提。</p>
     *
     * @param period 会计期间
     * @return 待计提清单
     */
    List<Map<String, Object>> selectPendingProvision(@Param("period") String period);

    /**
     * 新增跌价准备记录
     *
     * @param val 记录
     * @return 影响行数
     */
    int insertValuation(FinInventoryValuation val);

    /**
     * 冲回跌价准备（本期计提转回）
     *
     * @param valId 记录ID
     * @return 影响行数
     */
    int reverseProvision(@Param("valId") Long valId);

    /**
     * 删除跌价准备记录
     *
     * @param valIds 记录ID数组
     * @return 影响行数
     */
    int deleteValuationByIds(Long[] valIds);

    /**
     * 期间跌价准备汇总
     *
     * @param period 会计期间
     * @return 汇总
     */
    Map<String, Object> sumProvisionByPeriod(@Param("period") String period);

    /**
     * 累计已计提跌价准备（避免重复计提）
     *
     * @param materialId 物料ID
     * @return 累计金额
     */
    BigDecimal sumProvisionByMaterial(@Param("materialId") Long materialId);
}