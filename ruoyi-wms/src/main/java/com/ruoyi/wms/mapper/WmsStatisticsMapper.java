package com.ruoyi.wms.mapper;

import java.util.List;
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;
import org.apache.ibatis.annotations.Param;

/**
 * 仓库管理统计报表 Mapper
 *
 * @author ruoyi
 */
public interface WmsStatisticsMapper
{
    /**
     * 概览统计：物料总数、库存总量、仓库总数、入库单数、出库单数
     */
    public WmsStatisticsVO selectOverview();

    /**
     * 按仓库统计库存分布
     */
    public List<WmsStatisticsVO> selectInventoryByWarehouse();

    /**
     * 按物料类型统计库存分布
     */
    public List<WmsStatisticsVO> selectInventoryByMaterialType();

    /**
     * 出入库趋势（按日统计）
     */
    public List<WmsStatisticsVO> selectInboundOutboundTrend(WmsStatisticsVO query);

    /**
     * 入库按类型统计
     */
    public List<WmsStatisticsVO> selectInboundByType(WmsStatisticsVO query);

    /**
     * 出库按类型统计
     */
    public List<WmsStatisticsVO> selectOutboundByType(WmsStatisticsVO query);

    /**
     * 物料库存排行 Top N
     */
    public List<WmsStatisticsVO> selectMaterialTop(WmsStatisticsVO query);

    /**
     * 库存预警（低于安全库存下限）
     */
    public List<WmsStatisticsVO> selectStockAlert();

    /**
     * 库存预警列表（统一：低库存/积压/临期/过期），支持筛选
     */
    public List<WmsStatisticsVO> selectStockAlertList(WmsStatisticsVO query);

    /**
     * 库存预警汇总统计（各类型预警数量）
     */
    public List<WmsStatisticsVO> selectStockAlertSummary();

    /**
     * 根据预警信息删除对应的库存记录（用于临期/过期预警）
     */
    public int deleteInventoryByAlert(@Param("materialId") Long materialId, @Param("warehouseId") Long warehouseId,
                                      @Param("productionDate") java.util.Date productionDate, @Param("expiryDate") java.util.Date expiryDate);

    /**
     * 清除物料的安全库存上下限设置（用于库存不足/积压预警）
     */
    public int clearSafetyStock(@Param("materialId") Long materialId);
}
