package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinArWriteoff;
import com.ruoyi.fin.domain.FinBadDebt;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 应收核销与坏账计提 Mapper
 *
 * @author ruoyi
 */
public interface FinArWriteoffMapper
{
    /**
     * 新增核销记录
     *
     * @param writeoff 核销记录
     * @return 影响行数
     */
    int insertWriteoff(FinArWriteoff writeoff);

    /**
     * 查询某应收单的核销历史
     *
     * @param arId 应收单ID
     * @return 核销记录列表
     */
    List<FinArWriteoff> selectByArId(@Param("arId") Long arId);

    /**
     * 累计核销金额
     *
     * @param arId 应收单ID
     * @return 累计核销额
     */
    BigDecimal sumWriteoffAmount(@Param("arId") Long arId);

    /**
     * 新增坏账计提记录
     *
     * @param badDebt 坏账计提
     * @return 影响行数
     */
    int insertBadDebt(FinBadDebt badDebt);

    /**
     * 查询坏账计提记录
     *
     * @param period 期间（可空查全部）
     * @return 计提记录
     */
    List<FinBadDebt> selectBadDebtList(@Param("period") String period);
}
