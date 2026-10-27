package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinApWriteoff;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应付核销记录 Mapper
 *
 * @author ruoyi
 */
public interface FinApWriteoffMapper
{
    /**
     * 新增核销记录
     *
     * @param writeoff 核销记录
     * @return 影响行数
     */
    int insertWriteoff(FinApWriteoff writeoff);

    /**
     * 查询某应付单的核销历史
     *
     * @param apId 应付单ID
     * @return 核销记录列表
     */
    List<FinApWriteoff> selectByApId(@Param("apId") Long apId);

    /**
     * 累计核销金额
     *
     * @param apId 应付单ID
     * @return 累计核销额
     */
    java.math.BigDecimal sumWriteoffAmount(@Param("apId") Long apId);
}