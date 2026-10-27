package com.ruoyi.food.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.domain.FoodBatchGenealogy;
import com.ruoyi.food.domain.FoodBatchReserve;
import com.ruoyi.food.domain.FoodBatchStatusLog;
import com.ruoyi.food.domain.FoodBatchStock;
import com.ruoyi.food.mapper.FoodBatchMapper;
import com.ruoyi.food.mapper.FoodBatchReserveMapper;
import com.ruoyi.food.service.IFoodBatchRuleService;
import com.ruoyi.food.service.IFoodBatchService;
import com.ruoyi.food.service.IFoodExpiryPolicyService;

/**
 * 食品批次主表Service业务层处理
 *
 * 状态机：0待检 --qualify--> 1合格 --freeze--> 2冻结 --unfreeze--> 1合格
 *         0/1/2 --scrap--> 3报废（终态）
 *
 * @author ruoyi
 */
@Service
public class FoodBatchServiceImpl implements IFoodBatchService
{
    @Autowired
    private FoodBatchMapper batchMapper;

    @Autowired
    private FoodBatchReserveMapper reserveMapper;

    @Autowired
    private IFoodBatchRuleService ruleService;

    @Autowired
    private IFoodExpiryPolicyService policyService;

    @Override
    public FoodBatch selectBatchById(Long batchId)
    {
        return batchMapper.selectBatchById(batchId);
    }

    @Override
    public FoodBatch selectBatchByCode(String batchCode)
    {
        return batchMapper.selectBatchByCode(batchCode);
    }

    @Override
    public Integer selectShelfLifeDays(Long materialId)
    {
        if (materialId == null)
        {
            return null;
        }
        Integer days = batchMapper.selectMaterialShelfLife(materialId);
        return (days != null && days > 0) ? days : null;
    }

    @Override
    public List<FoodBatch> selectBatchList(FoodBatch batch)
    {
        return batchMapper.selectBatchList(batch);
    }

