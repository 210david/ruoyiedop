package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinInventoryCost;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 存货成本台账 Mapper
 *
 * @author ruoyi
 */
public interface FinInventoryCostMapper
{
    /**
     * 查询成本台账列表
     *
     * @param cost 查询条件
     * @return 列表
     */
    List<FinInventoryCost> selectCostList(FinInventoryCost cost);

    /**
     * 按四维唯一键查询（不加锁）
     *
     * @param materialId 物料ID
     * @param warehouseId 仓库ID
     * @param locationId 库位ID
     * @param batchNo    批次号
     * @return 成本记录，不存在返回 null
     */
    FinInventoryCost selectByDim(@Param("materialId") Long materialId,
                                 @Param("warehouseId") Long warehouseId,
                                 @Param("locationId") Long locationId,
                                 @Param("batchNo") String batchNo);

    /**
     * 按四维唯一键查询并加行锁（FOR UPDATE）
     *
     * <p>【P0-2 并发】计价写入时必须调用，防止并发结转导致成本串写。</p>
     *
     * @return 成本记录，不存在返回 null
     */
    FinInventoryCost selectByDimForUpdate(@Param("materialId") Long materialId,
                                          @Param("warehouseId") Long warehouseId,
                                          @Param("locationId") Long locationId,
                                          @Param("batchNo") String batchNo);

    /**
     * 按物料+仓库汇总结存（跨库位跨批次）
     *
     * @param materialId 物料ID
     * @param warehouseId 仓库ID
     * @return 汇总，含 totalQty/totalCost/avgCost
     */
    Map<String, Object> sumByMaterial(@Param("materialId") Long materialId,
                                      @Param("warehouseId") Long warehouseId);

    /**
     * 新增成本记录
     *
     * @param cost 成本记录
     * @return 影响行数
     */
    int insertCost(FinInventoryCost cost);

    /**
     * 更新成本记录（含乐观锁 version）
     *
     * @param cost 成本记录
     * @return 影响行数，0 表示版本冲突
     */
    int updateCost(FinInventoryCost cost);

    /**
     * 期末存货价值汇总（资产负债表取数）
     *
     * @param period 会计期间
     * @return 汇总列表
     */
    List<Map<String, Object>> sumValuation(@Param("period") String period);

    /**
     * 库存总成本（试算与总账核对用）
     *
     * @return 总成本
     */
    BigDecimal sumTotalCost();

    /**
     * 成本台账记录数
     *
     * @return 记录数
     */
    int countCost();
}