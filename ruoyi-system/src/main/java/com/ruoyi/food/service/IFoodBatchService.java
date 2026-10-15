package com.ruoyi.food.service;

import java.util.List;
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.domain.FoodBatchGenealogy;
import com.ruoyi.food.domain.FoodBatchReserve;
import com.ruoyi.food.domain.FoodBatchStock;
import com.ruoyi.food.domain.FoodBatchStatusLog;

/**
 * 食品批次主表Service接口
 *
 * @author ruoyi
 */
public interface IFoodBatchService
{
    public FoodBatch selectBatchById(Long batchId);

    /**
     * 按批次号查询批次（mms完工入库判断成品批次是否已登记）
     */
    public FoodBatch selectBatchByCode(String batchCode);

    /**
     * 查询物料保质期天数（供生产/收货环节创建批次时推算效期；未配置返回 null）
     */
    public Integer selectShelfLifeDays(Long materialId);

    public List<FoodBatch> selectBatchList(FoodBatch batch);

    /**
     * 创建批次：自动取号（规则引擎）、效期推算、写状态日志
     */
    public int insertBatch(FoodBatch batch);

    /**
     * 修改批次（批次号禁改；效期字段变更后重算效期状态）
     */
    public int updateBatch(FoodBatch batch);

    public int deleteBatchByIds(Long[] batchIds);

    /**
     * 合格判定（0待检→1合格）
     */
    public int qualifyBatch(Long batchId, String remark);

    /**
     * 冻结（0/1→2）
     */
    public int freezeBatch(Long batchId, String reason);

    /**
     * 解冻（2→1）
     */
    public int unfreezeBatch(Long batchId);

    /**
     * 报废（0/1/2→3）
     */
    public int scrapBatch(Long batchId, String reason);

    /**
     * 谱系查询
     *
     * @param direction up=向上追溯（本批次的父批次关系） down=向下追踪（本批次的子批次关系）
     */
    public List<FoodBatchGenealogy> selectGenealogy(Long batchId, String direction);

    public List<FoodBatchStock> selectBatchStockList(FoodBatchStock stock);

    /**
     * 按物料查询全部批次库存行（含批次状态/效期/仓库，供领料/退料批次弹框选择）
     */
    public List<FoodBatchStock> listBatchStocksByMaterial(Long materialId);

    public List<FoodBatchStatusLog> selectStatusLogList(Long batchId);

    // ---------------- 预留 ----------------

    public List<FoodBatchReserve> selectReserveList(FoodBatchReserve reserve);

    /**
     * 新增预留：校验批次可预留（合格且有可用库存），锁定批次库存qty_locked
     */
    public int insertReserve(FoodBatchReserve reserve);

    /**
     * 释放预留：解锁库存
     */
    public int releaseReserve(Long reserveId);

    public int deleteReserveByIds(Long[] reserveIds);

    /**
     * 存量库存迁移：扫描 wms_inventory（qty>0）为效期物料生成初始批次并补录效期，回填批次号、初始化批次库存（幂等）
     *
     * @return 迁移统计（inventoryRows/createdBatches/backfilled/stockRows/skipped）
     */
    public java.util.Map<String, Object> migrateLegacyStock();

    /**
     * 生产投入谱系桥接：完工入库确认后调用，把该工单领料明细的原料批次 → 成品批次
     * 写入谱系（relation_type=1 投入，多对多，幂等）
     *
     * @param workOrderNo    工单号
     * @param productBatchId 成品批次ID
     * @param refBillId      关联单据ID（完工入库单ID）
     * @param refBillCode    关联单据号（完工入库单号）
     * @param operator       操作人
     * @return 写入谱系边数
     */
    public int bridgeProductionEdges(String workOrderNo, Long productBatchId, Long refBillId, String refBillCode, String operator);
}
