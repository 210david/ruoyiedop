package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.vo.TraceFallbackVO;
import org.apache.ibatis.annotations.Param;

/**
 * 降级追溯 Mapper
 *
 * 利用已有业务单据中的 batch_no 做辅助关联追溯，
 * 当谱系表无数据时提供多维度信息。
 *
 * @author ruoyi
 */
public interface QmsTraceFallbackMapper
{
    /** 查询检验任务（QMS，按 batch_no 关联） */
    List<TraceFallbackVO.InspTaskSummary> selectInspTasksByBatchNo(@Param("batchNo") String batchNo);

    /** 查询 NCR（QMS，按 batch_no 关联） */
    List<TraceFallbackVO.NcrSummary> selectNcrsByBatchNo(@Param("batchNo") String batchNo);

    /** 查询客诉（QMS，按 batch_no 关联） */
    List<TraceFallbackVO.ComplaintSummary> selectComplaintsByBatchNo(@Param("batchNo") String batchNo);

    /** 查询仓库库存流水（WMS，按 batch_no 关联） */
    List<TraceFallbackVO.InventoryLogSummary> selectInventoryLogsByBatchNo(@Param("batchNo") String batchNo);

    /** 查询仓库库存快照（WMS，按 batch_no 关联） */
    List<TraceFallbackVO.InventorySummary> selectInventoryByBatchNo(@Param("batchNo") String batchNo);

    /** 查询采购收货明细（PMS，按 batch_no 关联） */
    List<TraceFallbackVO.ReceiveSummary> selectReceiveDetailsByBatchNo(@Param("batchNo") String batchNo);

    /**
     * 查询销售发货记录（MK→WMS，通过 mk_shipment.outbound_order_no → wms_outbound_order_detail.batch_no 关联）
     */
    List<TraceFallbackVO.ShipmentSummary> selectShipmentsByBatchNo(@Param("batchNo") String batchNo);
}
