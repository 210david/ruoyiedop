package com.ruoyi.food.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.ruoyi.food.domain.FoodBatchStock;
import com.ruoyi.food.domain.FoodFefoPlan;

/**
 * 食品批次库存Service接口
 *
 * 职责：
 * 1. 与 wms_inventory 双写同步（入库上架/出库/移库/盘点全链路钩子）
 * 2. FEFO批次分配（三重拦截：待检冻结禁发/最小剩余效期禁发/可用量禁发）
 *
 * @author ruoyi
 */
public interface IFoodBatchStockService
{
    /**
     * 库存增加同步（新增批次库存行或累加，并联动批次总量）
     * 若 batchNo 未关联 food_batch 且物料启用效期管理，自动创建初始化批次（存量/盘盈兜底）
     *
     * @return 批次ID（未关联批次返回null）
     */
    public Long syncAdd(Long materialId, String batchNo, Long warehouseId, Long locationId,
                        BigDecimal qty, String operator, Date productionDate, Date expiryDate);

    /**
     * 库存扣减同步（校验批次可用量，联动批次总量）
     */
    public void syncDeduct(Long materialId, String batchNo, Long warehouseId, Long locationId,
                           BigDecimal qty, String operator);

    /**
     * 库存锁定同步（qty → qtyLocked）
     */
    public void syncLock(Long materialId, String batchNo, Long warehouseId, Long locationId,
                         BigDecimal qty, String operator);

    /**
     * 库存解锁同步（qtyLocked → qty）
     */
    public void syncUnlock(Long materialId, String batchNo, Long warehouseId, Long locationId,
                           BigDecimal qty, String operator);

    /**
     * 锁定库存释放扣减同步（qtyLocked -= qty 且批次总量扣减，出库拣货完成）
     */
    public void syncDeductLocked(Long materialId, String batchNo, Long warehouseId, Long locationId,
                                 BigDecimal qty, String operator);

    /**
     * FEFO批次分配计划（不落库，供预览与出库提交共用）
     *
     * 三重拦截：批次非合格禁发（待检/冻结/报废）、最小剩余效期不足禁发、可分配量不足禁发
     *
     * @param materialId  物料ID
     * @param warehouseId 仓库ID
     * @param qty         需求数量
     * @return 分配计划（success=false 时 skipped 携带全部拦截原因）
     */
    public FoodFefoPlan planFefo(Long materialId, Long warehouseId, BigDecimal qty);

    /**
     * 人工指定批次的发运校验（三重拦截：仅合格可发/未过期/最小剩余效期）
     *
     * @return 批次ID；校验不通过抛 ServiceException
     */
    public Long validateBatchShippable(Long materialId, String batchCode);

    /**
     * 换批/占用留痕（写入批次状态日志，refBillType=outbound）
     */
    public void recordAllocLog(Long materialId, String batchCode, String operateType, String reason,
                               String refBillType, Long refBillId, String operator);

    /**
     * 按 FEFO 排序取批次可发列表（含拦截原因，供前端推荐展示）
     */
    public List<FoodBatchStock> listShippableBatches(Long materialId, Long warehouseId);

    /**
     * 跨仓扣减批次库存（mms领料场景：单据无仓库上下文，按批次库存行可用量从大到小逐行扣）
     * 仅合格批次可领用（待检/冻结/报废拦截）
     */
    public void deductAcrossWarehouse(Long materialId, String batchNo, java.math.BigDecimal qty, String operator);

    /**
     * 跨仓回补/新增批次库存（mms退料回归、完工入库成品批次场景：单据无仓库上下文）
     * 无指定仓库时优先复用该批次现有库存行仓库；批次不存在时自动创建（效期不重算：由调用方传入效期或留空沿用）
     *
     * @return 批次ID
     */
    public Long addAcrossWarehouse(Long materialId, String batchNo, java.math.BigDecimal qty, String operator,
                                   Long warehouseId, Long locationId, Date productionDate, Date expiryDate);

    /**
     * 按物料查询全部批次库存行（含批次状态/效期/仓库，供领料/退料批次弹框选择）
     */
    public List<FoodBatchStock> listBatchStocksByMaterial(Long materialId);
}
