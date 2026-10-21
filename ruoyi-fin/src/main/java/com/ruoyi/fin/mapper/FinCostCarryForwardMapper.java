package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinCostCarryForward;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 成本结转流水 Mapper
 *
 * @author ruoyi
 */
public interface FinCostCarryForwardMapper
{
    /**
     * 查询结转流水
     *
     * @param cf 查询条件
     * @return 流水列表
     */
    List<FinCostCarryForward> selectCarryList(FinCostCarryForward cf);

    /**
     * 按幂等键查询
     *
     * @param sourceModule 来源模块
     * @param sourceType   来源类型
     * @param sourceId     来源明细ID
     * @param materialId   物料ID
     * @return 流水，不存在返回 null
     */
    FinCostCarryForward selectBySource(@Param("sourceModule") String sourceModule,
                                       @Param("sourceType") String sourceType,
                                       @Param("sourceId") Long sourceId,
                                       @Param("materialId") Long materialId);

    /**
     * 新增结转流水
     *
     * @param cf 结转流水
     * @return 影响行数
     */
    int insertCarry(FinCostCarryForward cf);

    /**
     * 按期间汇总结转金额（总账取数）
     *
     * @param period 会计期间
     * @return 按 target_type 汇总
     */
    List<Map<String, Object>> sumByPeriod(@Param("period") String period);
}