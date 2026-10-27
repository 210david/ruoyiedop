package com.ruoyi.food.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.domain.FoodBatchGenealogy;
import com.ruoyi.food.domain.FoodBatchStock;
import com.ruoyi.food.domain.FoodBatchStatusLog;

/**
 * 食品批次主表Mapper接口（含谱系/库存/状态日志）
 *
 * @author ruoyi
 */
public interface FoodBatchMapper
{
    public FoodBatch selectBatchById(Long batchId);

    public FoodBatch selectBatchByCode(String batchCode);

    /**
     * 物料效期信息（跨模块轻量查询：is_expiry_manage/shelf_life_days/material_type/unit）
     */
    public java.util.Map<String, Object> selectMaterialInfo(Long materialId);

    public List<FoodBatch> selectBatchList(FoodBatch batch);

    /**
     * 临期批次（效期状态=临期，未冻结/报废，有库存）
     */
    public List<FoodBatch> selectNearExpiryBatches();

    /**
     * 已过期未冻结批次（效期状态=过期，待检/合格，有库存）
     */
    public List<FoodBatch> selectExpiredNotFrozenBatches();

    public int insertBatch(FoodBatch batch);

    public int updateBatch(FoodBatch batch);

    public int deleteBatchByIds(Long[] batchIds);

    public int updateBatchQty(Long batchId, BigDecimal qty);

    /**
     * 条件更新批次状态（乐观校验原状态），返回影响行数
     */
    public int updateBatchStatus(Long batchId, String fromStatus, String toStatus, String freezeReason);

    /**
     * 批量刷新效期状态（返回影响行数）
     */
    public int refreshExpireStatus();

    /**
     * 按批次重算效期状态
     */
    public int refreshBatchExpireStatus(Long batchId);

    // ---------------- 谱系 ----------------

    public int insertGenealogy(FoodBatchGenealogy genealogy);

    public List<FoodBatchGenealogy> selectGenealogyByChild(Long childBatchId);

    public List<FoodBatchGenealogy> selectGenealogyByParent(Long parentBatchId);

    // ---------------- 批次库存 ----------------

    public List<FoodBatchStock> selectBatchStockList(FoodBatchStock stock);

    public FoodBatchStock selectBatchStock(Long batchId, Long warehouseId, Long locationId);

    /**
     * FEFO候选：仓库内某物料全部有可用量的批次库存行（含批次状态/效期，按到期日升序）
     */
    public List<FoodBatchStock> selectFefoStockCandidates(@org.apache.ibatis.annotations.Param("materialId") Long materialId,
                                                          @org.apache.ibatis.annotations.Param("warehouseId") Long warehouseId);

    public int insertBatchStock(FoodBatchStock stock);

    public int updateBatchStockQty(FoodBatchStock stock);

    /**
     * 按物料查询全部批次库存行（含批次状态/效期/仓库，供领退料批次选择）
     */
    public List<FoodBatchStock> selectBatchStocksWithBatch(@org.apache.ibatis.annotations.Param("materialId") Long materialId);

    /**
     * 系统默认仓库（启用中的第一个仓库节点）
     */
    public java.util.Map<String, Object> selectDefaultWarehouse();

    /**
     * 查询物料保质期天数（非效期物料/未配置返回 null）
     */
    public Integer selectMaterialShelfLife(Long materialId);

    /**
     * 存量库存清单（qty>0，用于存量迁移扫描）
     */
    public List<java.util.Map<String, Object>> selectLegacyInventoryList();

    /**
     * 回填存量库存批次号
     */
    public int updateLegacyInventoryBatchNo(@org.apache.ibatis.annotations.Param("inventoryId") Long inventoryId,
                                            @org.apache.ibatis.annotations.Param("batchNo") String batchNo);

    /**
     * 工单已确认领料明细的投入批次（生产谱系桥接数据源：mms_issue + detail，状态=已领料）
     */
    public List<java.util.Map<String, Object>> selectFeedingBatchesByWorkOrder(@org.apache.ibatis.annotations.Param("workOrderNo") String workOrderNo);

    // ---------------- 状态日志（只增查，禁删改） ----------------

    public int insertStatusLog(FoodBatchStatusLog log);

    public List<FoodBatchStatusLog> selectStatusLogList(Long batchId);
}