    /**
     * 创建批次：规则引擎自动取号 → 效期推算固化 → 状态日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBatch(FoodBatch batch)
    {
        if (batch.getMaterialId() == null)
        {
            throw new ServiceException("物料不能为空");
        }
        if (StringUtils.isEmpty(batch.getBatchCode()))
        {
            batch.setBatchCode(ruleService.nextBatchCode(batch.getMaterialId(), batch.getMaterialCode()));
        }
        else if (batchMapper.selectBatchByCode(batch.getBatchCode()) != null)
        {
            throw new ServiceException("批次号已存在：" + batch.getBatchCode());
        }
        // 效期推算：生产日期 + 保质期天数（保质期优先取传入值，其次物料默认策略可在此扩展）
        if (batch.getExpireDate() == null && batch.getProduceDate() != null && batch.getShelfLifeDays() != null)
        {
            batch.setExpireDate(addDays(batch.getProduceDate(), batch.getShelfLifeDays()));
        }
        if (batch.getQty() == null)
        {
            batch.setQty(BigDecimal.ZERO);
        }
        if (StringUtils.isEmpty(batch.getBatchStatus()))
        {
            batch.setBatchStatus("0");
        }
        batch.setCreateBy(batch.getCreateBy() == null ? "system" : batch.getCreateBy());
        batch.setCreateTime(DateUtils.getNowDate());
        int rows = batchMapper.insertBatch(batch);
        batchMapper.refreshBatchExpireStatus(batch.getBatchId());
        insertStatusLog(batch, null, batch.getBatchStatus(), "CREATE", "批次创建", null, null);
        return rows;
    }

    /**
     * 修改批次：批次号禁改；效期字段变更后重算效期状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBatch(FoodBatch batch)
    {
        FoodBatch exist = batchMapper.selectBatchById(batch.getBatchId());
        if (exist == null)
        {
            throw new ServiceException("批次不存在");
        }
        batch.setBatchCode(null);
        if (batch.getExpireDate() == null && batch.getProduceDate() != null && batch.getShelfLifeDays() != null)
        {
            batch.setExpireDate(addDays(batch.getProduceDate(), batch.getShelfLifeDays()));
        }
        batch.setUpdateBy(batch.getUpdateBy() == null ? "system" : batch.getUpdateBy());
        int rows = batchMapper.updateBatch(batch);
        batchMapper.refreshBatchExpireStatus(batch.getBatchId());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchByIds(Long[] batchIds)
    {
        for (Long batchId : batchIds)
        {
            FoodBatch batch = batchMapper.selectBatchById(batchId);
            if (batch == null)
            {
                continue;
            }
            if (batch.getQty() != null && batch.getQty().compareTo(BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("批次【" + batch.getBatchCode() + "】仍有库存数量，不允许删除");
            }
        }
        return batchMapper.deleteBatchByIds(batchIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int qualifyBatch(Long batchId, String remark)
    {
        return changeStatus(batchId, "0", "1", "QUALIFY", remark, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int freezeBatch(Long batchId, String reason)
    {
        FoodBatch batch = getExistBatch(batchId);
        if (!"0".equals(batch.getBatchStatus()) && !"1".equals(batch.getBatchStatus()))
        {
            throw new ServiceException("批次【" + batch.getBatchCode() + "】当前状态不允许冻结（仅待检/合格可冻结）");
        }
        return changeStatus(batchId, batch.getBatchStatus(), "2", "FREEZE", reason, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unfreezeBatch(Long batchId)
    {
        return changeStatus(batchId, "2", "1", "UNFREEZE", "解冻复检合格", null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scrapBatch(Long batchId, String reason)
    {
        FoodBatch batch = getExistBatch(batchId);
        if ("3".equals(batch.getBatchStatus()))
        {
            throw new ServiceException("批次【" + batch.getBatchCode() + "】已报废");
        }
        return changeStatus(batchId, batch.getBatchStatus(), "3", "SCRAP", reason, null, null);
    }

    @Override
    public List<FoodBatchGenealogy> selectGenealogy(Long batchId, String direction)
    {
        if ("down".equalsIgnoreCase(direction))
        {
            return batchMapper.selectGenealogyByParent(batchId);
        }
        return batchMapper.selectGenealogyByChild(batchId);
    }

    @Override
    public List<FoodBatchStock> selectBatchStockList(FoodBatchStock stock)
    {
        return batchMapper.selectBatchStockList(stock);
    }

    @Override
    public List<FoodBatchStock> listBatchStocksByMaterial(Long materialId)
    {
        return batchMapper.selectBatchStocksWithBatch(materialId);
    }

    @Override
    public List<FoodBatchStatusLog> selectStatusLogList(Long batchId)
    {
        return batchMapper.selectStatusLogList(batchId);
    }

    // ---------------- 预留 ----------------

    @Override
    public List<FoodBatchReserve> selectReserveList(FoodBatchReserve reserve)
    {
        return reserveMapper.selectReserveList(reserve);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertReserve(FoodBatchReserve reserve)
    {
        FoodBatch batch = getExistBatch(reserve.getBatchId());
        if (!"1".equals(batch.getBatchStatus()))
        {
            throw new ServiceException("批次【" + batch.getBatchCode() + "】非合格状态，不允许预留");
        }
        if (reserve.getQty() == null || reserve.getQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("预留数量必须大于0");
        }
        // 定位一条可锁定的批次库存行
        FoodBatchStock query = new FoodBatchStock();
        query.setBatchId(reserve.getBatchId());
        List<FoodBatchStock> stocks = batchMapper.selectBatchStockList(query);
        FoodBatchStock target = null;
        for (FoodBatchStock s : stocks)
        {
            BigDecimal available = s.getQty().subtract(s.getQtyLocked());
            if (available.compareTo(reserve.getQty()) >= 0)
            {
                target = s;
                break;
            }
        }
        if (target == null)
        {
            throw new ServiceException("批次【" + batch.getBatchCode() + "】可用库存不足，无法预留");
        }
        target.setQtyLocked(target.getQtyLocked().add(reserve.getQty()));
        target.setUpdateBy(reserve.getCreateBy());
        if (batchMapper.updateBatchStockQty(target) != 1)
        {
            throw new ServiceException("库存并发冲突，请重试");
        }
        reserve.setMaterialId(batch.getMaterialId());
        reserve.setStatus("0");
        reserve.setCreateBy(reserve.getCreateBy() == null ? "system" : reserve.getCreateBy());
        return reserveMapper.insertReserve(reserve);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseReserve(Long reserveId)
    {
        FoodBatchReserve reserve = reserveMapper.selectReserveById(reserveId);
        if (reserve == null)
        {
            throw new ServiceException("预留记录不存在");
        }
        if (!"0".equals(reserve.getStatus()))
        {
            throw new ServiceException("仅预留中的记录可释放");
        }
        unlockStock(reserve, reserve.getCreateBy());
        FoodBatchReserve updater = new FoodBatchReserve();
        updater.setReserveId(reserveId);
        updater.setStatus("2");
        updater.setReleaseTime(DateUtils.getNowDate());
        updater.setUpdateBy(reserve.getCreateBy());
        return reserveMapper.updateReserve(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteReserveByIds(Long[] reserveIds)
    {
        for (Long reserveId : reserveIds)
        {
            FoodBatchReserve reserve = reserveMapper.selectReserveById(reserveId);
            if (reserve != null && "0".equals(reserve.getStatus()))
            {
                unlockStock(reserve, reserve.getUpdateBy());
            }
        }
        return reserveMapper.deleteReserveByIds(reserveIds);
    }

    // ---------------- 存量迁移与生产谱系桥接 ----------------

    /**
     * 存量库存迁移（幂等，可重复执行）：
     * 1. 扫描 wms_inventory qty>0 的存量库存
     * 2. 非效期物料跳过（不参与批次体系）
     * 3. 批次号缺失 → 生成 INIT{yyyyMMdd}-{inventoryId} 初始批次号回填库存
     * 4. food_batch 缺失 → 创建初始化批次（来源=初始化），效期：库存行效期优先，缺省按物料保质期自迁移日起推算
     * 5. food_batch_stock 缺行 → 按存量库存行初始化批次库存（已有行不重复累加）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Object> migrateLegacyStock()
    {
        java.util.Map<String, Object> stat = new java.util.HashMap<>();
        int inventoryRows = 0, createdBatches = 0, backfilled = 0, stockRows = 0, skipped = 0;
        List<java.util.Map<String, Object>> rows = batchMapper.selectLegacyInventoryList();
        for (java.util.Map<String, Object> row : rows)
        {
            inventoryRows++;
            Long inventoryId = ((Number) row.get("inventory_id")).longValue();
            Long materialId = ((Number) row.get("material_id")).longValue();
            String isExpiryManage = row.get("is_expiry_manage") == null ? "0" : String.valueOf(row.get("is_expiry_manage"));
            if (!"1".equals(isExpiryManage))
            {
                skipped++;
                continue;
            }
            String batchNo = row.get("batch_no") == null ? null : String.valueOf(row.get("batch_no")).trim();
            // 1. 批次号缺失 → 生成初始批次号回填
            if (StringUtils.isEmpty(batchNo))
            {
                batchNo = "INIT" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.getNowDate()) + "-" + inventoryId;
                batchMapper.updateLegacyInventoryBatchNo(inventoryId, batchNo);
                backfilled++;
            }
            // 2. 批次缺失 → 创建初始批次（效期：库存行优先，缺省按物料保质期推算）
            FoodBatch batch = batchMapper.selectBatchByCode(batchNo);
            if (batch == null)
            {
                batch = new FoodBatch();
                batch.setBatchCode(batchNo);
                batch.setMaterialId(materialId);
                batch.setMaterialCode((String) row.get("material_code"));
                batch.setMaterialName((String) row.get("material_name"));
                batch.setSourceType("6");
                batch.setSourceBillType("migration");
                batch.setSourceBillId(inventoryId);
                Integer shelfLifeDays = row.get("shelf_life_days") == null ? null : ((Number) row.get("shelf_life_days")).intValue();
                batch.setShelfLifeDays(shelfLifeDays);
                batch.setQtyUnit((String) row.get("unit"));
                java.sql.Date prodDate = row.get("production_date") == null ? null : new java.sql.Date(((java.sql.Date) row.get("production_date")).getTime());
                java.sql.Date expDate = row.get("expiry_date") == null ? null : new java.sql.Date(((java.sql.Date) row.get("expiry_date")).getTime());
                if (expDate != null)
                {
                    batch.setProduceDate(prodDate != null ? prodDate : DateUtils.getNowDate());
                    batch.setExpireDate(expDate);
                }
                else
                {
                    // 补录效期：迁移日为生产日，按物料保质期推算到期日（无保质期则效期留空由人工补录）
                    batch.setProduceDate(DateUtils.getNowDate());
                    if (shelfLifeDays != null && shelfLifeDays > 0)
                    {
                        batch.setExpireDate(addDays(DateUtils.getNowDate(), shelfLifeDays));
                    }
                }
                batch.setReceiveDate(DateUtils.getNowDate());
                batch.setBatchStatus("1"); // 存量在库批次默认合格放行，避免迁移即冻结卡死出库；异议批次走批次操作冻结
                batch.setCreateBy("migration");
                batchMapper.insertBatch(batch);
                batchMapper.refreshBatchExpireStatus(batch.getBatchId());
                createdBatches++;
            }
            else if (!materialId.equals(batch.getMaterialId()))
            {
                throw new com.ruoyi.common.exception.ServiceException("存量库存行 " + inventoryId
                        + " 批次【" + batchNo + "】归属物料与库存物料不一致，请先人工核对");
            }
            // 3. 批次库存缺行 → 初始化（幂等：已有任意行则跳过）
            FoodBatchStock q = new FoodBatchStock();
            q.setBatchId(batch.getBatchId());
            List<FoodBatchStock> stocks = batchMapper.selectBatchStockList(q);
            if (stocks.isEmpty())
            {
                FoodBatchStock ns = new FoodBatchStock();
                ns.setBatchId(batch.getBatchId());
                ns.setMaterialId(materialId);
                ns.setWarehouseId(((Number) row.get("warehouse_id")).longValue());
                ns.setLocationId(((Number) row.get("location_id")).longValue());
                ns.setQty((java.math.BigDecimal) row.get("qty"));
                ns.setQtyLocked(java.math.BigDecimal.ZERO);
                ns.setCreateBy("migration");
                batchMapper.insertBatchStock(ns);
                batchMapper.updateBatchQty(batch.getBatchId(), (java.math.BigDecimal) row.get("qty"));
                stockRows++;
            }
        }
        stat.put("inventoryRows", inventoryRows);
        stat.put("createdBatches", createdBatches);
        stat.put("backfilled", backfilled);
        stat.put("stockRows", stockRows);
        stat.put("skipped", skipped);
        return stat;
    }

    /**
     * 生产投入谱系桥接（幂等）：完工入库确认后，把该工单领料明细的原料批次 → 成品批次写谱系
     * relation_type=1 投入（多对多），关联单据=完工入库单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int bridgeProductionEdges(String workOrderNo, Long productBatchId, Long refBillId, String refBillCode, String operator)
    {
        if (StringUtils.isEmpty(workOrderNo) || productBatchId == null)
        {
            return 0;
        }
        // 已有本完工单桥接边则幂等跳过
        List<FoodBatchGenealogy> existEdges = batchMapper.selectGenealogyByChild(productBatchId);
        for (FoodBatchGenealogy g : existEdges)
        {
            if ("1".equals(g.getRelationType()) && "finish".equals(g.getRefBillType())
                    && refBillId != null && refBillId.equals(g.getRefBillId()))
            {
                return 0;
            }
        }
        List<java.util.Map<String, Object>> feedingRows = batchMapper.selectFeedingBatchesByWorkOrder(workOrderNo);
        int edges = 0;
        for (java.util.Map<String, Object> fr : feedingRows)
        {
            String batchNo = fr.get("batch_no") == null ? null : String.valueOf(fr.get("batch_no"));
            if (StringUtils.isEmpty(batchNo))
            {
                continue;
            }
            FoodBatch parent = batchMapper.selectBatchByCode(batchNo);
            if (parent == null)
            {
                continue; // 原料批次未登记（非效期物料等），跳过
            }
            FoodBatch child = batchMapper.selectBatchById(productBatchId);
            if (child == null || parent.getBatchId().equals(child.getBatchId()))
            {
                continue;
            }
            FoodBatchGenealogy genealogy = new FoodBatchGenealogy();
            genealogy.setParentBatchId(parent.getBatchId());
            genealogy.setChildBatchId(child.getBatchId());
            genealogy.setRelationType("1");
            genealogy.setQty((java.math.BigDecimal) fr.get("issue_qty"));
            genealogy.setQtyUnit((String) fr.get("unit"));
            genealogy.setRefBillType("finish");
            genealogy.setRefBillId(refBillId);
            genealogy.setRefBillCode(refBillCode);
            genealogy.setCreateBy(operator);
            batchMapper.insertGenealogy(genealogy);
            edges++;
        }
        return edges;
    }

    // ---------------- 私有方法 ----------------

    private FoodBatch getExistBatch(Long batchId)
    {
        FoodBatch batch = batchMapper.selectBatchById(batchId);
        if (batch == null)
        {
            throw new ServiceException("批次不存在或已删除");
        }
        return batch;
    }

    private int changeStatus(Long batchId, String fromStatus, String toStatus, String operateType, String reason, String refBillType, Long refBillId)
    {
        FoodBatch batch = getExistBatch(batchId);
        batchMapper.updateBatchStatus(batchId, fromStatus, toStatus, toStatus.equals("2") ? reason : null);
        insertStatusLog(batch, fromStatus, toStatus, operateType, reason, refBillType, refBillId);
        return 1;
    }

    private void insertStatusLog(FoodBatch batch, String fromStatus, String toStatus, String operateType, String reason, String refBillType, Long refBillId)
    {
        FoodBatchStatusLog log = new FoodBatchStatusLog();
        log.setBatchId(batch.getBatchId());
        log.setBatchCode(batch.getBatchCode());
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperateType(operateType);
        log.setReason(reason);
        log.setRefBillType(refBillType);
        log.setRefBillId(refBillId);
        log.setOperateBy(batch.getCreateBy());
        batchMapper.insertStatusLog(log);
    }

    /**
     * 释放预留锁定的库存
     */
    private void unlockStock(FoodBatchReserve reserve, String operator)
    {
        FoodBatchStock query = new FoodBatchStock();
        query.setBatchId(reserve.getBatchId());
        List<FoodBatchStock> stocks = batchMapper.selectBatchStockList(query);
        BigDecimal remain = reserve.getQty();
        for (FoodBatchStock s : stocks)
        {
            if (remain.compareTo(BigDecimal.ZERO) <= 0)
            {
                break;
            }
            BigDecimal locked = s.getQtyLocked();
            if (locked.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            BigDecimal deduct = locked.min(remain);
            s.setQtyLocked(locked.subtract(deduct));
            s.setUpdateBy(operator);
            batchMapper.updateBatchStockQty(s);
            remain = remain.subtract(deduct);
        }
    }

    private Date addDays(Date date, int days)
    {
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(days);
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
