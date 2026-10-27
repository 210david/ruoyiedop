package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinPeriod;
import org.apache.ibatis.annotations.Param;

/**
 * 会计期间 Mapper
 *
 * @author ruoyi
 */
public interface FinPeriodMapper
{
    /**
     * 按期间查询（YYYYMM）。
     *
     * @param yearMonth 会计期间
     * @return 期间对象，不存在返回 null
     */
    FinPeriod selectByPeriod(@Param("yearMonth") String yearMonth);

    /**
     * 统计已启用科目数，用于判断科目表是否初始化。
     *
     * @return 科目数
     */
    Integer countSubject();

    /**
     * 锁定期间行（结账/解锁时使用 FOR UPDATE）。
     *
     * @param yearMonth 会计期间
     * @return 期间对象
     */
    FinPeriod selectByPeriodForUpdate(@Param("yearMonth") String yearMonth);
}
