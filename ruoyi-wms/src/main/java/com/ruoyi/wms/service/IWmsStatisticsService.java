package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;

/**
 * 仓库管理统计报表 Service接口
 *
 * @author ruoyi
 */
public interface IWmsStatisticsService
{
    /**
     * 概览统计
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
     * 删除库存预警（根据预警类型处理底层数据）
     * - 临期/过期预警：软删除对应的库存记录
     * - 库存不足/积压预警：清除物料的安全库存上下限设置
     */
    public int deleteStockAlert(WmsStatisticsVO alert);
}
